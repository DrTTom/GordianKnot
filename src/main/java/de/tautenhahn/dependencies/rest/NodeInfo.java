package de.tautenhahn.dependencies.rest;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;


/**
 * Information to display about a node.
 *
 * @author TT
 */
public class NodeInfo
{

  private final String name;

  private final String type;

  private final String numberContainedClasses;

  private final String listMode;

  /**
   * Creates immutable instance
   * 
   * @param graph
   * @param number
   */
  public NodeInfo(DiGraph graph, int number)
  {
    IndexedNode node = graph.getAllNodes().get(number);
    name = node.getNode().getName();
    numberContainedClasses = Integer.toString(node.getNumberClasses());
    type = getType(node.getNode());
    listMode = node.getNode().getListMode().toString();
  }

  private String getType(Node node)
  {
    if (node instanceof ClassNode)
    {
      return "class";
    }
    String simpleName = node.getSimpleName();
    int pos = simpleName.indexOf(':');
    if (pos == -1)
    {
      return node.getListMode() == ListMode.COLLAPSED ? "package tree" : "package";
    }
    return simpleName.substring(0, pos);
  }
}
