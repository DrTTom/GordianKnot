package de.tautenhahn.dependencies.core.analyzers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.CycleFinder;
import de.tautenhahn.dependencies.analyzers.Graph;
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
  public void getComponents()
  {
    ContainerNode root = ContainerNode.createRoot();
    for ( char label = 'a' ; label < 'g' ; label++ )
    {
      root.createLeaf("" + label);
    }
    ((ClassNode)root.find("a")).addSuccessor((ClassNode)root.find("d"));
    ((ClassNode)root.find("a")).addSuccessor((ClassNode)root.find("f"));
    ((ClassNode)root.find("b")).addSuccessor((ClassNode)root.find("a"));
    ((ClassNode)root.find("c")).addSuccessor((ClassNode)root.find("d"));
    ((ClassNode)root.find("d")).addSuccessor((ClassNode)root.find("e"));
    ((ClassNode)root.find("e")).addSuccessor((ClassNode)root.find("a"));
    ((ClassNode)root.find("f")).addSuccessor((ClassNode)root.find("e"));

    List<List<Integer>> components = new CycleFinder(new Graph(root)).getStrongComponents();

    assertThat("biggest component", components.get(0), hasSize(4));
  }

}
