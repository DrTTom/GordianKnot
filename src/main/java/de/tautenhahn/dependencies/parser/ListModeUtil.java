package de.tautenhahn.dependencies.parser;

import de.tautenhahn.dependencies.parser.Node.ListMode;


/**
 * Utility class to set the list mode of all nodes to certain values.
 *
 * @author TT
 */
public final class ListModeUtil
{

  private ListModeUtil()
  {
    // no instances needed
  }

  /**
   * Changes the list mode to show all packages in class directories with classes in it and archive files for
   * all other stuff.
   *
   * @param root
   */
  public static void showJarsAndOwnPackages(ContainerNode root)
  {
    root.walkCompleteSubTree().forEach(n -> n.setListMode(n.getSimpleName().startsWith("jar:")
      ? ListMode.COLLAPSED : ListMode.LEAFS_COLLAPSED));
  }

  /**
   * Changes the list mode to show all classes found in directories and archive files for all other stuff.
   *
   * @param root
   */
  public static void showJarsAndOwnClasses(ContainerNode root)
  {
    root.walkCompleteSubTree().forEach(n -> n.setListMode(n.getSimpleName().startsWith("jar:")
      ? ListMode.COLLAPSED : ListMode.EXPANDED));
  }

  /**
   * Changes the list mode to show only top level resources.
   * 
   * @param root
   */
  public static void showResourcesOnly(ContainerNode root)
  {
    root.walkCompleteSubTree().forEach(n -> n.setListMode(ListMode.COLLAPSED));
  }
}
