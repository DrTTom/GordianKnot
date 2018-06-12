package de.tautenhahn.dependencies.rest;

import java.util.ArrayList;
import java.util.List;

import de.tautenhahn.dependencies.analyzers.CycleFinder;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Pair;


/**
 * Restricts the graph to elements contained in non-trivial cycles.
 *
 * @author TT
 */
public class CyclesOnly implements ViewFilter
{

  @Override
  public DiGraph apply(DiGraph input)
  {
    DiGraph allCycles = new CycleFinder(input).createGraphFromCycles();
    removeNoncriticalArcs(allCycles);
    return new CycleFinder(allCycles).createGraphFromCycles();
  }

  @Override
  public boolean isApplicable(DiGraph graph)
  {
    return true;
  }

  @Override
  public String getName()
  {
    return "non-trivial components of strong connectivity";
  }

  /**
   * Removes arcs from the graph which cause non-critical cycles, namely
   * <ul>
   * <li>inner classes depend on their outer class</li>
   * <li>Test suites are allowed to reference tests from sub-packages</li>
   * <li>Factory classes are allowed to reference classes from sub-packages</li>
   * <ul>
   */
  void removeNoncriticalArcs(DiGraph input)
  {
    for ( IndexedNode n : input.getAllNodes() )
    {
      removeArcToOuterClass(input, n);
      removeArcsCreatedByTestSuites(input, n);
    }
  }

  private void removeArcsCreatedByTestSuites(DiGraph input, IndexedNode node)
  {
    List<IndexedNode> ignorableSuccessors = new ArrayList<>();
    for ( IndexedNode succ : node.getSuccessors() )
    {
      List<Pair<Node, Node>> dep = node.getNode().getDependencyReason(succ.getNode());
      if (dep.stream()
             .allMatch(p -> isTestSuite(p.getFirst()) && (isTest(p.getSecond()) || isTestSuite(p.getSecond()))
                            && p.getFirst().getParent().isAnchestor(p.getSecond().getParent())))
      {
        ignorableSuccessors.add(succ);
      }
    }
    ignorableSuccessors.forEach(succ -> input.removeArc(node, succ));
  }

  private boolean isTestSuite(Node n)
  {
    return ((ClassNode)n).getSucLeafs()
                         .stream()
                         .map(s -> ((ClassNode)s).getClassName())
                         .anyMatch("org.junit.runner.RunWith"::equals);
  }

  private boolean isTest(Node n)
  {
    return ((ClassNode)n).getSucLeafs()
                         .stream()
                         .map(s -> ((ClassNode)s).getClassName())
                         .anyMatch("org.junit.Test"::equals);
  }

  private void removeArcToOuterClass(DiGraph input, IndexedNode node)
  {
    if (node.getNode() instanceof ClassNode)
    {
      String name = node.getNode().getSimpleName();
      int pos = name.lastIndexOf('$');
      if (pos == -1)
      {
        return;
      }
      String outerName = name.substring(0, pos);
      node.getSuccessors()
          .stream()
          .filter(s -> s.getNode().getSimpleName().equals(outerName))
          .filter(s -> s.getNode().getParent() == node.getNode().getParent())
          .findAny()
          .ifPresent(s -> input.removeArc(node, s));
    }
  }
}
