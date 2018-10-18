package de.tautenhahn.dependencies.rest;

import de.tautenhahn.dependencies.analyzers.ClassInterpreter;
import de.tautenhahn.dependencies.analyzers.CycleFinder;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;


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
    return "cycles only";
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
    }
    ClassInterpreter interpreter = new ClassInterpreter();
    interpreter.removeFactoryDependencies(input);
    interpreter.removeTestSuiteDependencies(input);
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

  @Override
  public int hashCode()
  {
    return 0;
  }

  @Override
  public boolean equals(Object obj)
  {
    return obj != null && CyclesOnly.class == obj.getClass();
  }
}
