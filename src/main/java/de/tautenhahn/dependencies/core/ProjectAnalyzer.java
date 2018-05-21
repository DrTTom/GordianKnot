package de.tautenhahn.dependencies.core;

import java.nio.file.Path;
import java.util.Collection;


/**
 * Analyzes a project and builds the dependency tree.
 * 
 * @author TT
 */
public class ProjectAnalyzer
{

  /**
   * Creates instance.
   * 
   * @param includes regular expressions for absolute node names.
   */
  public ProjectAnalyzer(String... includes)
  {

  }

  /**
   * Runs the dependency analysis for all classes in the class path with match some include. All other classes
   * are considered only as needed by included classes but not analyzed themselves.
   * 
   * @param classPath paths to jar files or build directories.
   * @return root node of the created graph.
   */
  public Node analyze(Collection<Path> classPath)
  {
    return null;
  }
}
