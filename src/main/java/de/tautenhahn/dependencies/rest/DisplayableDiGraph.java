package de.tautenhahn.dependencies.rest;

import java.util.ArrayList;
import java.util.List;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
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

  /**
   * Data record for a node.
   */
  private static class VisNode
  {

    @SuppressWarnings("unused") // read by GSON
    String label;

    @SuppressWarnings("unused") // read by GSON
    String id;

    @SuppressWarnings("unused") // read by GSON
    String color = "#c0d9fb";

    VisNode(String label, String id)
    {
      this.label = label;
      this.id = id;
    }

  }

  /**
   * Data record for an edge.
   */
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
    String label = n.getSimpleName().replaceAll(".*:", "").//
                    replaceAll("_([a-z]{3})$", ".$1").//
                    replaceAll("(.{8,18}[a-z])([A-Z])", "$1\n$2").//
                    replaceAll("(.{8,18}-)(\\w)", "$1\n$2");

    VisNode e = new VisNode(label, id);
    if (n instanceof ClassNode)
    {
      e.color = "#e9f1fb";
    }

    else if (n.getSimpleName().indexOf(':') > 0)
    {
      e.color = "#97c2fc";
    }
    nodes.add(e);
  }

}
