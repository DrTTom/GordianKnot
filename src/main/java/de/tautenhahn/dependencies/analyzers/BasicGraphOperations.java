package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
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

  /**
   * Returns nodes in depth-first order. If you do not care about the order, feel free to use the stream in
   * parallel.
   */
  public static Stream<IndexedNode> depthFirstSearch(DiGraph graph, IndexedNode start)
  {
    return new DfsWrapper(graph).search(start);
  }

  /**
   * Just avoiding the same references several times on the stack.
   *
   * @author jean
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
      IndexedNode s = Objects.requireNonNull(foundNodes.poll(), "queue is empty");
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
