package de.tautenhahn.dependencies.core.analyzers;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.EdgeDensity;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;


public class TestEdgeDensity
{

  @Test
  public void density()
  {
    ContainerNode root = ContainerNode.createRoot();
    for ( char label = 'a' ; label < 'd' ; label++ )
    {
      root.createLeaf("" + label);
    }
    ((ClassNode)root.find("a")).addSuccessor((ClassNode)root.find("b"));
    ((ClassNode)root.find("b")).addSuccessor((ClassNode)root.find("c"));
    DiGraph graph = new DiGraph(root);
    assertThat("density", new EdgeDensity().getDensity(graph), closeTo(0.3333d, 0.001d));
  }
}
