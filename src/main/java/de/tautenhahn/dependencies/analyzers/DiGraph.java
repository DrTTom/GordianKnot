package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node;


/**
 * Represents the simple directed graph created from the original dependency structure. While the dependencies
 * are defined by the analyzed project and parsing filter rules only, graphs may be created and altered in
 * different ways. Elements of this class will be added when needed.
 *
 * @author TT
 */
public class DiGraph
{

  private final List<IndexedNode> nodes;

  /**
   * Allows faster access to nodes.
   */
  public static class IndexedNode
  {

    private final Node node;

    private int index;

    private final int numberClasses;

    private final Set<IndexedNode> successors = new LinkedHashSet<>();

    private final Set<IndexedNode> predecessors = new LinkedHashSet<>();

    IndexedNode(Node node)
    {
      this.node = node;
      this.numberClasses = (int)(node instanceof ContainerNode
        ? ((ContainerNode)node).getContainedLeafs().count() : 1);
    }

    IndexedNode(IndexedNode original)
    {
      node = original.node;
      numberClasses = original.numberClasses;
    }

    /**
     * Returns the index of the node. Nodes should ne bumbered consecutively.
     */
    public int getIndex()
    {
      return index;
    }

    /**
     * Returns the original parsed node represented by this object.
     */
    public Node getNode()
    {
      return node;
    }

    /**
     * Returns the successors.
     */
    public Collection<IndexedNode> getSuccessors()
    {
      return Collections.unmodifiableCollection(successors);
    }

    /**
     * Returns the predecessors.
     */
    public Collection<IndexedNode> getPredecessors()
    {
      return Collections.unmodifiableCollection(predecessors);
    }

    /**
     * Returns the number of represented classes.
     */
    public int getNumberClasses()
    {
      return numberClasses;
    }

    @Override
    public String toString()
    {
      return index + ". " + node;
    }
  }

  /**
   * Returns an instance created from all visible nodes which have some content.
   *
   * @param root
   */
  public DiGraph(ContainerNode root)
  {
    this(root.walkSubTree().filter(Node::hasOwnContent));
  }

  /**
   * Creates instance created for specified nodes only.
   */
  @SuppressWarnings("synthetic-access")
  public DiGraph(Stream<Node> originalNodes)
  {
    nodes = originalNodes.map(IndexedNode::new).collect(Collectors.toCollection(() -> new ArrayList<>()));
    Map<Node, IndexedNode> indexes = new HashMap<>();
    for ( int i = 0 ; i < nodes.size() ; i++ )
    {
      IndexedNode node = nodes.get(i);
      node.index = i; // NOPMD want to disable access for any other class
      indexes.put(node.getNode(), node);
    }
    for ( IndexedNode node : nodes )
    {
      node.getNode()
          .getSuccessors()
          .stream()
          .map(indexes::get)
          .filter(Objects::nonNull)
          .forEach(succ -> addArc(node, succ));
    }
  }

  /**
   * Numbers the nodes from 0 to number of nodes. Sequence is not defined.
   */
  @SuppressWarnings("synthetic-access")
  public final void renumber()
  {
    for ( int i = 0 ; i < nodes.size() ; i++ )
    {
      nodes.get(i).index = i; // NOPMD want to disable access for any other class
    }
  }

  DiGraph(Collection<IndexedNode> nodesToRetain)
  {
    Map<Node, IndexedNode> indexes = new HashMap<>();
    nodes = nodesToRetain.stream()
                         .map(IndexedNode::new)
                         .peek(n -> indexes.put(n.getNode(), n))
                         .collect(Collectors.toCollection(() -> new ArrayList<>()));
    for ( IndexedNode original : nodesToRetain )
    {
      IndexedNode copy = indexes.get(original.getNode());
      original.getSuccessors()
              .stream()
              .map(s -> indexes.get(s.getNode()))
              .filter(Objects::nonNull)
              .forEach(s -> addArc(copy, s));
    }
    renumber();
  }

  DiGraph(DiGraph... others)
  {
    nodes = new ArrayList<>();
    for ( DiGraph other : others )
    {
      DiGraph otherCopy = new DiGraph(other.nodes);
      nodes.addAll(otherCopy.nodes);
    }
    renumber();
  }

  /**
   * Removes specified arc from the graph, does not change underlying parsed structure.
   *
   * @param from
   * @param to
   */
  @SuppressWarnings("synthetic-access")
  public void removeArc(IndexedNode from, IndexedNode to)
  {
    to.predecessors.remove(from); // NOPMD want to hide this access from all other classes
    from.successors.remove(to); // NOPMD
  }

  /**
   * Adds an arc to the graph, does not change underlying parsed structure.
   *
   * @param from
   * @param to
   */
  @SuppressWarnings("synthetic-access")
  public void addArc(IndexedNode from, IndexedNode to)
  {
    to.predecessors.add(from); // NOPMD want to hide this access from all other classes
    from.successors.add(to); // NOPMD
  }

  /**
   * Returns the list of all nodes. If node set was not changed till creation or last call of
   * {@link #renumber()}, list index is node index.
   */
  public List<IndexedNode> getAllNodes()
  {
    return nodes;
  }

}
