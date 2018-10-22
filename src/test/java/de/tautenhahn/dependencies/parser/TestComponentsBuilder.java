package de.tautenhahn.dependencies.parser;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tautenhahn.dependencies.reports.ArchitecturalMatch;


/**
 * Unit test for creating a component based project tree.
 *
 * @author TT, WS
 */
public class TestComponentsBuilder
{

  private static ComponentsDesign components;

  /**
   * Reads some components assumptions about current project.
   *
   * @throws IOException
   */
  @BeforeClass
  public static void readComponentsDesign() throws IOException
  {
    components = new ComponentsDesign(TestComponentsBuilder.class.getResource("/components.conf"));
  }

  /**
   * Check whether component definition is used.
   *
   * @throws IOException
   */
  @Test
  public void assignClassToComponent() throws IOException
  {
    assertThat("subpackage",
               components.getComponentName("de.tautenhahn.dependencies.parser.Irgendwas"),
               is("Core"));
    assertThat("base package", components.getComponentName("de.tautenhahn.Irgendwas"), is("Utils"));
  }


  /**
   * Checks that a tree with components can be build and contains necessary dependencies.
   */
  @Test
  public void buildTree() throws IOException
  {
    ProjectScanner scanner = new ProjectScanner(new Filter());
    ParsedClassPath classPath = new ParsedClassPath(Paths.get("build", "classes", "java", "main").toString());
    ContainerNode root = scanner.scan(classPath);

    ComponentsBuilder systemUnderTest = new ComponentsBuilder(components);

    ContainerNode otherRoot = systemUnderTest.addComponents(root);
    // TODO: correct class name as soon as node types are sorted out
    Node a = getByName(otherRoot, "Core." + ComponentsBuilder.class.getName()).get();
    Node b = getByName(otherRoot, "Core." + ClassNode.class.getName()).get();
    assertThat("dependency", a.getSuccessors(), hasItem(b));

    System.out.println(new ArchitecturalMatch(components, root));
  }

  private Optional<Node> getByName(ContainerNode n, String name)
  {
    return n.walkCompleteSubTree().filter(l -> l.getName().equals(name)).findAny();
  }

}