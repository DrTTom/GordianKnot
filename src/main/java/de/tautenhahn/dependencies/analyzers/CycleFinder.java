package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Applies the algorithm of Tarjan to find components of strong connectivity in a graph.
 *
 * @author TT
 */
public class CycleFinder
{

  private final Graph graph;

  private final int[] index;

  private final int[] lowLink;

  private int maxUsedIndex;

  private final boolean[] isOnStack;

  private final int[] stack;

  private int stackSize;

  private final List<List<Integer>> strongComponents = new ArrayList<>();

  /**
   * Returns the components of strong connectivity sorted by descending size.
   */
  public List<List<Integer>> getStrongComponents()
  {
    return strongComponents;
  }

  /**
   * Returns the subgraph induced by all nodes which are on cycles.
   */
  public Graph returnAllCycles()
  {
    // TODO: define how to join graphs and display only relevant edges.
    List<Integer> retainNodes = strongComponents.stream()
                                                .filter(l -> l.size() > 1)
                                                .flatMap(List::stream)
                                                .collect(Collectors.toList());
    return new Graph(graph, retainNodes);
  }

  /**
   * Creates instance and runs analysis
   *
   * @param graph Graph to analyze.
   */
  public CycleFinder(Graph graph)
  {
    this.graph = graph;
    index = new int[graph.numberNodes()];
    lowLink = new int[index.length];
    isOnStack = new boolean[index.length];
    stack = new int[index.length];

    for ( int node = 0 ; node < index.length ; node++ )
    {
      if (index[node] == 0)
      {
        tarjan(node);
      }
    }
    Collections.sort(strongComponents, (a, b) -> b.size() - a.size());
  }

  private void tarjan(int node)
  {
    index[node] = ++maxUsedIndex;
    lowLink[node] = maxUsedIndex;
    push(node);
    for ( int succ : graph.getSuccessors(node) )
    {
      if (index[succ] == 0)
      {
        tarjan(succ);
        lowLink[node] = Math.min(lowLink[node], lowLink[succ]);
      }
      else if (isOnStack[succ])
      {
        lowLink[node] = Math.min(lowLink[node], lowLink[succ]);
      }
    }
    if (lowLink[node] == index[node])
    {
      List<Integer> component = new ArrayList<>();
      strongComponents.add(component);
      int other = -1;
      do
      {
        other = pop();
        component.add(Integer.valueOf(other));
      }
      while (other != node);
    }
  }

  private void push(int node)
  {
    stack[stackSize++] = node;
    isOnStack[node] = true;
  }

  private int pop()
  {
    int node = stack[--stackSize];
    isOnStack[node] = false;
    return node;
  }
}
