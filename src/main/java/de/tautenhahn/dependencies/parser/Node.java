package de.tautenhahn.dependencies.parser;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Node in a dependency structure. , might stand for a class, package, package set, jar file or module. Nodes
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

  /**
   * Defines how elements are listed and where dependencies are addressed to.
   */
  public enum ListMode
  {
    /** all children hidden, dependencies of whole subtree on this node */
    COLLAPSED,
    /** direct leaves collapsed, all other children listed separately */
    LEAFS_COLLAPSED,
    /** all children listed separately */
    EXPANDED;
  }

  /**
   * Separator for the name parts.
   */
  public static final char SEPARATOR = '.';

  private final Node parent;

  private final String simpleName;

  private ListMode listMode = ListMode.EXPANDED;

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
  public abstract Collection<Node> getChildren();

  abstract Node getChildByName(String simpleChildName);

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
   * Returns true if there is at least one class represented by this node and not by some expanded child node.
   */
  public abstract boolean hasOwnContent();

  /**
   * Returns a list of pairs (a,b) denoting the smallest known units where a is a child of the current node, b
   * a child of the other node, a depends on b and both a and b represent the smallest known units containing
   * the dependency. This looks into collapsed nodes.
   */
  public abstract List<Pair<Node, Node>> getDependencyReason(Node other);

  /**
   * Same as {@link #getDependencyReason(Node)} but returns pairs of short comprehensive strings.
   *
   * @param other
   */
  public List<Pair<String, String>> explainDependencyTo(Node other)
  {
    return getDependencyReason(other).stream()
                                     .map(p -> new Pair<>(p.getFirst().getRelativeName(this),
                                                          p.getSecond().getRelativeName(other)))
                                     .collect(Collectors.toList());
  }

  /**
   * Returns a stream of children in depth-first order, ignoring parts of collapsed nodes.
   */
  public abstract Stream<Node> walkSubTree();

  /**
   * Returns the mode in which children in the container structure are handled as integral part of this node
   * or as separate nodes.
   */
  public ListMode getListMode()
  {
    return listMode;
  }

  /**
   * If parameter is true, combine this node with all its children into one collective node, collapse the
   * children as well. If false is given, consider the children as separate nodes.
   *
   * @param listMode
   */
  public void setListMode(ListMode listMode)
  {
    this.listMode = listMode;
  }

  /**
   * Returns sub-node specified by path, even if inside some collapsed node.
   *
   * @param path relative to this node.
   */
  public Node find(String path)
  {
    Pair<String, String> parts = splitPath(path);
    Optional<Node> result = Optional.ofNullable(getChildByName(parts.getFirst()));
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
    Node ancestor = n.getParent();
    boolean isFirst = true;
    while (ancestor != null)
    {
      if (ancestor.listMode == ListMode.COLLAPSED || isFirst && ancestor.listMode == ListMode.LEAFS_COLLAPSED)
      {
        result = ancestor;
      }
      ancestor = ancestor.getParent();
      isFirst = false;
    }
    return result;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "(" + getName() + ")";
  }

  /**
   * Returns the name relative to another node. Will throw exception if not in the subtree. In case of nodes
   * are same, the simple name is returned instead because its more useful.
   */
  public String getRelativeName(Node ancestor)
  {
    if (ancestor == parent || ancestor == this) // NOPMD we mean the same object
    {
      return simpleName;
    }
    if (parent == null)
    {
      throw new IllegalArgumentException(ancestor + " is not an anchestor");
    }
    return parent.getRelativeName(ancestor) + SEPARATOR + simpleName;
  }

  /**
   * Return the list of all direct children, ignoring the list mode.
   */
  public abstract Collection<Node> getAllChildren();
}
