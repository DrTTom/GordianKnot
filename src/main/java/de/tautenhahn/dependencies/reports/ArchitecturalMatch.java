package de.tautenhahn.dependencies.reports;

import de.tautenhahn.dependencies.parser.ComponentsBuilder;
import de.tautenhahn.dependencies.parser.ComponentsDesign;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;


/**
 * Matches the real components and their dependency against architectural assumptions. Differences may
 * indicate security problems in addition to messy structure.
 *
 * @author TT
 */
class ArchitecturalMatch
{

  // private final ComponentsDesign components;

  private final ContainerNode root;

  /**
   * Creates new instance, performing the necessary analysis.
   *
   * @param components wanted components
   * @param root project as initially parsed, will be copied, not changed.
   */
  public ArchitecturalMatch(ComponentsDesign components, ContainerNode root)
  {
    // this.components = components;
    this.root = new ComponentsBuilder(components).addComponents(root);
    this.root.getChildren().forEach(n -> n.setListMode(ListMode.COLLAPSED));
  }

  /**
   * Returns the text report, currently only the findings. TODO: add some interpretation!
   */
  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    result.append("#### COMPONENTS ####\n\n");
    root.getChildren().forEach(n -> result.append(n).append("\n\n"));

    result.append("#### INTERFACES ####\n\n");
    for ( Node c1 : root.getChildren() )
    {
      for ( Node c2 : c1.getSuccessors() )
      {
        result.append(c1.getSimpleName()).append(" -> ").append(c2.getSimpleName()).append('\n');
        c1.explainDependencyTo(c2).forEach(p -> result.append("   ").append(p).append('\n'));
        result.append('\n');
      }
    }
    return result.toString();
  }
}
