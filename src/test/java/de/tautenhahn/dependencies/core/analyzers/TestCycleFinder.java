package de.tautenhahn.dependencies.core.analyzers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.CycleFinder;
import de.tautenhahn.dependencies.core.InnerNode;
import de.tautenhahn.dependencies.core.Leaf;
import de.tautenhahn.dependencies.core.Node;


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
    InnerNode root = InnerNode.createRoot();
    for ( char label = 'a' ; label < 'g' ; label++ )
    {
      root.createLeaf("" + label);
    }
    ((Leaf)root.find("a")).addSuccessor((Leaf)root.find("d"));
    ((Leaf)root.find("a")).addSuccessor((Leaf)root.find("f"));
    ((Leaf)root.find("b")).addSuccessor((Leaf)root.find("a"));
    ((Leaf)root.find("c")).addSuccessor((Leaf)root.find("d"));
    ((Leaf)root.find("d")).addSuccessor((Leaf)root.find("e"));
    ((Leaf)root.find("e")).addSuccessor((Leaf)root.find("a"));
    ((Leaf)root.find("f")).addSuccessor((Leaf)root.find("e"));

    List<List<Node>> components = new CycleFinder(root).getStrongComponents();

    assertThat("biggest component", components.get(0), hasSize(4));
  }

}
