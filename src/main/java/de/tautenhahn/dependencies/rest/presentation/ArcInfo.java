package de.tautenhahn.dependencies.rest.presentation;

import java.util.Collections;
import java.util.List;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.parser.Pair;


/**
 * Information about an arc to display to the user. TT
 */
public class ArcInfo
{

  private final NodeInfo from;

  private final NodeInfo to;

  private final List<Pair<String, String>> reason;

  /**
   * Creates immutable instance.
   *
   * @param graph
   * @param arcId
   */
  public ArcInfo(DiGraph graph, String arcId)
  {
    String[] nodeNumbers = arcId.split("-", -1);
    int fromNumber = Integer.parseInt(nodeNumbers[0]);
    int toNumber = Integer.parseInt(nodeNumbers[1]);
    from = new NodeInfo(graph, fromNumber);
    to = new NodeInfo(graph, toNumber);
    reason = graph.getAllNodes()
                  .get(fromNumber)
                  .getNode()
                  .explainDependencyTo(graph.getAllNodes().get(toNumber).getNode());
  }

  /**
   * Returns arcs start node.
   */
  public NodeInfo getFrom()
  {
    return from;
  }

  /**
   * Returns arcs end node.
   */
  public NodeInfo getTo()
  {
    return to;
  }

  /**
   * Returns pairs of relative class names causing this dependency arc.
   */
  public List<Pair<String, String>> getReason()
  {
    return Collections.unmodifiableList(reason);
  }

}
