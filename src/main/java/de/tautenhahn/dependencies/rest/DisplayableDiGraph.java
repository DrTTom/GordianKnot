package de.tautenhahn.dependencies.rest;

import java.util.ArrayList;
import java.util.Collections;
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
  static class VisNode
  {

    final String label;

    final String id;

    String group = "package";

    VisNode(String label, String id)
    {
      this.label = label;
      this.id = id;
    }

    /**
     * @return the group
     */
    public String getGroup()
    {
      return group;
    }

    /**
     * @return the label
     */
    public String getLabel()
    {
      return label;
    }

    /**
     * @return the id
     */
    public String getId()
    {
      return id;
    }

  }

  /**
   * Data record for an edge.
   */
  static class VisEdge
  {

    String from;

    String to;

    String arrows = "middle";

    String id;

    VisEdge(String i, String j)
    {
      this.from = i;
      this.to = j;
      id = from + "-" + to;
    }


    /**
     * @return the from
     */
    public String getFrom()
    {
      return from;
    }

    /**
     * @return the to
     */
    public String getTo()
    {
      return to;
    }

    /**
     * @return the arrows
     */
    public String getArrows()
    {
      return arrows;
    }

    /**
     * @return the id
     */
    public String getId()
    {
      return id;
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
      e.group = "class";
    }

    else if (n.getSimpleName().indexOf(':') > 0)
    {
      e.group = n.getSimpleName().substring(0, n.getSimpleName().indexOf(':'));
    }
    nodes.add(e);
  }


  public List<VisNode> getNodes()
  {
    return Collections.unmodifiableList(nodes);
  }

  public List<VisEdge> getEdges()
  {
    return Collections.unmodifiableList(edges);
  }

}
