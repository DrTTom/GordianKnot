package de.tautenhahn.dependencies.commontests;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.Pair;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class to avoid multiple scans of same project during test suite. Expects only one or two entries per test
 * suite, so details are kept very simple.
 *
 * @author TT
 */
final class ProjectCache
{

    private static final Map<Pair<ParsedClassPath, Filter>, ContainerNode> CACHE =
        Collections.synchronizedMap(new LinkedHashMap<>());

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
        ContainerNode result =
            CACHE.computeIfAbsent(new Pair<>(path, filter), p -> new ProjectScanner(filter).scan(path));
        if (CACHE.size() > LIMIT)
        {
            CACHE.remove(CACHE.keySet().iterator().next());
        }
        return result;
    }
}
