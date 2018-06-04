package de.tautenhahn.dependencies.analyzers;

import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;


/**
 * Provides generic manipulations and functions handling a graph. Node that induced subgraphs are provided by
 * the {@link DiGraph} class itself because a special constructor is required.
 *
 * @author TT
 */
public final class BasicGraphOperations
{

  private BasicGraphOperations()
  {
    // no instances needed
  }

  /**
   * Returns the classical edge density of dependency graph, respecting collapsed nodes.
   *
   * @param graph
   */
  public static double getDensity(DiGraph graph)
  {
    int[] numberNodesAndArcs = new int[2];
    graph.getAllNodes().forEach(n -> count(n, numberNodesAndArcs));
    int numberNodes = numberNodesAndArcs[0];
    return 1.0 * numberNodesAndArcs[1] / (numberNodes * (numberNodes - 1));
  }

  private static void count(IndexedNode n, int[] numberNodesAndArcs)
  {
    numberNodesAndArcs[0]++;
    numberNodesAndArcs[1] += n.getSuccessors().size();
  }

  /**
   * Returns the transitive closure of a given graph.
   *
   * @param graph
   */
  public static DiGraph transitiveClosure(DiGraph graph)
  {
    DiGraph result = new DiGraph(graph);

    // TODO
    return result;

  }
}
