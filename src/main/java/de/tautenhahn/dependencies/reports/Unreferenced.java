package de.tautenhahn.dependencies.reports;

import java.util.List;
import java.util.Map;

import de.tautenhahn.dependencies.parser.ContainerNode;


/**
 * Reports classes and jars which are not referenced.
 * 
 * @author TT
 */
public class Unreferenced
{

  /**
   * @param root
   */
  public Unreferenced(ContainerNode root)
  {

  }

  /**
   * Specifies to ignore unreferenced classes if they have a main method or some other known mechanism of
   * invocation without direct reference, for instance EJBs, servlets ...
   */
  public void ignoreDefaultEntryClasses()
  {

  }

  /**
   * Adds names of nodes which are known to be needed by the application but may not be found, for instance
   * classes which are only dynamically instantiated.
   * 
   * @param name
   */
  public void addNeededElements(String... name)
  {

  }

  /**
   * Returns the list of classes which are not referenced, using the configured exceptions.
   */
  public List<String> getUnreferencedClasses()
  {
    return null;
  }

  /**
   * Returns the list of jar files without referenced classes in it.
   */
  public List<String> getUnreferencedJars()
  {
    return null;
  }

  /**
   * Returns a list of jars which are required by a small number of classes only.
   * 
   * @param limit
   */
  public Map<String, List<String>> getRarelyUsedJars(int limit)
  {
    return null;
  }

  /**
   * Returns a list of jars from which a small number of classes is referenced.
   * 
   * @param limit
   */
  public Map<String, List<String>> getLittleUsedJars(int limit)
  {
    return null;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    result.append("Unused Classes:");
    return result.toString();
  }
}
