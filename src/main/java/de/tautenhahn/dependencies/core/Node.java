package de.tautenhahn.dependencies.core;

import java.util.List;
import java.util.stream.Stream;


/**
 * Node in a dependency structure, might stand for a class, package, package set, jar file or module. Nodes
 * are expected to form a single-root tree structure where all children are classes. Computations will be made
 * on the class nodes initially, the higher level nodes will follow the results. <br>
 * Note that we distinguish between the "contains" and "dependsOn" relations.
 * 
 * @author TT
 */
public abstract class Node
{

  public static final char SEPARATOR = '.';

  private final Node parent;

  private final String simpleName;

  private boolean collapsed;

  /**
   * Creates new instance.
   * 
   * @param parent
   * @param simpleName
   */
  Node(Node parent, String simpleName)
  {
    this.parent = parent;
    this.simpleName = simpleName;
  }

  /**
   * Returns the fully qualified name.
   */
  public String getName()
  {
    return parent == null ? simpleName : parent.getName() + '/' + simpleName;
  }

  /**
   * Returns the simple name.
   */
  public String getSimpleName()
  {
    return simpleName;
  }



  /**
   * Returns the parent in the container structure.
   */
  public Node getParent()
  {
    return parent;
  }

  /**
   * Returns the direct children on the container structure.
   */
  abstract List<Node> getChildren();

  /**
   * Returns the nodes which depend on this node. In case an inner node is collapsed, it will be returned
   * instead of its hidden children.
   */
  abstract List<Node> getSuccessors();

  /**
   * Return the node this node depends on. Same handling of collapsed nodes.
   */
  abstract List<Node> getPredecessors();

  /**
   * Returns a list of pairs (a,b) where a is a child of the current node, b a child of the other node, a
   * depends on b and both a and b are leaves.
   * 
   * @return
   */
  abstract List<List<Node>> getDependencyReason(Node other);

  /**
   * Returns a stream of children in depth-first order.
   */
  abstract Stream<Node> walkSubTree();

  /**
   * Returns true if all the children of this node in the container structure are handled as integral part of
   * this node, false if they are considered as separate nodes.
   */
  public boolean isCollapsed()
  {
    return collapsed;
  }

  /**
   * If parameter is true, combine this node with all its children into one collective node, collapse the
   * children as well. If false is given, consider the children as separate nodes.
   * 
   * @param collapsed
   */
  public void setCollapsed(boolean collapsed)
  {
    this.collapsed = collapsed;
  }

  public Node find(String path)
  {
    int pos = path.indexOf(SEPARATOR);
    String first = pos > 0 ? path.substring(0, pos) : path;
    String rest = pos > 0 ? path.substring(pos + 1) : null;
    Node result = getChildren().stream().filter(n -> n.getSimpleName().equals(first)).findAny().orElse(null);
    return rest == null || result == null ? result : (InnerNode)result.find(rest);
  }

  protected Pair<String, String> splitPath(String path)
  {
    int pos = path.indexOf(SEPARATOR);
    return pos > 0 ? new Pair(path.substring(0, pos), path.substring(pos + 1)) : new Pair(path, null);
  }

}
