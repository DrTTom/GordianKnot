package de.tautenhahn.dependencies.rest;

import java.util.ArrayList;
import java.util.List;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.Node;


/**
 * Represents the dependency graph in a format compatible to the visualizing java script library.
 *
 * @author TT
 */
public class DisplayableDiGraph
{

  private final List<VisNode> nodes = new ArrayList<>();

  private final List<VisEdge> edges = new ArrayList<>();

  private static class VisNode
  {

    @SuppressWarnings("unused") // read by GSON
    String label;

    @SuppressWarnings("unused") // read by GSON
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

    @SuppressWarnings("unused") // read by GSON
    String arrows = "middle";

    @SuppressWarnings("unused") // read by GSON
    String id;

    VisEdge(String i, String j)
    {
      this.from = i;
      this.to = j;
      id = from + "-" + to;
    }

  }

  /**
   * Creates immutable instance filled with given information.
   *
   * @param graph
   */
  public DisplayableDiGraph(DiGraph graph)
  {
    graph.renumber();
    for ( IndexedNode node : graph.getAllNodes() )
    {
      addNode(node.getNode(), Integer.toString(node.getIndex()));
      for ( IndexedNode j : node.getSuccessors() )
      {
        edges.add(new VisEdge(Integer.toString(node.getIndex()), Integer.toString(j.getIndex())));
      }
    }
  }

  private void addNode(Node n, String id)
  {
    nodes.add(new VisNode(n.getSimpleName(), id));
  }

}
