package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node.ListMode;


public class TestCyclesOnly
{

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
    root.find("de.tautenhahn.impl").setListMode(ListMode.LEAFS_COLLAPSED);
    DiGraph graph = new DiGraph(root);

    CyclesOnly systemUnderTest = new CyclesOnly();
    // systemUnderTest.removeNoncriticalArcs(graph);
    graph.getAllNodes().forEach(n -> System.out.println(n + " " + n.getSuccessors()));
  }

}
