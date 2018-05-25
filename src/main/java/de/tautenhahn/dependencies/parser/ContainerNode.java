package de.tautenhahn.dependencies.parser;

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
public class ContainerNode extends Node
{

  private final List<Node> children = new ArrayList<>();

  private ContainerNode(Node parent, String name)
  {
    super(parent, name);
  }

  /**
   * Creates a new instance as child of current node.
   *
   * @param name must be relative to current node
   */
  public ContainerNode createInnerChild(String name)
  {
    return createChild(name, ContainerNode::new);
  }

  /**
   * Creates a new instance as child of current node.
   *
   * @param name must be relative to current node
   */
  public ClassNode createLeaf(String name)
  {
    return createChild(name, ClassNode::new);
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
    if (intermed instanceof ClassNode)
    {
      throw new IllegalArgumentException("cannot add a child of " + intermed.getName());
    }
    if (intermed == null)
    {
      intermed = createInnerChild(parts.getFirst());
    }
    return ((ContainerNode)intermed).createChild(parts.getSecond(), constructor);
  }


  /**
   * Creates a virtual root node with no parent and no name.
   */
  public static ContainerNode createRoot()
  {
    return new ContainerNode(null, null);
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
    return getNeighbours(ClassNode::getSucLeafs);
  }

  private List<Node> getNeighbours(Function<ClassNode, List<Node>> lister)
  {
    return getContainedLeafs().flatMap(l -> lister.apply(l).stream())
                              .distinct()
                              .map(this::replaceByCollapsedAnchestor)
                              .distinct()
                              .filter(n -> n != this)
                              .collect(Collectors.toList());
  }

  @Override
  public List<Node> getPredecessors()
  {
    return getNeighbours(ClassNode::getPredLeafs);
  }

  @Override
  public List<Pair<Node, Node>> getDependencyReason(Node other)
  {
    return getContainedLeafs().flatMap(l -> l.getDependencyReason(other).stream())
                              .collect(Collectors.toList());
  }

  @Override
  public Stream<Node> walkSubTree()
  {
    switch (getListMode())
    {
      case COLLAPSED:
        return Stream.empty();
      case LEAFS_COLLAPSED:
        return children.stream()
                       .filter(c -> c instanceof ContainerNode)
                       .flatMap(n -> Stream.concat(n.walkSubTree(), Stream.of(n)));
      case EXPANDED:
        return children.stream().flatMap(n -> Stream.concat(n.walkSubTree(), Stream.of(n)));
      default:
        throw new IllegalStateException("unsupported list mode");
    }
  }

  /**
   * Returns all Leafs currently represented by this node, excluding expanded stuff.
   */
  public Stream<ClassNode> getContainedLeafs()
  {
    switch (getListMode())
    {
      case COLLAPSED:
        return getAllDescendentLeafs();
      case LEAFS_COLLAPSED:
        return children.stream().filter(n -> n instanceof ClassNode).map(l -> (ClassNode)l);
      case EXPANDED:
        return Stream.empty();
      default:
        throw new IllegalStateException("unsupported list mode");
    }
  }

  private Stream<ClassNode> getAllDescendentLeafs()
  {
    return children.stream().flatMap(n -> n instanceof ContainerNode ? ((ContainerNode)n).getAllDescendentLeafs()
      : Stream.of((ClassNode)n));
  }

  @Override
  public boolean hasOwnContent()
  {
    return getContainedLeafs().findAny().isPresent();
  }
}
