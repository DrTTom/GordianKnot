package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.Pair;


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
   * @param graph graph to analyze
   * @return ratio of number existing arcs to number possible arcs
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
   * For cycle-free graphs, return the ranks of each node. <br>
   * For cyclic graphs, this method will just ignore some of the arcs, so the outcome is undefined. However,
   * removing some arcs which belong to cycles, there is a subgraph where each vertex has the rank as
   * indicated. For organizing some graphic output, that may be just good enough.
   *
   * @param graph structure to analyze
   * @return for each node, length of a longest path from a source to that node
   */
  public static int[] getRanks(DiGraph graph)
  {
    List<IndexedNode> sorted = topSort(graph);
    int[] result = new int[sorted.size()];
    sorted.forEach(n -> result[n.getIndex()] = n.getPredecessors()
                                                .stream()
                                                .mapToInt(p -> result[p.getIndex()])
                                                .max()
                                                .orElse(0)
                                               + 1);
    return result;
  }

  /**
   * Returns the number of nodes each node depends on (inclusive itself) and used from.
   *
   * @param graph any directed graph, transitive closure is computed internally.
   * @return all that counts
   */
  public static Pair<int[], int[]> countDependsOnAndUsedFrom(DiGraph graph)
  {
    DiGraph transitive = transitiveClosure(graph);
    List<IndexedNode> nodes = transitive.getAllNodes();
    int[] dependsOn = new int[nodes.size()];
    int[] usedFrom = new int[nodes.size()];
    for ( IndexedNode node : nodes )
    {
      int self = node.getSuccessors().contains(node) ? 0 : 1;
      dependsOn[node.getIndex()] = self + node.getSuccessors().size();
      usedFrom[node.getIndex()] = self + node.getPredecessors().size();
    }
    return new Pair<>(dependsOn, usedFrom);
  }

  /**
   * @return the cumulative dependsOn value.
   * @param numbers return value of {@link #countDependsOnAndUsedFrom(DiGraph)}
   */
  public static int ccd(Pair<int[], int[]> numbers)
  {
    return Arrays.stream(numbers.getFirst()).sum();
  }

  /**
   * @return the average dependsOn value.
   * @param numbers return value of {@link #countDependsOnAndUsedFrom(DiGraph)}
   */
  public static double acd(Pair<int[], int[]> numbers)
  {
    return 1.0 * ccd(numbers) / numbers.getFirst().length;
  }

  /**
   * Returns the ratio between ccd of current graph and ccd of a balanced binary tree of same size. That value
   * should be comparable between graphs of different node number.
   *
   * @param numbers return value of {@link #countDependsOnAndUsedFrom(DiGraph)}
   * @return that ratio
   */
  public static double rcd(Pair<int[], int[]> numbers)
  {
    return 1.0 * ccd(numbers) / getTreeValue(numbers.getFirst().length);
  }

  private static int getTreeValue(int n)
  {
    if (n <= 1)
    {
      return n;
    }
    if (n % 2 == 0)
    {
      return getTreeValue(n / 2 - 1) + getTreeValue(n / 2) + n;
    }
    return 2 * getTreeValue((n - 1) / 2) + n;
  }

  /**
   * For cycle-free graphs, returns the nodes in such an order that all arcs go forward in the sequence. This
   * method can be applied for cyclic graphs where it just ignores some of the arcs. The outcome in that case
   * is undefined but should be good enough if we just want a large number of arcs pointing in the right
   * direction.
   *
   * @param graph structure to order
   * @return ordered node list
   */
  private static List<IndexedNode> topSort(DiGraph graph)
  {
    if (graph.getAllNodes().isEmpty())
    {
      return Collections.emptyList();
    }
    TopSortWrapper wrapper = new TopSortWrapper(graph);
    while (true)
    {
      Optional<IndexedNode> source = graph.getAllNodes()
                                          .stream()
                                          .filter(n -> !wrapper.listed[n.getIndex()])
                                          .min((a, b) -> a.getPredecessors().size()
                                                         - b.getPredecessors().size());
      if (!source.isPresent())
      {
        break;
      }
      wrapper.visit(source.get());
    }
    return new ArrayList<>(wrapper.result);
  }

  /**
   * Avoids putting some arrays onto the stack several times.
   */
  private static class TopSortWrapper
  {

    final boolean[] listed;

    final boolean[] seen;

    final Deque<IndexedNode> result = new ArrayDeque<>();

    TopSortWrapper(DiGraph graph)
    {
      listed = new boolean[graph.getAllNodes().size()];
      seen = new boolean[listed.length];
    }

    void visit(IndexedNode n)
    {
      if (!listed[n.getIndex()])
      {
        // if seen there is a cycle;
        seen[n.getIndex()] = true;
        n.getSuccessors().stream().filter(s -> !seen[s.getIndex()]).forEach(this::visit);
        listed[n.getIndex()] = true;
        result.addFirst(n);
      }
    }
  }

  /**
   * Returns the transitive closure of a given graph using repeated algorithm of Floyd/Warshall. In case of
   * performance problems, implement Purdom's algorithm instead! May be its OK to work directly on the lists.
   *
   * @param graph any graph
   * @return transitive closure
   */
  public static DiGraph transitiveClosure(DiGraph graph)
  {
    int n = graph.getAllNodes().size();
    boolean[][] adjacency = new boolean[n][n];
    for ( int l = 0 ; l < n ; l++ )
    {
      for ( IndexedNode node : graph.getAllNodes() )
      {
        for ( IndexedNode succ : node.getSuccessors() )
        {
          adjacency[node.getIndex()][succ.getIndex()] = true;
        }
      }
    }
    for ( int k = 0 ; k < n ; k++ )
    {
      for ( int i = 0 ; i < n ; i++ )
      {
        for ( int j = 0 ; j < n ; j++ )
        {
          adjacency[i][j] |= adjacency[i][k] && adjacency[k][j];
        }
      }
    }
    DiGraph result = new DiGraph(graph);
    for ( int i = 0 ; i < n ; i++ )
    {
      for ( int j = 0 ; j < n ; j++ )
      {
        if (adjacency[i][j])
        {
          result.addArc(result.getAllNodes().get(i), result.getAllNodes().get(j));
        }
      }
    }
    return result;
  }

  /**
   * Returns nodes in breadth-first order. This implementation is much less elegant than several
   * implementations from the net but contrary to those it is correct. Furthermore, it creates less streams
   * and collections. Note that you cannot make the stream parallel without loosing the strict breadth-first
   * sequence. Use the cheaper depth-first search if you do not care about the sequence.
   *
   * @param graph any graph
   * @param forward true to follow arc directions
   * @param start several nodes may be given where the graph theorist would introduce an artificial source.
   * @return sorted nodes
   */
  public static Stream<IndexedNode> breadthFirstSearch(DiGraph graph, boolean forward, IndexedNode... start)
  {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new BfsIterator(graph, forward, start),
                                                                    0),
                                false);
  }

  /**
   * @return nodes in depth-first order. If you do not care about the order, feel free to use the stream in
   *         parallel.
   */
  public static Stream<IndexedNode> depthFirstSearch(DiGraph graph, IndexedNode start)
  {
    return new DfsWrapper(graph).search(start);
  }


  /**
   * Just avoiding the same references several times on the stack.
   */
  private static class DfsWrapper
  {

    private final boolean[] found;

    DfsWrapper(DiGraph graph)
    {
      found = new boolean[graph.getAllNodes().size()];
    }

    Stream<IndexedNode> search(IndexedNode start)
    {
      found[start.getIndex()] = true;
      return Stream.concat(Stream.of(start),
                           start.getSuccessors()
                                .stream()
                                .filter(n -> !found[n.getIndex()])
                                .flatMap(this::search));
    }
  }

  /**
   * Need a special iterator because we have to stream a collection which changes as the stream is read.
   */
  private static class BfsIterator implements Iterator<IndexedNode>
  {

    private final Queue<IndexedNode> foundNodes = new ArrayDeque<>();

    private final boolean[] found;

    private final boolean forward;

    BfsIterator(DiGraph graph, boolean forward, IndexedNode... start)
    {
      found = new boolean[graph.getAllNodes().size()];
      for ( IndexedNode s : start )
      {
        foundNodes.add(s);
        found[s.getIndex()] = true;
      }
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
      if (foundNodes.isEmpty())
      {
        throw new NoSuchElementException("queue is empty");
      }
      IndexedNode s = foundNodes.poll();
      (forward ? s.getSuccessors() : s.getPredecessors()).stream()
                                                         .filter(n -> !found[n.getIndex()])
                                                         .forEach(n -> {
                                                           found[n.getIndex()] = true;
                                                           foundNodes.add(n);
                                                         });
      return s;
    }
  }

}
