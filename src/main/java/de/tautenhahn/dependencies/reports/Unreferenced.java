package de.tautenhahn.dependencies.reports;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;
import de.tautenhahn.dependencies.parser.Pair;


/**
 * Reports classes and jars which are not referenced.
 *
 * @author TT
 */
public class Unreferenced
{

  private List<String> classes;

  private List<String> jars;

  private Map<String, List<String>> rarelyUsedLibs;

  private Map<String, List<String>> littleSupplyingLibs;

  /**
   * Defines how the report is built.
   */
  public static class ReportConfig
  {

    ClassLoader loader;

    List<String> knownEntryClasses = new ArrayList<>();

    boolean reportUnrefClasses = true;

    boolean reportUnrefJars = true;

    int rareUsageLimit = 2;

    int littleSupplyLimit = 1;

    public void setLoader(ClassLoader loader)
    {
      this.loader = loader;
    }

    /**
     * Adds names of nodes which are known to be needed by the application but may not be found, for instance
     * classes which are only dynamically instantiated.
     *
     * @param name
     */
    public void addNeededElements(String... name)
    {
      knownEntryClasses.addAll(Arrays.asList(name));
    }

    boolean isKnownAsNeeded(String className)
    {
      if (knownEntryClasses.contains(className))
      {
        return true;
      }
      List<String> markers = Arrays.asList("org.junit.Test");
      if (loader != null)
      {

        try
        {
          Class<?> clazz = loader.loadClass(className);
          for ( Method m : clazz.getMethods() )
          {
            if (Arrays.stream(m.getAnnotations())
                      .map(a -> a.annotationType().getName())
                      .anyMatch(n -> markers.contains(n)))
            {
              return true;
            }
          }
        }
        catch (ClassNotFoundException e)
        {
          return false;
        }
      }
      return false;
    }

  }

  /**
   * Creates instance for given parsed project. Instance will change list mode as it works.
   *
   * @param root
   */
  public Unreferenced(ContainerNode root, ReportConfig cfg)
  {
    root.walkCompleteSubTree().forEach(n -> n.setListMode(n.getSimpleName().startsWith("jar:")
      ? ListMode.COLLAPSED : ListMode.EXPANDED));
    if (cfg.reportUnrefClasses)
    {
      classes = root.walkSubTree()
                    .filter(n -> n instanceof ClassNode)
                    .map(n -> (ClassNode)n)
                    .filter(n -> n.getPredecessors().isEmpty())
                    .map(ClassNode::getClassName)
                    .filter(name -> !cfg.isKnownAsNeeded(name))
                    .collect(Collectors.toList());
    }

    if (cfg.reportUnrefJars)
    {
      jars = collectNodesWith(root, Unreferenced::isJar, List::isEmpty);
    }
    if (cfg.rareUsageLimit > 0)
    {
      rarelyUsedLibs = new HashMap<>();
      root.walkSubTree().filter(Unreferenced::isJar).forEach(j -> checkRarelyUsed(j, cfg.rareUsageLimit));
    }
    if (cfg.littleSupplyLimit > 0)
    {
      littleSupplyingLibs = new HashMap<>();
      root.walkSubTree()
          .filter(Unreferenced::isJar)
          .forEach(j -> checkSuppliesLittle(j, cfg.littleSupplyLimit));
    }
  }

  private void checkSuppliesLittle(Node j, int littleSupplyLimit)
  {
    List<Node> predecessors = j.getPredecessors();
    if (predecessors.isEmpty() || predecessors.stream().anyMatch(Unreferenced::isJar))
    {
      return;
    }
    Set<String> supplied = new HashSet<>();
    for ( Node pred : predecessors )
    {
      pred.explainDependencyTo(j).stream().map(Pair::getSecond).forEach(supplied::add);
      if (supplied.size() > littleSupplyLimit)
      {
        return;
      }
    }
    littleSupplyingLibs.put(j.toString(), new ArrayList(supplied));
  }

  private static boolean isJar(Node n)
  {
    return n.getSimpleName().startsWith("jar:");
  }

  private void checkRarelyUsed(Node j, int rareUsageLimit)
  {
    List<Node> predecessors = j.getPredecessors();
    if (predecessors.isEmpty() || predecessors.size() > rareUsageLimit
        || predecessors.stream().anyMatch(Unreferenced::isJar))
    {
      return;
    }
    rarelyUsedLibs.put(j.toString(), predecessors.stream().map(Node::getName).collect(Collectors.toList()));
  }

  private static List<String> collectNodesWith(ContainerNode root,
                                               Predicate<Node> ofType,
                                               Predicate<List<Node>> pred)
  {
    return root.walkSubTree()
               .filter(ofType)
               .filter(n -> pred.test(n.getPredecessors()))
               .map(Node::getName)
               .collect(Collectors.toList());
  }


  /**
   * Returns the list of classes which are not referenced, using the configured exceptions.
   */
  public List<String> getUnreferencedClasses()
  {
    return Optional.ofNullable(classes).orElseThrow(() -> new IllegalStateException("not computed"));
  }

  /**
   * Returns the list of jar files without referenced classes in it.
   */
  public List<String> getUnreferencedJars()
  {
    return Optional.ofNullable(jars).orElseThrow(() -> new IllegalStateException("not computed"));
  }

  /**
   * Returns a list of jars which are required by a small number of classes only.
   *
   * @param limit
   */
  public Map<String, List<String>> getRarelyUsedJars(int limit)
  {
    return Optional.ofNullable(rarelyUsedLibs).orElseThrow(() -> new IllegalStateException("not computed"));
  }

  /**
   * Returns a list of jars from which a small number of classes is referenced.
   *
   * @param limit
   */
  public Map<String, List<String>> getLittleUsedJars(int limit)
  {
    return Optional.ofNullable(littleSupplyingLibs)
                   .orElseThrow(() -> new IllegalStateException("not computed"));
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    if (classes != null)
    {
      result.append("Unused classes:");
      classes.forEach(u -> result.append("\n").append(u));
    }

    if (jars != null)
    {
      result.append("\nUnused libraries:");
      jars.forEach(u -> result.append("\n").append(u));
    }

    return result.toString();
  }
}
