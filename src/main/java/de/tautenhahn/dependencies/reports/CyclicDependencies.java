package de.tautenhahn.dependencies.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.tautenhahn.dependencies.analyzers.ClassInterpreter;
import de.tautenhahn.dependencies.analyzers.CycleFinder;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;
import de.tautenhahn.dependencies.parser.Pair;


/**
 * Checks the enclosing project for cyclic dependencies on package and jar level. Define an own test class
 * which fails if results are worse than expected.
 *
 * @author TT
 */
public class CyclicDependencies
{

  /**
   * For programmatic access. Special types will be introduced when needed.
   */
  List<Map<Pair<String, String>, List<Pair<String, String>>>> cycles = new ArrayList<>();

  /**
   * Creates new instance analyzing given graph.
   * 
   * @param graph
   */
  public CyclicDependencies(DiGraph graph)
  {
    CycleFinder finder = new CycleFinder(graph);
    finder.getStrongComponents().stream().filter(c -> c.size() > 1).forEach(this::explainCycle);
  }

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
        boolean isFirst = true;
        for ( Pair<String, String> rsn : entry.getValue() )
        {
          if (isFirst)
          {
            isFirst = false;
          }
          else
          {
            result.append(", ");
          }
          result.append(rsn.getFirst()).append(" -> ").append(rsn.getSecond());
        }
        result.append(')');
      }
      result.append('\n');
    }
    return result.toString();
  }

  /**
   * Returns a measure on how bad the cyclic dependencies are.
   */
  public int getSeverity()
  {
    return cycles.stream().mapToInt(Map::size).sum();
  }

  /**
   * Returns a report about packages in build directories (interpreted as the own packages in your software).
   *
   * @param root
   */
  public static CyclicDependencies findForPackages(ContainerNode root)
  {
    List<Node> packages = root.getChildren()
                              .stream()
                              .filter(c -> c.getSimpleName().startsWith("dir:"))
                              .map(n -> (ContainerNode)n)
                              .flatMap(ContainerNode::walkCompleteSubTree)
                              .filter(ContainerNode.class::isInstance)
                              .peek(p -> p.setListMode(ListMode.LEAFS_COLLAPSED))
                              .collect(Collectors.toList());
    packages.removeIf(p -> !p.hasOwnContent());
    DiGraph packageDeps = new DiGraph(packages.stream());
    ClassInterpreter interpreter = new ClassInterpreter();
    interpreter.removeFactoryDependencies(packageDeps);
    interpreter.removeTestSuiteDependencies(packageDeps);
    return new CyclicDependencies(packageDeps);
  }

  /**
   * Returns a report about jars.
   * 
   * @param root
   */
  public static CyclicDependencies findForJars(ContainerNode root)
  {
    Stream<Node> jars = root.getChildren()
                            .stream()
                            .filter(c -> c.getSimpleName().startsWith("jar:"))
                            .peek(p -> p.setListMode(ListMode.COLLAPSED));
    DiGraph packageDeps = new DiGraph(jars);
    return new CyclicDependencies(packageDeps);
  }

  private void explainCycle(List<IndexedNode> c)
  {
    Map<Pair<String, String>, List<Pair<String, String>>> cycle = new HashMap<>();
    for ( IndexedNode node : c )
    {
      for ( IndexedNode succ : node.getSuccessors() )
      {
        Pair<String, String> key = new Pair<>(getName(node), getName(succ));
        List<Pair<String, String>> reason = node.getNode().explainDependencyTo(succ.getNode());
        cycle.put(key, reason);
      }
    }
    cycles.add(cycle);
  }

  private String getName(IndexedNode node)
  {
    return node.getNode()
               .getName()
               .replaceAll(".*:[^.]*\\.", "")
               .replaceAll("[jwer]ar:", "")
               .replaceAll("_([jwer]ar)", ".$1");
  }
}
