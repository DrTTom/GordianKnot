package de.tautenhahn.dependencies.reports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


  /**
   * Creates instance.
   *
   * @param root
   */
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

  private void init(ContainerNode root)
  {
    DiGraph packageDeps = new DiGraph(root);
    CycleFinder finder = new CycleFinder(packageDeps);
    finder.getStrongComponents().stream().filter(c -> c.size() > 1).forEach(this::explainCycle);
  }


  private void explainCycle(List<IndexedNode> c)
  {
    Map<Pair<String, String>, List<Pair<String, String>>> cycle = new HashMap<>();
    for ( IndexedNode node : c )
    {
      for ( IndexedNode succ : node.getSuccessors() )
      {
        Pair<String, String> key = new Pair<>(node.getNode().toString(), succ.getNode().toString());
        List<Pair<String, String>> reason = node.getNode().explainDependencyTo(succ.getNode());
        cycle.put(key, reason);
      }
    }
    cycles.add(cycle);
  }


}
