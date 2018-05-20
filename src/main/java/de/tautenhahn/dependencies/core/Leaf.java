package de.tautenhahn.dependencies.core;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


/**
 * Represents leafs in the containment structure which are intended to denote classes.
 * 
 * @author TT
 */
public class Leaf extends Node
{

  private List<Node> predecessors;

  private List<Node> successors;


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
    return predecessors;
  }



  public void setPredecessors(List<Node> predecessors)
  {
    this.predecessors = predecessors;
  }



  @Override
  public List<Node> getSuccessors()
  {
    return successors;
  }



  public void setSuccessors(List<Node> successors)
  {
    this.successors = successors;
  }


  @Override
  List<List<Node>> getDependencyReason(Node other)
  {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  Stream<Node> walkSubTree()
  {
    return Stream.empty();
  }

}
