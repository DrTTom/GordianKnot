package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayList;
import java.util.stream.Collectors;

import de.tautenhahn.dependencies.core.Node;


/**
 * Applies the algorithm of Tarjan to find cycles in the dependency structure.
 *
 * @author TT
 */
public class CycleFinder
{

  // using array for faster access
  private final Node[] allNodes;

  private final int[] index;

  private final int[] lowLink;

  private int maxUsedIndex;

  private final boolean[] isOnStack;

  private final int[] stack;

  private int stackSize;

  /**
   * Creates instance and runs analysis
   *
   * @param root imaginary root node of the containment structure.
   */
  public CycleFinder(Node root)
  {
    ArrayList<Node> nodes = root.walkSubTree().collect(Collectors.toCollection(() -> new ArrayList<>()));
    allNodes = nodes.toArray(new Node[0]);
    for ( int i = 0 ; i < allNodes.length ; i++ )
    {
      allNodes[i].setIndex(i);
    }
    // TODO: add an index to the node class
    index = new int[allNodes.length];
    lowLink = new int[allNodes.length];
    isOnStack = new boolean[allNodes.length];
    stack = new int[allNodes.length];

    for ( int node = 0 ; node < allNodes.length ; node++ )
    {
      if (index[node] == 0)
      {
        tarjan(node);
      }
    }

  }

  private void tarjan(int node)
  {
    index[node] = ++maxUsedIndex;
    lowLink[node] = maxUsedIndex;
    push(node);
    for ( Node succNode : allNodes[node].getSuccessors() )
    {
      int succ = succNode.getIndex();
      System.out.println("   successor " + allNodes[succ]);
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
      System.out.println("\nComponent is:");
      int other = -1;
      do
      {
        other = pop();
        System.out.println(allNodes[other]);
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
