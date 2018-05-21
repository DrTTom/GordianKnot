package de.tautenhahn.dependencies.core;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Node in a dependency structure, might stand for a class, package, package set, jar file or module. Nodes
 * are expected to form a single-root tree structure where all children are classes. Computations will be made
 * on the class nodes initially, the higher level nodes will follow the results. <br>
 * Note that we distinguish between the "contains" and "dependsOn" relations.<br>
 * WARNING: This is not a valid concept to model a general directed graph but specialized for analyzing java
 * dependencies.
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
    return Optional.ofNullable(parent)
                   .map(Node::getName)
                   .map(n -> n + SEPARATOR + simpleName)
                   .orElse(simpleName);
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
   * Returns the direct children on the container structure, empty in case of collapsed nodes.
   */
  public abstract List<Node> getChildren();

  /**
   * Returns the nodes which depend on this node. In case an inner node is collapsed, it will be returned
   * instead of its hidden children.
   */
  public abstract List<Node> getSuccessors();

  /**
   * Return the node this node depends on. Same handling of collapsed nodes.
   */
  public abstract List<Node> getPredecessors();

  /**
   * Returns a list of pairs (a,b) where a is a child of the current node, b a child of the other node, a
   * depends on b and both a and b represent the smallest known units containing the dependency. This looks
   * into collapsed nodes.
   */
  public abstract List<Pair<Node, Node>> getDependencyReason(Node other);

  /**
   * Returns a stream of children in depth-first order, ignoring parts of collapsed nodes.
   * 
   * @param skipHidden true to skip nodes inside collapsed containers, false to list all nodes.
   */
  public abstract Stream<Node> walkSubTree();

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

  /**
   * Returns sub-node specified by path, even if inside some collapsed node.
   * 
   * @param path relative to this node.
   */
  public Node find(String path)
  {
    Pair<String, String> parts = splitPath(path);
    Optional<Node> result = getChildren().stream()
                                         .filter(n -> n.getSimpleName().equals(parts.getFirst()))
                                         .findAny();
    return result.map(n -> Optional.ofNullable(parts.getSecond()).map(s -> n.find(s)).orElse(n)).orElse(null);
  }

  /**
   * Separates the first part of a path.
   * 
   * @param path
   */
  protected Pair<String, String> splitPath(String path)
  {
    int pos = path.indexOf(SEPARATOR);
    return pos > 0 ? new Pair<>(path.substring(0, pos), path.substring(pos + 1)) : new Pair<>(path, null);
  }

  /**
   * Returns the biggest collapsed node containing given node or node itself if no ancestor is collapsed.
   */
  protected Node replaceByCollapsedAnchestor(Node n)
  {
    Node result = n;
    Node parent = n.getParent();
    while (parent != null)
    {
      if (parent.isCollapsed())
      {
        result = parent;
      }
      parent = parent.getParent();
    }
    return result;
  }

}
