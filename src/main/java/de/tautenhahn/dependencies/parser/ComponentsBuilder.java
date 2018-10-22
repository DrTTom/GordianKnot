package de.tautenhahn.dependencies.parser;

import java.util.HashMap;
import java.util.Map;


/**
 * Creates a modified project tree which contains component nodes.
 *
 * @author TT, WS
 */
public class ComponentsBuilder
{

  private final ComponentsDesign comp;

  /**
   * Creates instance and parses component definition from given configuration file.
   *
   * @param comp
   */
  public ComponentsBuilder(ComponentsDesign comp)
  {
    this.comp = comp;
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
    return comp.getComponentName(className);
  }
}
