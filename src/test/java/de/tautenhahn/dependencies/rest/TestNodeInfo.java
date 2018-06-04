package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node.ListMode;


/**
 * Unit tests for describing a node.
 *
 * @author TT
 */
public class TestNodeInfo
{

  /**
   * Sorry for complicated setup, access level of API elements is designed for application, not for this test.
   */
  @Test
  public void parseNames()
  {
    ContainerNode root = ContainerNode.createRoot();
    ContainerNode ear = root.createInnerChild("ear:dummy_1");
    ContainerNode jar = ear.createInnerChild("jar:jarfile");
    ContainerNode pkg = jar.createInnerChild("somepackage");
    pkg.createLeaf("MyClass");

    checkFirstNode(root,
                   "ear:dummy_1.jar:jarfile.somepackage.MyClass",
                   "class",
                   "somepackage.MyClass",
                   "jar",
                   "jarfile");

    jar.setListMode(ListMode.COLLAPSED);
    checkFirstNode(root, "ear:dummy_1.jar:jarfile", "jar", "jarfile", "ear", "dummy_1");

    ear.setListMode(ListMode.COLLAPSED);
    checkFirstNode(root, "ear:dummy_1", "ear", "dummy_1", null, null);
  }

  private void checkFirstNode(ContainerNode root,
                              String nodeName,
                              String type,
                              String name,
                              String resourceType,
                              String resourceName)
  {
    DiGraph ctx = new DiGraph(root);
    NodeInfo systemUnderTest = new NodeInfo(ctx.getAllNodes().get(0));
    assertThat("node name", systemUnderTest.getNodeName(), is(nodeName));
    assertThat("type", systemUnderTest.getType(), is(type));
    assertThat("name", systemUnderTest.getName(), is(name));
    assertThat("resource type", systemUnderTest.getResourceType(), is(resourceType));
    assertThat("resource name", systemUnderTest.getResourceName(), is(resourceName));
  }
}
