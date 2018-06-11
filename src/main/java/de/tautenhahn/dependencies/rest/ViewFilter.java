package de.tautenhahn.dependencies.rest;

import java.util.function.Function;

import de.tautenhahn.dependencies.analyzers.DiGraph;


/**
 * Replaces a graph with a specially filtered one.
 *
 * @author TT
 */
public interface ViewFilter extends Function<DiGraph, DiGraph>
{

  /**
   * Returns true if this filter can be applied to the current graph. Application may remove filters which are
   * no longer applicable.
   */
  boolean isApplicable(DiGraph graph);

  /**
   * Returns the name of the filter.
   */
  String getName();

}
