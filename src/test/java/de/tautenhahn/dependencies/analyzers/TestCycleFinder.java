package de.tautenhahn.dependencies.analyzers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;


/**
 * Unit test for finding cycles.
 *
 * @author TT
 */
public class TestCycleFinder
{

  /**
   * Asserts that in an example graph the biggest component of strong connectivity is found.
   */
  @Test
  public void findComponents()
  {
    ContainerNode root = ContainerNode.createRoot();
    for ( char label = 'a' ; label < 'g' ; label++ )
    {
      root.createLeaf(new String(new char[]{label}));
    }
    ((ClassNode)root.find("a")).addSuccessor((ClassNode)root.find("d"));
    ((ClassNode)root.find("a")).addSuccessor((ClassNode)root.find("f"));
    ((ClassNode)root.find("b")).addSuccessor((ClassNode)root.find("a"));
    ((ClassNode)root.find("c")).addSuccessor((ClassNode)root.find("d"));
    ((ClassNode)root.find("d")).addSuccessor((ClassNode)root.find("e"));
    ((ClassNode)root.find("e")).addSuccessor((ClassNode)root.find("a"));
    ((ClassNode)root.find("f")).addSuccessor((ClassNode)root.find("e"));

    CycleFinder systemUnderTest = new CycleFinder(new DiGraph(root));
    List<List<IndexedNode>> components = systemUnderTest.getStrongComponents();
    assertThat("biggest component", components.get(0), hasSize(4));

    DiGraph graph = systemUnderTest.createGraphFromCycles();
    assertThat("nodes in cycle graph", graph.getAllNodes(), hasSize(4));

  }

}
