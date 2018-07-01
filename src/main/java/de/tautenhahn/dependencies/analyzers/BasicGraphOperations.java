package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
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
   * For cycle-free graphs, return the ranks of each node. <br>
   * For cyclic graphs, this method will just ignore some of the arcs, so the outcome is undefined. However,
   * removing some arcs which belong to cycles, there is a subgraph where each vertex has the rank as
   * indicated. For organizing some graphic output, that may be just good enough.
   * 
   * @param graph
   * @return
   */
  public static int[] getRanks(DiGraph graph)
  {
    if (graph.getAllNodes().isEmpty())
    {
      return new int[0];
    }
    int minInValence = graph.getAllNodes()
                            .stream()
                            .mapToInt(n -> n.getPredecessors().size())
                            .min()
                            .getAsInt();
    Set<IndexedNode> sources = graph.getAllNodes()
                                    .stream()
                                    .filter(n -> n.getPredecessors().size() == minInValence)
                                    .collect(Collectors.toSet());
    int[] result = new int[graph.getAllNodes().size()];
    breadthFirstSearch(graph, true, sources.toArray(new IndexedNode[0])).forEach(n -> {
      if (!sources.contains(n))
      {
        result[n.getIndex()] = n.getPredecessors()
                                .stream()
                                .mapToInt(p -> result[p.getIndex()])
                                .max()
                                .getAsInt()
                               + 1;
      }
    });
    return result;
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
   * @param forward
   * @param start several nodes my be given where the graph theorist would introduce an artificial source.
   */
  public static Stream<IndexedNode> breadthFirstSearch(DiGraph graph, boolean forward, IndexedNode... start)
  {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new BfsIterator(graph, forward, start),
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
