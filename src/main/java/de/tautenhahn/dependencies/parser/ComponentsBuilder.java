package de.tautenhahn.dependencies.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


/**
 * Creates a modified project tree which contains component nodes.
 *
 * @author TT, WS
 */
public class ComponentsBuilder
{

  Map<String, String> componentByPackage = new TreeMap<>((a, b) -> b.length() - a.length());

  /**
   * Creates instance and parses component definition from given configuration file.
   *
   * @param conf
   * @throws IOException
   */
  public ComponentsBuilder(URL conf) throws IOException
  {
    try (InputStream ins = conf.openStream(); Scanner s = new Scanner(ins, "UTF-8"))
    {
      // TODO handle wrong input!
      String component = null;
      while (s.hasNextLine())
      {
        String line = s.nextLine().trim();
        if (line.isEmpty())
        {
          continue;
        }

        if (line.charAt(0) == '[')
        {
          component = line.substring(1, line.length() - 1);
        }
        else
        {
          componentByPackage.put(line, component);
        }
      }
    }
  }

  /**
   * Returns a new project tree with given classes where nodes directly in the root represent components.
   * Class nodes are assigned to the respective subtree, package nodes may be duplicated.
   *
   * @param root
   */
  public ContainerNode addComponents(ContainerNode root)
  {
    ContainerNode result = ContainerNode.createRoot();
    Map<ClassNode, ClassNode> copyByOriginalLeafs = new HashMap<>();
    root.walkCompleteSubTree()
        .filter(ClassNode.class::isInstance)
        .map(n -> (ClassNode)n)
        .forEach(l -> addToComponent(result, l, copyByOriginalLeafs));

    root.walkCompleteSubTree()
        .filter(ClassNode.class::isInstance)
        .map(n -> (ClassNode)n)
        .forEach(l -> copyDependencies(l, copyByOriginalLeafs));
    return result;
  }

  private void copyDependencies(ClassNode original, Map<ClassNode, ClassNode> copyByOrinalLeafs)
  {
    ClassNode myCopy = copyByOrinalLeafs.get(original);
    original.getSucLeafs().stream().map(copyByOrinalLeafs::get).forEach(s -> myCopy.addSuccessor(s));
  }

  private void addToComponent(ContainerNode result, ClassNode l, Map<ClassNode, ClassNode> copyByOrinalLeafs)
  {
    String className = l.getClassName();
    String component = getComponentName(className);
    ContainerNode componentNode = (ContainerNode)result.getChildByName(component);
    if (componentNode == null)
    {
      componentNode = result.createInnerChild(component);
    }
    ClassNode copy = componentNode.createLeaf(className);
    copyByOrinalLeafs.put(l, copy);
  }

  /**
   * Returns the name of the component for given class name.
   */
  String getComponentName(String className)
  {
    return componentByPackage.entrySet()
                             .stream()
                             .filter(e -> className.startsWith(e.getKey()))
                             .map(Map.Entry::getValue)
                             .findFirst()
                             .orElse("UNASSIGNED");
  }
}
