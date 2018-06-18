package de.tautenhahn.dependencies.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tautenhahn.dependencies.parser.Pair;


/**
 * Checks the enclosing project for cyclic dependencies on package and jar level. Define an own test class
 * which fails if results are worse than expected.
 *
 * @author TT
 */
public abstract class CyclicDependencies
{

  /**
   * For programmatic access. Special types will be introduced when needed.
   */
  List<Map<Pair<String, String>, List<Pair<String, String>>>> cycles = new ArrayList<>();

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    for ( Map<Pair<String, String>, List<Pair<String, String>>> cycle : cycles )
    {
      result.append("detected cycle:");
      for ( Entry<Pair<String, String>, List<Pair<String, String>>> entry : cycle.entrySet() )
      {
        result.append("\n  ")
              .append(entry.getKey().getFirst())
              .append(" -> ")
              .append(entry.getKey().getSecond())
              .append("     ("); // NOPMD not called with consecutive literals
        entry.getValue()
             .forEach(p -> result.append(p.getFirst()).append(" -> ").append(p.getSecond()).append(", "));
        result.append(')');
      }
      result.append('\n');
    }
    return result.toString();

  }

}
