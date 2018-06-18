package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayList;
import java.util.List;

import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;


/**
 * Applies the algorithm of Tarjan to find components of strong connectivity in a graph.
 *
 * @author TT
 */
public class CycleFinder
{

  private final int[] foundInStep;

  private final int[] lowLink;

  private int maxUsedIndex;

  private final boolean[] isOnStack;

  private final IndexedNode[] stack;

  private int stackSize;

  private final List<List<IndexedNode>> strongComponents = new ArrayList<>();

  /**
   * Returns the components of strong connectivity sorted by descending size.
   */
  public List<List<IndexedNode>> getStrongComponents()
  {
    return strongComponents;
  }

  /**
   * Returns the subgraph induced by all nodes which are on cycles.
   */
  public DiGraph createGraphFromCycles()
  {
    return new DiGraph(strongComponents.stream()
                                       .filter(l -> l.size() > 1)
                                       .map(DiGraph::new)
                                       .toArray(DiGraph[]::new));
  }

  /**
   * Creates instance and runs analysis
   *
   * @param graph Graph to analyze.
   */
  public CycleFinder(DiGraph graph)
  {
    foundInStep = new int[graph.getAllNodes().size()];
    lowLink = new int[foundInStep.length];
    isOnStack = new boolean[foundInStep.length];
    stack = new IndexedNode[foundInStep.length];

    for ( IndexedNode node : graph.getAllNodes() )
    {
      if (foundInStep[node.getIndex()] == 0)
      {
        tarjan(node);
      }
    }
    strongComponents.sort((a, b) -> b.size() - a.size());
  }

  private void tarjan(IndexedNode inode)
  {
    int node = inode.getIndex();
    foundInStep[node] = ++maxUsedIndex;
    lowLink[node] = maxUsedIndex;
    push(inode);
    for ( IndexedNode succN : inode.getSuccessors() )
    {
      int succ = succN.getIndex();
      if (foundInStep[succ] == 0)
      {
        tarjan(succN);
        lowLink[node] = Math.min(lowLink[node], lowLink[succ]);
      }
      else if (isOnStack[succ])
      {
        lowLink[node] = Math.min(lowLink[node], lowLink[succ]);
      }
    }
    if (lowLink[node] == foundInStep[node])
    {
      List<IndexedNode> component = new ArrayList<>();
      strongComponents.add(component);
      IndexedNode other = null;
      do
      {
        other = pop();
        component.add(other);
      }
      while (other != inode); // NOPMD we really mean the same object
    }
  }

  private void push(IndexedNode node)
  {
    stack[stackSize++] = node;
    isOnStack[node.getIndex()] = true;
  }

  private IndexedNode pop()
  {
    IndexedNode node = stack[--stackSize];
    isOnStack[node.getIndex()] = false;
    return node;
  }
}
