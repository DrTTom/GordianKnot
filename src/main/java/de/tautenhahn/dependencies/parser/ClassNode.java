package de.tautenhahn.dependencies.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Represents leafs in the containment structure which are intended to denote classes.
 *
 * @author TT
 */
public class ClassNode extends Node
{

  private final List<ClassNode> predLeafs = new ArrayList<>();

  private final List<ClassNode> sucLeafs = new ArrayList<>();

  private final Collection<String> missingDependencies = new ArrayList<>();

  private final String className;

  /**
   * Creates instance.
   *
   * @param name simple name
   * @param parent
   */
  ClassNode(Node parent, String name)
  {
    super(parent, name);
    className = getName().replaceAll("[^\\.]+:[^\\.]+\\.", "");
  }

  @Override
  public List<Node> getPredecessors()
  {
    return predLeafs.stream().map(Node::getListedContainer).distinct().collect(Collectors.toList());
  }

  @Override
  public List<Node> getSuccessors()
  {
    return sucLeafs.stream().map(Node::getListedContainer).distinct().collect(Collectors.toList());
  }

  /**
   * Adds a successor, namely a node for a class own class depends on.
   *
   * @param successor
   */
  public void addSuccessor(ClassNode successor)
  {
    sucLeafs.add(successor);
    successor.predLeafs.add(this);
  }

  @SuppressWarnings("unused")
  @Override
  public List<Pair<Node, Node>> getDependencyReason(Node other)
  {
    return other instanceof ClassNode ? Collections.singletonList(new Pair<>(this, other))
      : ((ContainerNode)other).getContainedLeafs()
                              .filter(sucLeafs::contains)
                              .map(l -> new Pair<Node, Node>(this, l))
                              .collect(Collectors.toList());
  }

  @Override
  public Stream<Node> walkSubTree()
  {
    return Stream.empty();
  }

  List<ClassNode> getPredLeafs()
  {
    return predLeafs;
  }

  /**
   * Returns the direct successors, ignoring any collapsed containers.
   */
  public List<ClassNode> getSucLeafs()
  {
    return Collections.unmodifiableList(sucLeafs);
  }

  @Override
  public boolean hasOwnContent()
  {
    return true;
  }

  /**
   * Returns all class names which the represented class references but which to not match a successor node.
   */
  public Collection<String> getMissingDependencies()
  {
    return missingDependencies;
  }

  @Override
  Node getChildByName(String simpleName)
  {
    return null;
  }

  /**
   * Returns the name of the represented class.
   */
  public String getClassName()
  {
    return className;
  }

}
