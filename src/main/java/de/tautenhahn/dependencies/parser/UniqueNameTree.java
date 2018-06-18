package de.tautenhahn.dependencies.parser;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


/**
 * Creates unique short names from a set of paths.
 *
 * @author TT
 */
public class UniqueNameTree
{

  private final NameNode root = new NameNode("", null);

  /**
   * Need a tree structure.
   */
  private static class NameNode
  {

    final String name;

    Path remaining;

    Map<String, NameNode> children = new HashMap<>();

    NameNode(String name, Path remaining)
    {
      this.name = name;
      this.remaining = remaining;
    }
  }

  /**
   * @param path path from root excluding the parts already in my tree
   * @param ctx node standing for some "reversed path"
   */
  private void add(Path path, NameNode ctx)
  {
    if (path == null)
    {
      return; // already got that path
    }
    if (ctx.children.isEmpty() && ctx != root) // NOPMD this should compare the reference, not the value!
    {
      String oldName = ctx.remaining.getFileName().toString();
      Path oldRemaining = ctx.remaining.getParent();
      ctx.children.put(oldName, new NameNode(oldName + "/" + ctx.name, oldRemaining));
      add(path, ctx);
      return;
    }

    String thisLevel = path.getFileName().toString();
    Path remaining = path.getParent();
    NameNode child = ctx.children.get(thisLevel);
    if (child == null)
    {
      child = new NameNode(thisLevel + "/" + ctx.name, remaining);
      ctx.children.put(thisLevel, child);
      return;
    }
    add(remaining, child);
  }

  /**
   * Adds a new path.
   *
   * @param path
   */
  public void add(Path path)
  {
    add(path, root);
  }

  /**
   * Returns the names created for the paths.
   */
  public Map<Path, String> createNames()
  {
    Map<Path, String> result = new HashMap<>();
    addNames(root, "", result);
    return result;
  }


  private void addNames(NameNode node, String name, Map<Path, String> result)
  {
    if (node.children.isEmpty())
    {
      Path path = node.remaining.resolve(node.name);
      result.put(path, name);
    }
    else
    {
      boolean hasSeveralChildren = node.children.size() > 1;
      node.children.forEach((namePart, c) -> addNames(c, concat(hasSeveralChildren, namePart, name), result));
    }
  }

  private String concat(boolean firstPartDistinct, String first, String second)
  {
    if (firstPartDistinct)
    {
      return second.isEmpty() ? first : first + "_" + second;
    }
    return second;
  }
}
