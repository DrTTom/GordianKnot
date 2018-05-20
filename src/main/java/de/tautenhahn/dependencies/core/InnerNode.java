package de.tautenhahn.dependencies.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Inner node of the containment structure. Those nodes have relations exclusively depending on the relations
 * of their children.
 * 
 * @author TT
 */
public class InnerNode extends Node
{

  public static final char SEPARATOR = '.';

  private final List<Node> children = new ArrayList<>();

  private InnerNode(Node parent, String name)
  {
    super(parent, name);
  }

  /**
   * Creates a new instance as child of current node.
   * 
   * @param name simple name of class, package, jar or module
   */
  public InnerNode createInnerChild(String name)
  {
    return new InnerNode(this, name);
  }

  /**
   * Creates a new instance as child of current node.
   * 
   * @param name simple name of class, package, jar or module
   */
  public Leaf createLeaf(String name)
  {
    return new Leaf(this, name);
  }

  public Node find(String path)
  {
    int pos = path.indexOf(SEPARATOR);
    String first = pos > 0 ? path.substring(0, pos) : path;
    String rest = pos > 0 ? path.substring(pos + 1) : null;
    Node result = children.stream().filter(n -> n.getSimpleName().equals(first)).findAny().orElse(null);
    return rest == null || result == null ? result : (InnerNode)result.find(rest);
  }


  /**
   * Creates a root node with no parent and no name.
   */
  public static InnerNode createRoot()
  {
    return new InnerNode(null, "");
  }

  /**
   * Returns the children on the container structure.
   */
  @Override
  public List<Node> getChildren()
  {
    return Collections.unmodifiableList(children);
  }

  /**
   * Returns the nodes this node depends on.
   */
  @Override
  public List<Node> getSuccessors()
  {
    Stream<? extends Node> relevantNodes = isCollapsed() ? walkSubTree() : children.stream();
    return relevantNodes.filter(x -> x instanceof Leaf)
                        .flatMap(l -> l.getSuccessors().stream())
                        .distinct()
                        .map(this::replaceByCollapsedAnchestor)
                        .distinct()
                        .collect(Collectors.toList());
  }

  private Node replaceByCollapsedAnchestor(Node n)
  {
    Node result = n;
    Node parent = n.getParent();
    while (parent != null)
    {
      if (parent.isCollapsed())
      {
        result = parent;
      }
      parent = parent.getParent();
    }
    return result;
  }

  @Override
  public List<Node> getPredecessors()
  {
    return null;
  }

  @Override
  public List<List<Node>> getDependencyReason(Node other)
  {
    // TODO
    return null;
  }

  @Override
  Stream<Node> walkSubTree()
  {
    return children.stream().flatMap(n -> Stream.concat(n.walkSubTree(), Stream.of(n)));
  }
}
