package de.tautenhahn.dependencies.rest;

import java.util.Optional;

import de.tautenhahn.dependencies.analyzers.BasicGraphOperations;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;


/**
 * Filters for all those nodes which require or are required by some selected node.
 *
 * @author TT
 */
public final class ImpliedByNode implements ViewFilter
{

  private final String nodeName;

  private final boolean useSuccessors;

  private ImpliedByNode(String nodeName, boolean useSuccessors)
  {
    this.nodeName = nodeName;
    this.useSuccessors = useSuccessors;
  }

  /**
   * Returns instance which filters only named node and those which rely on it.
   *
   * @param name
   */
  public static ImpliedByNode dependingOn(String name)
  {
    return new ImpliedByNode(name, false);
  }

  /**
   * Returns instance which filters only named node and those needed by named node.
   *
   * @param name
   */
  public static ImpliedByNode requiredBy(String name)
  {
    return new ImpliedByNode(name, true);
  }

  @Override
  public DiGraph apply(DiGraph input)
  {
    IndexedNode start = findNodeByName(input).orElseThrow(() -> new IllegalArgumentException("missing node "
                                                                                             + nodeName));
    return new DiGraph(BasicGraphOperations.breadthFirstSearch(input, start, useSuccessors)
                                           .map(IndexedNode::getNode));
  }

  private Optional<IndexedNode> findNodeByName(DiGraph input)
  {
    return input.getAllNodes().stream().filter(n -> nodeName.equals(n.getNode().getName())).findAny();
  }

  @Override
  public boolean isApplicable(DiGraph graph)
  {
    return findNodeByName(graph).isPresent();
  }

  @Override
  public String getName()
  {
    return useSuccessors ? "required by " : "depending on " + nodeName;
  }

}
