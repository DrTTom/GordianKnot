package de.tautenhahn.dependencies.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.tautenhahn.dependencies.analyzers.ReferenceChecker;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.ParsedClassPath;


/**
 * Reports classes and jars which are not or rarely referenced.
 *
 * @author TT
 */
public class Unreferenced
{

  private final List<UnrefElement> classes = new ArrayList<>();

  private final List<UnrefElement> jars = new ArrayList<>();

  private final List<AlmostUnrefElement> rarelyUsedLibs = new ArrayList<>();

  private final List<AlmostUnrefElement> littleSupplyingLibs = new ArrayList<>();

  private final int usedByLimit;

  private final int jarContributionLimit;

  /**
   * Specifies an element by display name and full node name.
   */
  public static class UnrefElement
  {

    private final String name;

    private final String nodeName;

    UnrefElement(Node node)
    {
      this.name = node.getDisplayName();
      this.nodeName = node.getName();
    }

    /**
     * @return the name
     */
    public String getName()
    {
      return name;
    }

    /**
     * @return the nodeName
     */
    public String getNodeName()
    {
      return nodeName;
    }
  }

  /**
   * Like {@link UnrefElement} but with a list of class names where the (few) references are.
   */
  public static class AlmostUnrefElement extends UnrefElement
  {

    private final List<String> refClasses;

    AlmostUnrefElement(Node node, List<String> refClasses)
    {
      super(node);
      this.refClasses = refClasses;
    }

    /**
     * @return the refClasses
     */
    public List<String> getRefClasses()
    {
      return refClasses;
    }
  }

  /**
   * Allows fluent API for several options.
   */
  public static class Builder
  {

    ReferenceChecker checker;

    Builder(ReferenceChecker checker)
    {
      this.checker = checker;
    }

    /**
     * Creates instance.
     */
    public Unreferenced create()
    {
      return new Unreferenced(checker);
    }

    /**
     * Adjusts the report limits.
     *
     * @param usedBy
     * @param contributing
     */
    public Builder withLimits(int usedBy, int contributing)
    {
      checker.setUsedByLimit(usedBy);
      checker.setJarContributionLimit(contributing);
      return this;
    }
  }

  /**
   * Fluent for creating an instance with optional settings.
   *
   * @param root
   * @param filter
   * @param classpath
   * @return temporary builder instance
   */
  public static Builder forProject(ContainerNode root, Filter filter, ParsedClassPath classpath)
  {
    return new Builder(new ReferenceChecker(root, filter, classpath));
  }

  Unreferenced(ReferenceChecker checker)
  {
    checker.getUnrefClasses().forEach(n -> classes.add(new UnrefElement(n)));
    checker.getUnrefJars().forEach(n -> jars.add(new UnrefElement(n)));
    checker.getLittleUsedJars()
           .forEach((n, l) -> rarelyUsedLibs.add(new AlmostUnrefElement(n, toClassNameList(l))));
    checker.getLittleContributionJars()
           .forEach((n, l) -> littleSupplyingLibs.add(new AlmostUnrefElement(n, l)));
    usedByLimit = checker.getUsedByLimit();
    jarContributionLimit = checker.getJarContributionLimit();
  }


  private List<String> toClassNameList(List<ClassNode> l)
  {
    return l.stream().map(ClassNode::getClassName).collect(Collectors.toList());
  }

  /**
   * Returns the list of classes which are not referenced, using the configured exceptions.
   */
  public List<UnrefElement> getUnreferencedClasses()
  {
    return classes;
  }

  /**
   * Returns the list of jar files without referenced classes in it.
   */
  public List<UnrefElement> getUnreferencedJars()
  {
    return jars;
  }

  /**
   * Returns a list of jars which are required by a small number of classes only.
   */
  public List<AlmostUnrefElement> getRarelyUsedJars()
  {
    return rarelyUsedLibs;
  }

  /**
   * Returns a list of jars from which a small number of classes is referenced.
   */
  public List<AlmostUnrefElement> getLittleUsedJars()
  {
    return littleSupplyingLibs;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    result.append("UNREFERENCED CLASSES:"); // NOPMD next append is in a loop
    classes.forEach(c -> result.append("\n   ").append(c.getName()));
    result.append("\nUNUSED LIBRARIES:");
    jars.forEach((j) -> result.append("\n   ").append(j.getName()));
    result.append("\nLIBRARIES USED BY ").append(usedByLimit).append(" OR LESS CLASSES:");
    addEntries(rarelyUsedLibs, result);
    result.append("\nLIBRARIES PROVIDING ").append(jarContributionLimit).append(" OR LESS CLASSES:");
    addEntries(littleSupplyingLibs, result);
    return result.toString();
  }

  private void addEntries(List<AlmostUnrefElement> list, StringBuilder result)
  {
    list.forEach(e -> result.append("\n   ").append(e.getName()).append("  ").append(e.getRefClasses()));
  }
}
