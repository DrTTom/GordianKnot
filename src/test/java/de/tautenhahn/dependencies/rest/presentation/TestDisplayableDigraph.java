package de.tautenhahn.dependencies.rest.presentation;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.rest.presentation.DisplayableDiGraph.VisEdge;


/**
 * Unit tests for {@link DisplayableDiGraph} and its elements.
 *
 * @author TT
 */
public class TestDisplayableDigraph
{

  /**
   * Check that arcs in a directed graph are properly represented.
   */
  @Test
  public void checkEdges()
  {
    ContainerNode root = ContainerNode.createRoot();
    ClassNode a = root.createLeaf("jar:lib_jar.com.someone.ClassA");
    ClassNode b = root.createLeaf("dir:scr.de.ich.ClassB");
    b.addSuccessor(a);
    DisplayableDiGraph systemUnderTest = new DisplayableDiGraph(new DiGraph(root));
    List<VisEdge> edges = systemUnderTest.getEdges();
    assertThat("edges", edges, hasSize(1));
    VisEdge edge = edges.get(0);
    assertThat("from", edge.getFrom(), is("1"));
    assertThat("from", edge.getTo(), is("0"));
  }
}
