package de.tautenhahn.dependencies.core.analyzers;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.BasicGraphOperations;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;


/**
 * Unit test for basic functions on a graph.
 *
 * @author TT
 */
public class TestBasicGraphOperations
{

  private DiGraph graph;

  private IndexedNode a;

  /**
   * Creates some graph to play with.
   */
  @Before
  public void createGraph()
  {
    ContainerNode root = ContainerNode.createRoot();
    for ( char label = 'a' ; label < 'e' ; label++ )
    {
      root.createLeaf(new String(new char[]{label}));
    }
    ((ClassNode)root.find("a")).addSuccessor((ClassNode)root.find("b"));
    ((ClassNode)root.find("a")).addSuccessor((ClassNode)root.find("d"));
    ((ClassNode)root.find("b")).addSuccessor((ClassNode)root.find("c"));
    ((ClassNode)root.find("c")).addSuccessor((ClassNode)root.find("d"));
    ((ClassNode)root.find("d")).addSuccessor((ClassNode)root.find("b"));
    ((ClassNode)root.find("d")).addSuccessor((ClassNode)root.find("a"));

    graph = new DiGraph(root);
    a = graph.getAllNodes().stream().filter(n -> n.getNode().getName().equals("a")).findFirst().orElse(null);
  }

  /**
   * Asserts edge density is computed correctly.
   */
  @SuppressWarnings("boxing")
  @Test
  public void density()
  {
    assertThat("density", BasicGraphOperations.getDensity(graph), closeTo(0.5d, 0.001d));
  }

  /**
   * Performs a breadth first search and checks the correct sequence.
   */
  @Test
  public void breadthFirstSearch()
  {
    assertThat("bfs sequence",
               BasicGraphOperations.breadthFirstSearch(graph, true, a)
                                   .map(n -> n.getNode().getName())
                                   .collect(Collectors.toList()),
               contains("a", "b", "d", "c"));
    assertThat("bfs backwards sequence",
               BasicGraphOperations.breadthFirstSearch(graph, false, a)
                                   .map(n -> n.getNode().getName())
                                   .collect(Collectors.toList()),
               contains("a", "d", "c", "b"));
  }

  /**
   * Performs a depth first search and checks the correct sequence.
   */
  @Test
  public void depthFirstSearch()
  {
    assertThat("dfs sequence",
               BasicGraphOperations.depthFirstSearch(graph, a)
                                   .map(n -> n.getNode().getName())
                                   .collect(Collectors.toList()),
               contains("a", "b", "c", "d"));

  }

}
