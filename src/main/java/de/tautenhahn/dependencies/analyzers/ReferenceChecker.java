package de.tautenhahn.dependencies.analyzers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ListModeUtil;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Pair;
import de.tautenhahn.dependencies.parser.ParsedClassPath;


/**
 * Checks all the references in a project finding unreferenced elements, unsatisfied references, libraries
 * which might be candidates for replacement and so on.
 *
 * @author TT
 */
public class ReferenceChecker
{

  private final ContainerNode root;

  private final Filter filter;

  private int jarContributionLimit = 3;

  private int usedByLimit = 3;

  private final Set<String> knownNeededClasses = new HashSet<>();

  private final ClassInterpreter interpreter;

  /**
   * Create instance.
   *
   * @param root project to analyze, WARNING: folding mode will be adjusted for this check, avoid parallel
   *          usage.
   * @param filter defines which elements to check
   * @param classpath in case a class loader is needed.
   */
  public ReferenceChecker(ContainerNode root, Filter filter, ParsedClassPath classpath)
  {
    this.root = root;
    ListModeUtil.showJarsAndOwnClasses(root);
    this.filter = filter;
    interpreter = new ClassInterpreter();
    interpreter.setClassLoader(classpath.createClassLoader());
  }

  /**
   * Adds names of classes which are known to be required by the software. Specify classes which are only
   * instantiated dynamically to exclude from the report.
   *
   * @param classNames fully qualified class names
   */
  public void addKnownNeededClasses(String... classNames)
  {
    knownNeededClasses.addAll(Arrays.asList(classNames));
  }

  /**
   * Returns the nodes representing unreferenced classes together with likely interpretations.
   */
  public List<ClassNode> getUnrefClasses()
  {
    List<ClassNode> unrefClasses = root.walkSubTree()
                                       .filter(n -> n instanceof ClassNode)
                                       .map(n -> (ClassNode)n)
                                       .filter(n -> n.getPredecessors().isEmpty())
                                       .filter(n -> filter.isInFocus(n.getName()))
                                       .collect(Collectors.toList());
    if (!unrefClasses.removeIf(interpreter::isTestSuite))
    {
      unrefClasses.removeIf(interpreter::isTest);
    }
    unrefClasses.removeIf(n -> knownNeededClasses.contains(n.getClassName()));
    unrefClasses.removeIf(interpreter::isRecognizedAsMainClass);
    unrefClasses.removeIf(interpreter::isEjb);
    unrefClasses.removeIf(interpreter::isWebService);
    // TODO: Servlets, registered services, ...
    return unrefClasses;
  }

  /**
   * Returns the list of unreferenced jar nodes.
   */
  public List<ContainerNode> getUnrefJars()
  {
    return root.walkSubTree()
               .filter(n -> n.getSimpleName().startsWith("jar:"))
               .filter(n -> n.getPredecessors().isEmpty())
               .map(n -> (ContainerNode)n)
               .collect(Collectors.toList());
  }

  /**
   * Returns all jar nodes from which too few classes are used together with the names of those classes.
   */
  public Map<ContainerNode, List<String>> getLittleContributionJars()
  {
    Map<ContainerNode, List<String>> result = new HashMap<>();
    root.walkSubTree()
        .filter(n -> n.getSimpleName().startsWith("jar:"))
        .forEach(j -> checkSuppliesLittle((ContainerNode)j, result));
    return result;
  }

  /**
   * To avoid false positives in case a jar has only one entry class, one could wish to count also re
   * recursive dependencies. However, that would disable almost all warnings, even if the library supplies
   * only a single unimportant method.
   */
  private void checkSuppliesLittle(ContainerNode jar, Map<ContainerNode, List<String>> result)
  {
    List<Node> predecessors = jar.getPredecessors();
    if (predecessors.isEmpty())
    {
      return;
    }
    Set<String> supplied = new HashSet<>();
    for ( Node pred : predecessors )
    {
      pred.explainDependencyTo(jar).stream().map(Pair::getSecond).forEach(supplied::add);
      if (supplied.size() > jarContributionLimit)
      {
        return;
      }
    }
    result.put(jar, new ArrayList<>(supplied));
  }

  /**
   * Returns the jar nodes which are referenced by too few classes together with the list of those class
   * nodes.
   */
  public Map<ContainerNode, List<ClassNode>> getLittleUsedJars()
  {
    Map<ContainerNode, List<ClassNode>> result = new HashMap<>();
    root.walkSubTree()
        .filter(n -> n.getSimpleName().startsWith("jar:"))
        .forEach(j -> checkRarelyUsed(j, result));
    return result;
  }

  private void checkRarelyUsed(Node j, Map<ContainerNode, List<ClassNode>> result)
  {
    List<Node> predecessors = j.getPredecessors();
    if (predecessors.isEmpty() || predecessors.size() > usedByLimit
        || predecessors.stream().anyMatch(n -> n.getSimpleName().startsWith("jar:")))
    {
      return;
    }
    result.put((ContainerNode)j, predecessors.stream().map(p -> (ClassNode)p).collect(Collectors.toList()));
  }


  /**
   * @return the usedByLimit
   */
  public int getUsedByLimit()
  {
    return usedByLimit;
  }

  /**
   * @param usedByLimit the usedByLimit to set
   */
  public void setUsedByLimit(int usedByLimit)
  {
    this.usedByLimit = usedByLimit;
  }

  /**
   * @return the jarContributionLimit
   */
  public int getJarContributionLimit()
  {
    return jarContributionLimit;
  }


  /**
   * @param jarContributionLimit the jarContributionLimit to set
   */
  public void setJarContributionLimit(int jarContributionLimit)
  {
    this.jarContributionLimit = jarContributionLimit;
  }

}
