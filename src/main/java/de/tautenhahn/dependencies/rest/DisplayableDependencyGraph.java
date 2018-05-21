package de.tautenhahn.dependencies.rest;

import java.util.List;

import de.tautenhahn.dependencies.core.Node;


/**
 * Represents the dependency graph in a format compatible to the visualizing java script library.
 * 
 * @author TT
 */
public class DisplayableDependencyGraph
{

  private List<VisNode> nodes;

  private List<VisEdge> edges;

  private int idSource;

  private static class VisNode
  {

    String label;

    String id;
  }

  private static class VisEdge
  {

    String from;

    String to;
  }

  /**
   * Creates immutable instance filled with given information.
   * 
   * @param root
   */
  public DisplayableDependencyGraph(Node root)
  {
    root.walkSubTree().forEach(this::addNode);
  }

  private void addNode(Node n)
  {
    // nodes.add(new VisNode(Integer.toString(idSource++)))
  }
}
