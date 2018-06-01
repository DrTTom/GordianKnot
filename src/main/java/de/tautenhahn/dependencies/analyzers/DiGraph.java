package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

    private final Set<IndexedNode> successors = new HashSet<>();

    private final Set<IndexedNode> predecessors = new HashSet<>();

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


    public int getIndex()
    {
      return index;
    }


    public Node getNode()
    {
      return node;
    }


    public Collection<IndexedNode> getSuccessors()
    {
      return successors;
    }

    public int getNumberClasses()
    {
      return numberClasses;
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
  public DiGraph(Stream<Node> originalNodes)
  {
    nodes = originalNodes.map(IndexedNode::new).collect(Collectors.toCollection(() -> new ArrayList<>()));
    Map<Node, IndexedNode> indexes = new HashMap<>();
    for ( int i = 0 ; i < nodes.size() ; i++ )
    {
      IndexedNode node = nodes.get(i);
      node.index = i;
      indexes.put(node.node, node);
    }
    for ( IndexedNode node : nodes )
    {
      node.node.getSuccessors()
               .stream()
               .map(indexes::get)
               .filter(Objects::nonNull)
               .forEach(succ -> addArc(node, succ));
    }
  }

  public void renumber()
  {
    for ( int i = 0 ; i < nodes.size() ; i++ )
    {
      nodes.get(i).index = i;
    }
  }

  DiGraph(Collection<IndexedNode> nodesToRetain)
  {
    Map<Node, IndexedNode> indexes = new HashMap<>();
    nodes = nodesToRetain.stream()
                         .map(IndexedNode::new)
                         .peek(n -> indexes.put(n.node, n))
                         .collect(Collectors.toCollection(() -> new ArrayList<>()));
    for ( IndexedNode original : nodesToRetain )
    {
      IndexedNode copy = indexes.get(original.node);
      original.successors.stream()
                         .map(s -> indexes.get(s.node))
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

  public void removeArc(IndexedNode from, IndexedNode to)
  {
    to.predecessors.remove(from);
    from.successors.remove(to);
  }

  public void addArc(IndexedNode from, IndexedNode to)
  {
    to.predecessors.add(from);
    from.successors.add(to);
  }

  public List<IndexedNode> getAllNodes()
  {
    return nodes;
  }

}
