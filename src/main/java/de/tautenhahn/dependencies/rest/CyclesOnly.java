package de.tautenhahn.dependencies.rest;

import de.tautenhahn.dependencies.analyzers.CycleFinder;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;


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
    for ( IndexedNode n : input.getAllNodes() )
    {
      String name = n.getNode().getSimpleName();
      int pos = name.lastIndexOf('$');
      if (pos > 0)
      {
        String outerName = name.substring(0, pos);
        n.getSuccessors()
         .stream()
         .filter(s -> s.getNode().getSimpleName().equals(outerName))
         .findAny()
         .ifPresent(s -> input.removeArc(n, s));
      }
    }
    return new CycleFinder(input).createGraphFromCycles();
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

}
