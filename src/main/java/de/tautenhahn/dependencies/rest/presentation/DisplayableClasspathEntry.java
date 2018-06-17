package de.tautenhahn.dependencies.rest.presentation;

import java.nio.file.Path;

import de.tautenhahn.dependencies.parser.ParsedClassPath;


/**
 * Represents a class path entry in the front end.
 *
 * @author TT
 */
public class DisplayableClasspathEntry
{

  private boolean active;

  private final String label;

  private String nodeId;

  private final String fullPath;

  /**
   * Creates instance.
   *
   * @param p
   * @param classPath
   */
  public DisplayableClasspathEntry(Path p, ParsedClassPath classPath)
  {
    fullPath = p.toString();
    label = classPath.getName(p);
    active = true;
  }


  /**
   * Returns true if this entry was used in the analysis of the project.
   */
  public boolean isActive()
  {
    return active;
  }


  /**
   * @param active the active to set
   */
  public void setActive(boolean active)
  {
    this.active = active;
  }


  /**
   * Returns the label of the resource node.
   */
  public String getLabel()
  {
    return label;
  }

  /**
   * @return the nodeId
   */
  public String getNodeId()
  {
    return nodeId;
  }


  /**
   * @param nodeId the nodeId to set
   */
  public void setNodeId(String nodeId)
  {
    this.nodeId = nodeId;
  }


  /**
   * Returns the path as found inside class path.
   */
  public String getFullPath()
  {
    return fullPath;
  }
}
