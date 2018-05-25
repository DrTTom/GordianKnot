package de.tautenhahn.dependencies.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Represents leafs in the containment structure which are intended to denote classes.
 *
 * @author TT
 */
public class Leaf extends Node
{

  private final List<Node> predLeafs = new ArrayList<>();

  private final List<Node> sucLeafs = new ArrayList<>();

  /**
   * Creates instance.
   *
   * @param name simple name
   * @param parent
   */
  Leaf(Node parent, String name)
  {
    super(parent, name);
  }

  @Override
  public List<Node> getChildren()
  {
    return Collections.emptyList();
  }

  @Override
  public List<Node> getPredecessors()
  {
    return predLeafs.stream().map(this::replaceByCollapsedAnchestor).distinct().collect(Collectors.toList());
  }

  @Override
  public List<Node> getSuccessors()
  {
    return sucLeafs.stream().map(this::replaceByCollapsedAnchestor).distinct().collect(Collectors.toList());
  }

  public void addSuccessor(Leaf successor)
  {
    sucLeafs.add(successor);
    successor.predLeafs.add(this);
  }

  @SuppressWarnings("unused")
  @Override
  public List<Pair<Node, Node>> getDependencyReason(Node other)
  {
    return other instanceof Leaf ? Collections.singletonList(new Pair<>(this, other))
      : ((InnerNode)other).getContainedLeafs()
                          .filter(sucLeafs::contains)
                          .map(l -> new Pair<Node, Node>(this, l))
                          .collect(Collectors.toList());
  }

  @Override
  public Stream<Node> walkSubTree()
  {
    return Stream.empty();
  }

  List<Node> getPredLeafs()
  {
    return predLeafs;
  }

  List<Node> getSucLeafs()
  {
    return sucLeafs;
  }

  @Override
  public boolean hasOwnContent()
  {
    return true;
  }
}
