package de.tautenhahn.dependencies.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
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
   * @param name must be relative to current node
   */
  public InnerNode createInnerChild(String name)
  {
    return createChild(name, InnerNode::new);
  }

  /**
   * Creates a new instance as child of current node.
   * 
   * @param name must be relative to current node
   */
  public Leaf createLeaf(String name)
  {
    return createChild(name, Leaf::new);
  }

  private <T extends Node> T createChild(String name, BiFunction<Node, String, T> constructor)
  {
    Pair<String, String> parts = splitPath(name);
    if (parts.getSecond() == null)
    {
      T result = constructor.apply(this, name);
      children.add(result);
      return result;
    }

    Node intermed = find(parts.getFirst());
    if (intermed instanceof Leaf)
    {
      throw new IllegalArgumentException("cannot add a child of " + intermed.getName());
    }
    if (intermed == null)
    {
      intermed = createInnerChild(parts.getFirst());
    }
    return ((InnerNode)intermed).createChild(parts.getSecond(), constructor);
  }


  /**
   * Creates a virtual root node with no parent and no name.
   */
  public static InnerNode createRoot()
  {
    return new InnerNode(null, null);
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
    return getNeighbours(Leaf::getSucLeafs);
  }

  private List<Node> getNeighbours(Function<Leaf, List<Node>> lister)
  {
    Stream<? extends Node> relevantNodes = isCollapsed() ? walkHiddenSubTree() : children.stream();
    return relevantNodes.filter(x -> x instanceof Leaf)
                        .map(n -> (Leaf)n)
                        .flatMap(l -> lister.apply(l).stream())
                        .distinct()
                        .map(this::replaceByCollapsedAnchestor)
                        .distinct()
                        .collect(Collectors.toList());
  }

  @Override
  public List<Node> getPredecessors()
  {
    return getNeighbours(Leaf::getPredLeafs);
  }

  @Override
  public List<Pair<Node, Node>> getDependencyReason(Node other)
  {
    // TODO
    return null;
  }

  @Override
  public Stream<Node> walkSubTree()
  {
    return isCollapsed() ? Stream.empty()
      : children.stream().flatMap(n -> Stream.concat(n.walkSubTree(), Stream.of(n)));
  }

  private Stream<Node> walkHiddenSubTree()
  {
    return children.stream().flatMap(n -> n instanceof InnerNode
      ? Stream.concat(((InnerNode)n).walkHiddenSubTree(), Stream.of(n)) : Stream.of(n));
  }
}
