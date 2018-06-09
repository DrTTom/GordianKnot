package de.tautenhahn.dependencies.analyzers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;


/**
 * Provides generic manipulations and functions handling a graph. Node that induced subgraphs are provided by
 * the {@link DiGraph} class itself because a special constructor is required.
 *
 * @author TT
 */
public final class BasicGraphOperations
{

  public BasicGraphOperations()
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

  private static void count(IndexedNode n, int... numberNodesAndArcs)
  {
    numberNodesAndArcs[0]++;
    numberNodesAndArcs[1] += n.getSuccessors().size();
  }

  /**
   * Returns the transitive closure of a given graph using repeated algorithm of Floyd/Warshall. In case of
   * performance problems, implement Purdom's algorithm instead!
   *
   * @param graph
   */
  public static DiGraph transitiveClosure(DiGraph graph)
  {
    return new DiGraph(graph); // TODO!
  }

  /**
   * Returns nodes in breadth-first order. This implementation is much less elegant than several
   * implementations from the net but contrary to those it is correct. Furthermore, it creates less streams
   * and collections. Note that you cannot make the stream parallel without loosing the strict breadth-first
   * sequence. Use the cheaper depth-first search if you do not care about the sequence.
   * 
   * @param graph
   * @param start
   * @param forward
   */
  public static Stream<IndexedNode> breadthFirstSearch(DiGraph graph, IndexedNode start, boolean forward)
  {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new BfsIterator(graph, start, forward),
                                                                    0),
                                false);
  }

  private static class BfsIterator implements Iterator<IndexedNode>
  {

    private final Queue<IndexedNode> foundNodes = new LinkedList<>();

    private final boolean[] found;

    private final boolean forward;

    BfsIterator(DiGraph graph, IndexedNode start, boolean forward)
    {
      foundNodes.add(start);
      found = new boolean[graph.getAllNodes().size()];
      found[start.getIndex()] = true;
      this.forward = forward;
    }

    @Override
    public boolean hasNext()
    {
      return !foundNodes.isEmpty();
    }

    @Override
    public IndexedNode next()
    {
      IndexedNode s = foundNodes.poll();
      (forward ? s.getSuccessors() : s.getPredecessors()).stream()
                                                         .filter(n -> !found[n.getIndex()])
                                                         .forEach(n -> {
                                                           found[n.getIndex()] = true;
                                                           foundNodes.add(n);
                                                         });
      System.out.println(s.getNode());
      return s;
    }
  }

}
