package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Pair;


/**
 * Provides some basic rules about certain classes which may have special reference status without being
 * listed as problem. The following situations are not considered as a problem:
 * <ul>
 * <li>A JUnit test suite references test classes in a sub-package, thus causing a cyclic package dependency.
 * </li>
 * <li>A Factory class references classes in a sub-package, thus causing a cyclic package dependency.</li>
 * TODO:
 * <li>An inner class references its outer class and vice versa, causing a cyclic class dependency.</li>
 * <li>A class having a main method is unreferenced.</li>
 * <li>An EJB, servlet class or JUnit test suite is unreferenced.</li>
 * <li>A JUnit test case is unreferenced and the project does not use test suites.</li>
 * </ul>
 *
 * @author TT
 */
public class ClassInterpreter
{

  /**
   * Removes arcs exclusively cause by a test suite referencing stuff in sub-packages.
   *
   * @param graph
   * @return true if changed
   */
  public boolean removeTestSuiteDependencies(DiGraph graph)
  {
    return removeArcsToSubpackage(graph, this::isTestSuite, n -> isTestSuite(n) || isTest(n));
  }

  /**
   * Removes arcs exclusively cause by a factory referencing stuff in sub-packages.
   *
   * @param graph
   * @return true if changed
   */
  public boolean removeFactoryDependencies(DiGraph graph)
  {
    return removeArcsToSubpackage(graph, n -> n.getClassName().endsWith("Factory"), n -> true);
  }

  /**
   * Removes all arcs outgoing from given node which are excused by some specified rule.
   *
   * @param graph
   * @param excusedStart
   * @param excusedTarget
   * @return true if graph is changed
   */
  private boolean removeArcsToSubpackage(DiGraph graph,
                                         Predicate<ClassNode> excusedStart,
                                         Predicate<ClassNode> excusedTarget)
  {
    boolean result = false;
    for ( IndexedNode node : graph.getAllNodes() )
    {
      List<IndexedNode> ignorableSuccessors = new ArrayList<>();
      for ( IndexedNode succ : node.getSuccessors() )
      {
        List<Pair<Node, Node>> dep = node.getNode().getDependencyReason(succ.getNode());
        if (dep.stream()
               .allMatch(p -> excusedStart.test((ClassNode)p.getFirst())
                              && excusedTarget.test((ClassNode)p.getSecond())
                              && p.getFirst().getParent().isAnchestor(p.getSecond().getParent())))
        {
          ignorableSuccessors.add(succ);
        }
      }
      ignorableSuccessors.forEach(succ -> graph.removeArc(node, succ));
      result = result || !ignorableSuccessors.isEmpty();
    }
    return result;
  }

  private boolean isTestSuite(ClassNode n)
  {
    return n.getSucLeafs()
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
}
