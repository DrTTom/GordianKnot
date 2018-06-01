package de.tautenhahn.dependencies.rest;

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
    String[] nodeNumbers = arcId.split("-");
    int fromNumber = Integer.parseInt(nodeNumbers[0]);
    int toNumber = Integer.parseInt(nodeNumbers[1]);
    from = new NodeInfo(graph, fromNumber);
    to = new NodeInfo(graph, toNumber);
    reason = graph.getAllNodes()
                  .get(fromNumber)
                  .getNode()
                  .explainDependencyTo(graph.getAllNodes().get(toNumber).getNode());
  }

}
