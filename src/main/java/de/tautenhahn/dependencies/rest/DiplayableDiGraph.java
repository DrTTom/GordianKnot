package de.tautenhahn.dependencies.rest;

import java.util.ArrayList;
import java.util.List;

import de.tautenhahn.dependencies.analyzers.Graph;
import de.tautenhahn.dependencies.parser.Node;


/**
 * Represents the dependency graph in a format compatible to the visualizing java script library.
 *
 * @author TT
 */
public class DiplayableDiGraph
{

  private final List<VisNode> nodes = new ArrayList<>();

  private final List<VisEdge> edges = new ArrayList<>();

  private static class VisNode
  {

    String label;

    String id;

    VisNode(String label, String id)
    {
      this.label = label;
      this.id = id;
    }

  }

  private static class VisEdge
  {

    String from;

    String to;

    String arrows = "middle";

    VisEdge(String i, String j)
    {
      this.from = i;
      this.to = j;
    }

  }

  /**
   * Creates immutable instance filled with given information.
   *
   * @param root
   */
  public DiplayableDiGraph(Graph graph)
  {
    for ( int i = 0 ; i < graph.numberNodes() ; i++ )
    {
      addNode(graph.getNode(i), Integer.toString(i));
      for ( int j : graph.getSuccessors(i) )
      {
        edges.add(new VisEdge(Integer.toString(i), Integer.toString(j)));
      }
    }
  }

  private void addNode(Node n, String id)
  {
    nodes.add(new VisNode(n.getSimpleName(), id));
  }

}
