package de.tautenhahn.dependencies.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.tautenhahn.dependencies.core.Leaf;
import de.tautenhahn.dependencies.core.Node;
import de.tautenhahn.dependencies.core.Node.ListMode;


/**
 * Represents the dependency graph in a format compatible to the visualizing java script library.
 *
 * @author TT
 */
public class DisplayableDependencyGraph
{

  private final List<VisNode> nodes = new ArrayList<>();

  private final List<VisEdge> edges = new ArrayList<>();

  private int idSource;

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

    VisEdge(String from, String to)
    {
      this.from = from;
      this.to = to;
    }

  }

  /**
   * Creates immutable instance filled with given information.
   *
   * @param root
   */
  public DisplayableDependencyGraph(Node root)
  {
    try
    {
      // TODO: configure filters like this example:
      Map<Node, String> ids = new HashMap<>();
      List<Node> allNodes = root.walkSubTree().collect(Collectors.toList());
      allNodes.forEach(n -> n.setListMode(ListMode.LEAFS_COLLAPSED));

      root.walkSubTree()
          .filter(n -> n.getChildren().stream().anyMatch(c -> c instanceof Leaf))
          .forEach(n -> createId(n, ids));
      ids.forEach(this::addNode);
      ids.forEach((n, i) -> addEdges(n, i, ids));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void createId(Node n, Map<Node, String> ids)
  {
    ids.put(n, Integer.toString(idSource++));
  }

  private void addNode(Node n, String id)
  {
    nodes.add(new VisNode(n.getSimpleName(), id));
  }

  private void addEdges(Node n, String from, Map<Node, String> ids)
  {
    n.getSuccessors()
     .stream()
     .map(ids::get)
     .filter(Objects::nonNull)
     .forEach(to -> edges.add(new VisEdge(from, to)));
  }
}
