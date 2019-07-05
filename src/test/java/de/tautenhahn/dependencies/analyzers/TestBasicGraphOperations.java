package de.tautenhahn.dependencies.analyzers;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Pair;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    @BeforeEach
    public void createGraph()
    {
        ContainerNode root = ContainerNode.createRoot();
        for (char label = 'a'; label < 'e'; label++)
        {
            root.createLeaf(new String(new char[] {label}));
        }
        ((ClassNode) root.find("a")).addSuccessor((ClassNode) root.find("b"));
        ((ClassNode) root.find("a")).addSuccessor((ClassNode) root.find("d"));
        ((ClassNode) root.find("b")).addSuccessor((ClassNode) root.find("c"));
        ((ClassNode) root.find("c")).addSuccessor((ClassNode) root.find("d"));
        ((ClassNode) root.find("d")).addSuccessor((ClassNode) root.find("b"));
        ((ClassNode) root.find("d")).addSuccessor((ClassNode) root.find("a"));

        graph = new DiGraph(root);
        a = graph.getAllNodes().stream().filter(n -> n.getNode().getName().equals("a")).findFirst().orElse(null);
    }

    /**
     * Asserts edge density is computed correctly.
     */
    @Test
    public void density()
    {
        assertThat(BasicGraphOperations.getDensity(graph)).isCloseTo(0.5d, Offset.offset(0.001d));
    }

    /**
     * Performs a breadth first search and checks the correct sequence.
     */
    @Test
    public void breadthFirstSearch()
    {
        assertThat(BasicGraphOperations.breadthFirstSearch(graph, true, a))
            .as("bfs sequence")
            .extracting(n -> n.getNode().getName())
            .containsExactly("a", "b", "d", "c");
        assertThat(BasicGraphOperations.breadthFirstSearch(graph, false, a))
            .as("bfs backwards sequence")
            .extracting(n -> n.getNode().getName())
            .containsExactly("a", "d", "c", "b");
    }

    /**
     * Performs a depth first search and checks the correct sequence.
     */
    @Test
    public void depthFirstSearch()
    {
        assertThat(BasicGraphOperations.depthFirstSearch(graph, a))
            .as("dfs sequence")
            .extracting(n -> n.getNode().getName())
            .containsExactly("a", "b", "c", "d");
    }

    /**
     * Checks rcd value of sample graph.
     */
    @Test
    public void rcdValue()
    {
        Pair<int[], int[]> numbers = BasicGraphOperations.countDependsOnAndUsedFrom(graph);
        assertThat(BasicGraphOperations.rcd(numbers)).isEqualTo(2.0);
    }
}
