package de.tautenhahn.dependencies.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;


/**
 * Reports missing classes. For properly checked projects, this report will always be empty. However, in case
 * of lacking test coverage, there might be entries. Furthermore, this report may come handy while playing
 * around with your class path. It gives you a handy text report why some library is needed.
 * 
 * @author TT
 */
public class MissingClasses
{

  private final Set<String> knownClasses = new HashSet<>();

  private final Stack<String> referencingClassNames = new Stack<>();

  Map<String, List<List<String>>> content = new HashMap<>();

  /**
   * Creates report for given parsed project. Using own search here because of several subtle differences to
   * the basic graph search operation.
   * 
   * @param root
   * @param filter
   */
  public MissingClasses(ContainerNode root, Filter filter)
  {
    root.walkCompleteSubTree()
        .filter(n -> n instanceof ClassNode)
        .map(n -> (ClassNode)n)
        .filter(c -> !knownClasses.contains(c.getClassName()))
        .filter(c -> filter.isInFocus(c.getName()))
        .forEach(this::checkRefs);
  }

  private void checkRefs(ClassNode node)
  {
    String name = node.getClassName();
    referencingClassNames.push(name);
    knownClasses.add(name);
    node.getMissingDependencies().forEach(this::addMissing);
    node.getSucLeafs().stream().filter(n -> !knownClasses.contains(name)).forEach(this::checkRefs);
    referencingClassNames.pop();
  }

  private void addMissing(String className)
  {
    content.computeIfAbsent(className, n -> new ArrayList<>()).add(new ArrayList<>(referencingClassNames));
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    content.forEach((m, rs) -> {
      result.append('\n').append(m);
      rs.forEach(r -> result.append("\n   ").append(r));
    });
    return result.toString();
  }
}
