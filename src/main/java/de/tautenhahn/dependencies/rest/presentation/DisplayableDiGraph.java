package de.tautenhahn.dependencies.rest.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tautenhahn.dependencies.analyzers.BasicGraphOperations;
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
  public static class VisNode
  {

    private final String label;

    private final String id;

    private final int level;

    String group = "package";

    VisNode(String label, String id, int level)
    {
      this.label = label;
      this.id = id;
      this.level = level;
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

    public int getLevel()
    {
      return level;
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
    int[] rank = BasicGraphOperations.getRanks(graph);
    for ( IndexedNode node : graph.getAllNodes() )
    {
      addNode(node.getNode(), Integer.toString(node.getIndex()), rank[node.getIndex()]);
      for ( IndexedNode j : node.getSuccessors() )
      {
        edges.add(new VisEdge(Integer.toString(node.getIndex()), Integer.toString(j.getIndex())));
      }
    }
  }

  private void addNode(Node n, String id, int rank)
  {
    String label = n.getSimpleName().replaceAll(".*:", "").//
                    replaceAll("_([a-z]{3})$", ".$1").//
                    replaceAll("(.{8,18}[a-z])([A-Z])", "$1\n$2").//
                    replaceAll("(.{8,18}-)(\\w)", "$1\n$2");

    VisNode e = new VisNode(label, id, rank);
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

  /**
   * Returns the list of nodes.
   */
  public List<VisNode> getNodes()
  {
    return Collections.unmodifiableList(nodes);
  }

  /**
   * Returns the list of edges.
   */
  public List<VisEdge> getEdges()
  {
    return Collections.unmodifiableList(edges);
  }
}
