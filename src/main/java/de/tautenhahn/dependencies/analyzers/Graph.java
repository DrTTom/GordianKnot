package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node;


/**
 * Represents the simple directed graph created from the original dependency structure. While the dependencies
 * are defined by the analyzed project only, graphs may be created and altered in different ways. Elements of
 * this class will be added when needed.
 *
 * @author TT
 */
public class Graph
{

  private final Node[] nodes;

  // not sure these types are sensible, will change if needed:
  private final Map<Integer, Arc>[] outgoingArcs;

  private final List<Arc>[] incomingArcs;

  private int[] nodeWeights;

  private final NodeDecoration[] decorations;

  /**
   * Represents an arc and its properties
   */
  public static class Arc
  {

    Arc(int from, int to)
    {
      this.from = from;
      this.to = to;
    }

    int from;

    int to;

    String styleClass;
  }

  /**
   * Additional properties stored for a node.
   */
  public static class NodeDecoration
  {

    NodeDecoration(int numberClasses)
    {
      this.numberClasses = numberClasses;
    }

    int numberClasses;

    String styleClass;
  }


  /**
   * Creates new instance using all nodes which are not inside collapsed ones. This instance represents the
   * current state of the dependency structure, ignoring Nodes without content. Upon collapsing or expanding
   * some nodes, create a new one.
   *
   * @param root
   */
  @SuppressWarnings("unchecked")
  public Graph(ContainerNode root)
  {
    List<Node> allNodes = root.walkSubTree()
                              .filter(Node::hasOwnContent)
                              .collect(Collectors.toCollection(() -> new ArrayList<>()));
    nodes = allNodes.toArray(new Node[0]);
    decorations = new NodeDecoration[nodes.length];
    Map<Node, Integer> nodeNumber = new HashMap<>();
    for ( int i = 0 ; i < nodes.length ; i++ )
    {
      Node n = nodes[i];
      nodeNumber.put(n, Integer.valueOf(i));
      int weight = (int)(n instanceof ContainerNode ? ((ContainerNode)n).getContainedLeafs().count() : 1);
      decorations[i] = new NodeDecoration(weight);
    }
    outgoingArcs = new Map[nodes.length];
    incomingArcs = new ArrayList[nodes.length];
    for ( int i = 0 ; i < nodes.length ; i++ )
    {
      int from = i;
      for ( Node suc : nodes[i].getSuccessors() )
      {
        Optional.ofNullable(nodeNumber.get(suc)).ifPresent(to -> addArc(from, to.intValue()));
      }
    }
  }

  /**
   * Returns an induced subgraph. Method is here because complete new structure is built.
   *
   * @param existing
   * @param retainNodes
   */
  @SuppressWarnings("unchecked")
  Graph(Graph existing, List<Integer> retainNodes)
  {
    nodes = new Node[retainNodes.size()];
    decorations = new NodeDecoration[nodes.length];
    int[] newIndex = new int[existing.numberNodes()];
    Arrays.fill(newIndex, -1);
    for ( int i = 0 ; i < retainNodes.size() ; i++ )
    {
      int oldIndex = retainNodes.get(i).intValue();
      newIndex[oldIndex] = i;
      nodes[i] = existing.getNode(oldIndex);
      decorations[i] = new NodeDecoration(existing.getNodeDecoration(oldIndex).numberClasses);
    }
    outgoingArcs = new Map[nodes.length];
    incomingArcs = new ArrayList[nodes.length];
    for ( Integer oldNode : retainNodes )
    {
      int from = newIndex[oldNode.intValue()];
      for ( int oldTo : existing.getSuccessors(oldNode.intValue()) )
      {
        int to = newIndex[oldTo];
        if (to != -1)
        {
          addArc(from, to);
        }
      }
    }
  }

  /**
   * Returns an arc if exists.
   *
   * @param from
   * @param to
   */
  public Arc getArc(int from, int to)
  {
    return Optional.ofNullable(outgoingArcs[from]).map(m -> m.get(Integer.valueOf(to))).orElse(null);
  }

  /**
   * Adds or replaces an arc.
   *
   * @param from
   * @param to
   */
  public Arc addArc(int from, int to)
  {
    Arc result = new Arc(from, to);
    if (outgoingArcs[from] == null)
    {
      outgoingArcs[from] = new HashMap<>();
    }
    if (incomingArcs[to] == null)
    {
      incomingArcs[to] = new ArrayList<>();
    }
    outgoingArcs[from].put(Integer.valueOf(to), result);
    incomingArcs[to].add(result);
    return result;
  }

  /**
   * Removes an arc, does nothing if it does not exist.
   *
   * @param from
   * @param to
   */
  public void removeArc(int from, int to)
  {
    Optional.ofNullable(outgoingArcs[from]).ifPresent(m -> m.remove(Integer.valueOf(to)));
    Optional.ofNullable(incomingArcs[to]).ifPresent(l -> l.removeIf(a -> a.from == from));
  }

  /**
   * Returns a node.
   *
   * @param number
   */
  public Node getNode(int number)
  {
    return nodes[number];
  }

  /**
   * Returns the additional information added for a node.
   *
   * @param number
   */
  public NodeDecoration getNodeDecoration(int number)
  {
    return decorations[number];
  }

  public Collection<Integer> getSuccessors(int node)
  {
    return Optional.ofNullable(outgoingArcs[node]).map(Map::keySet).orElse(Collections.emptySet());
  }

  public Collection<Integer> getPredecessors(int node)
  {
    if (incomingArcs[node] == null)
    {
      return Collections.emptyList();
    }
    return incomingArcs[node].stream().map(a -> Integer.valueOf(a.from)).collect(Collectors.toList());
  }

  public int numberNodes()
  {
    return nodes.length;
  }

}
