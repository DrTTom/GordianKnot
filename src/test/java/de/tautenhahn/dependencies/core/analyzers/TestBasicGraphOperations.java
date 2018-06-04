package de.tautenhahn.dependencies.core.analyzers;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.BasicGraphOperations;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;


/**
 * Unit test for basic functions on a graph.
 *
 * @author TT
 */
public class TestBasicGraphOperations
{

  /**
   * Asserts edge density is computed correctly.
   */
  @SuppressWarnings("boxing")
  @Test
  public void density()
  {
    ContainerNode root = ContainerNode.createRoot();
    for ( char label = 'a' ; label < 'd' ; label++ )
    {
      root.createLeaf(new String(new char[]{label}));
    }
    ((ClassNode)root.find("a")).addSuccessor((ClassNode)root.find("b"));
    ((ClassNode)root.find("b")).addSuccessor((ClassNode)root.find("c"));
    DiGraph graph = new DiGraph(root);
    assertThat("density", BasicGraphOperations.getDensity(graph), closeTo(0.3333d, 0.001d));
  }
}
