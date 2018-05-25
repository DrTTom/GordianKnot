package de.tautenhahn.dependencies.analyzers;

import de.tautenhahn.dependencies.core.Node;


/**
 * Measures the level of dependency between the nodes.
 *
 * @author TT
 */
public class EdgeDensity
{

  /**
   * Returns the classical edge density of dependency graph, respecting collapsed nodes.
   *
   * @param root
   */
  public double getDensity(Node root)
  {
    int[] numberNodesAndArcs = new int[2];
    root.walkSubTree().forEach(n -> count(n, numberNodesAndArcs));
    int numberNodes = numberNodesAndArcs[0];
    return 1.0 * numberNodesAndArcs[1] / (numberNodes * (numberNodes - 1));
  }

  /**
   * Returns the edge density of the transitive closure of the dependency graph. This measure can distinguish
   * between differently complicated graphs with same number of nodes and edges.
   *
   * @param n
   * @param numberNodesAndArcs
   */
  public double getTransitiveDensity(Node root)
  {
    // TODO!
    int[] numberNodesAndArcs = new int[2];
    root.walkSubTree().forEach(n -> count(n, numberNodesAndArcs));
    int numberNodes = numberNodesAndArcs[0];
    return 1.0 * numberNodesAndArcs[1] / (numberNodes * (numberNodes - 1));
  }

  private void count(Node n, int[] numberNodesAndArcs)
  {
    numberNodesAndArcs[0]++;
    numberNodesAndArcs[1] += n.getSuccessors().size();
  }

}
