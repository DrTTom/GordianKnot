package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
   * For cycle-free graphs, return the ranks of each node. <br>
   * For cyclic graphs, this method will just ignore some of the arcs, so the outcome is undefined. However,
   * removing some arcs which belong to cycles, there is a subgraph where each vertex has the rank as
   * indicated. For organizing some graphic output, that may be just good enough.
   *
   * @param graph
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
   * For cycle-free graphs, returns the nodes in such an order that all arcs go forward in the sequence. This
   * method can be applied for cyclic graphs where it just ignores some of the arcs. The outcome in that case
   * is undefined but should be good enough if we just want a large number of arcs pointing in the right
   * direction.
   *
   * @param graph
   */
  public static List<IndexedNode> topSort(DiGraph graph)
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
                                          .sorted((a, b) -> a.getPredecessors().size()
                                                            - b.getPredecessors().size())
                                          .findFirst();
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

    boolean[] listed;

    boolean[] seen;

    Deque<IndexedNode> result = new ArrayDeque<>();

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
