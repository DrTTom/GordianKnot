package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;


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
    assertThat("class name", runWith.getClassName(), is("org.junit.runner.RunWith"));
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
    assertThat("successors of suite", suite.getSuccessors(), hasItem(testPackage));
    IndexedNode basePackage = graph.getAllNodes()
                                   .stream()
                                   .filter(n -> "tautenhahn".equals(n.getNode().getSimpleName()))
                                   .findAny()
                                   .orElse(null);
    assertThat("successors of base package", basePackage.getSuccessors(), hasSize(2));

    CyclesOnly systemUnderTest = new CyclesOnly();
    systemUnderTest.removeNoncriticalArcs(graph);
    assertThat("successors of base package after removal", basePackage.getSuccessors(), hasSize(1));
  }

  /**
   * Check that all instances are equal.
   */
  @Test
  public void allInstancesEqual()
  {
    assertThat("new instance", new CyclesOnly(), equalTo(new CyclesOnly()));
    assertThat("other class",
               ImpliedByNode.dependingOn("org.junit.runner.RunWith"),
               not(equalTo(new CyclesOnly())));
  }

}
