package de.tautenhahn.dependencies.rest;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for cycle finding.
 *
 * @author TT
 */
public class TestCyclesOnly
{

    /**
     * Asserts that a package dependency from a test suite into a sub package is disregarded.
     */
    @Test
    public void ignoreTestSuites()
    {
        ContainerNode root = ContainerNode.createRoot();
        ClassNode runWith = root.createLeaf("org.junit.runner.RunWith");
        ClassNode testAnnotation = root.createLeaf("org.junit.Test");
        assertThat(runWith.getClassName()).as("class name").isEqualTo("org.junit.runner.RunWith");
        ClassNode helper = root.createLeaf("de.tautenhahn.TestHelper");
        ClassNode suite = root.createLeaf("de.tautenhahn.TestSuite");
        suite.addSuccessor(runWith);
        ClassNode test = root.createLeaf("de.tautenhahn.impl.MyTest");
        test.addSuccessor(testAnnotation);
        test.addSuccessor(helper);
        suite.addSuccessor(test);
        root.find("de.tautenhahn").setListMode(ListMode.LEAFS_COLLAPSED);
        Node testPackage = root.find("de.tautenhahn.impl");
        testPackage.setListMode(ListMode.LEAFS_COLLAPSED);
        DiGraph graph = new DiGraph(root);
        assertThat(suite.getSuccessors()).as("successors of suite").contains(testPackage);
        IndexedNode basePackage = graph
            .getAllNodes()
            .stream()
            .filter(n -> "tautenhahn".equals(n.getNode().getSimpleName()))
            .findAny()
            .orElse(null);
        assertThat(basePackage.getSuccessors()).as("successors of base package").hasSize(2);

        CyclesOnly systemUnderTest = new CyclesOnly();
        systemUnderTest.removeNoncriticalArcs(graph);
        assertThat(basePackage.getSuccessors()).as("successors of base package after removal").hasSize(1);
    }

    /**
     * Check that all instances are equal.
     */
    @Test
    public void allInstancesEqual()
    {
        assertThat(new CyclesOnly()).as("new instance").isEqualTo(new CyclesOnly());
        assertThat(ImpliedByNode.dependingOn("org.junit.runner.RunWith"))
            .as("other class")
            .isNotEqualTo(new CyclesOnly());
    }
}
