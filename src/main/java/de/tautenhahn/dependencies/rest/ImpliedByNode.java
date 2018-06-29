package de.tautenhahn.dependencies.rest;

import java.util.Objects;
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

  private String nodeDisplayName;

  private final boolean useSuccessors;

  private ImpliedByNode(String nodeName, boolean useSuccessors)
  {
    this.nodeName = nodeName;
    nodeDisplayName = nodeName;
    this.useSuccessors = useSuccessors;
  }

  /**
   * Returns instance which filters only named node and those which rely on it.
   *
   * @param nodeName
   */
  public static ImpliedByNode dependingOn(String nodeName)
  {
    return new ImpliedByNode(nodeName, false);
  }

  /**
   * Returns instance which filters only named node and those needed by named node.
   *
   * @param nodeName
   */
  public static ImpliedByNode requiredBy(String nodeName)
  {
    return new ImpliedByNode(nodeName, true);
  }

  @Override
  public DiGraph apply(DiGraph input)
  {
    IndexedNode start = findNodeByName(input).orElseThrow(() -> new IllegalArgumentException("missing node "
                                                                                             + nodeName));
    nodeDisplayName = start.getNode().getDisplayName();
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
  public int hashCode()
  {
    return ((nodeName == null) ? 0 : nodeName.hashCode()) + (useSuccessors ? 1 : 2);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null || getClass() != obj.getClass())
    {
      return false;
    }
    ImpliedByNode other = (ImpliedByNode)obj;
    return Objects.equals(nodeName, other.nodeName) && useSuccessors == other.useSuccessors;
  }

  @Override
  public String getName()
  {
    return (useSuccessors ? "required by " : "depending on ") + nodeDisplayName;
  }

}
