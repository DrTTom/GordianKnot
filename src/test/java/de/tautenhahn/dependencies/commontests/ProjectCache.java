package de.tautenhahn.dependencies.commontests;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.Pair;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;


/**
 * Helper class to avoid multiple scans of same project during test suite. Expects only one or two entries per
 * test suite, so details are kept very simple.
 * 
 * @author TT
 */
public final class ProjectCache
{

  private static Map<Pair<ParsedClassPath, Filter>, ContainerNode> cache = Collections.synchronizedMap(new LinkedHashMap<>());

  private static final int LIMIT = 4;

  private ProjectCache()
  {
    // not enough content to require even a singleton
  }

  /**
   * Returns the root node of the projects dependency structure.
   * 
   * @param path
   * @param filter
   */
  public static ContainerNode getScannedProject(ParsedClassPath path, Filter filter)
  {
    ContainerNode result = cache.computeIfAbsent(new Pair<>(path, filter),
                                                 p -> new ProjectScanner(filter).scan(path));
    if (cache.size() > LIMIT)
    {
      cache.remove(cache.keySet().iterator().next());
    }
    return result;
  }
}
