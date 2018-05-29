package de.tautenhahn.dependencies.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.tautenhahn.dependencies.analyzers.CycleFinder;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;
import de.tautenhahn.dependencies.parser.Pair;


/**
 * Reports all cyclic dependencies on package level. It will consider only classes found as class files in
 * some directory.
 *
 * @author TT
 */
public class CyclicPackageDependencies extends CyclicDependencies
{

  public List<Map<Pair<String, String>, List<Pair<String, String>>>> cycles = new ArrayList<>();

  public static CyclicPackageDependencies findFor(ContainerNode root)
  {
    root.getChildren()
        .stream()
        .filter(c -> c.getSimpleName().startsWith("dir:"))
        .forEach(CyclicPackageDependencies::adjustListMode);
    CyclicPackageDependencies result = new CyclicPackageDependencies();
    result.init(root);
    return result;
  }

  private static void adjustListMode(Node resource)
  {
    resource.setListMode(ListMode.LEAFS_COLLAPSED);
    if (resource instanceof ContainerNode)
    {
      ((ContainerNode)resource).walkSubTree().forEach(n -> {
        if (n.getListMode() == ListMode.EXPANDED)
        {
          n.setListMode(ListMode.LEAFS_COLLAPSED);
        }
      });
    }
  }

  public void init(ContainerNode root)
  {
    DiGraph packageDeps = new DiGraph(root);
    CycleFinder cycles = new CycleFinder(packageDeps);
    StringBuilder result = new StringBuilder();
    cycles.getStrongComponents().stream().filter(c -> c.size() > 1).forEach(c -> explainCycle(c, result));
  }


  private void explainCycle(List<IndexedNode> c, StringBuilder result)
  {
    Map<Pair<String, String>, List<Pair<String, String>>> cycle = new HashMap<>();
    for ( IndexedNode node : c )
    {
      for ( IndexedNode succ : node.getSuccessors() )
      {
        Pair<String, String> key = new Pair<>(getContainerString(node.getNode()),
                                              getContainerString(succ.getNode()));
        List<Pair<String, String>> reason = node.getNode()
                                                .getDependencyReason(succ.getNode())
                                                .stream()
                                                .map(this::pairToString)
                                                .collect(Collectors.toList());
        cycle.put(key, reason);
      }
    }
    cycles.add(cycle);
  }

  private Pair<String, String> pairToString(Pair<Node, Node> input)
  {
    return new Pair<>(getClassString(input.getFirst()), getClassString(input.getSecond()));
  }

  protected String getContainerString(Node n)
  {
    String result = n.getName();
    return result.substring(result.indexOf('.') + 1); // TODO: look again when EARs are supported
  }

  protected String getClassString(Node n)
  {
    return n.getSimpleName();
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
              .append("     (");
        entry.getValue()
             .forEach(p -> result.append(p.getFirst()).append(" -> ").append(p.getSecond()).append(", "));
        result.append(")");
      }
      result.append("\n");
    }
    return result.toString();

  }
}
