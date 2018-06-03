package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node.ListMode;


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

    DiGraph ctx = new DiGraph(root);
    NodeInfo systemUnderTest = new NodeInfo(ctx.getAllNodes().get(0));
    assertThat("node name", systemUnderTest.getNodeName(), is("ear:dummy_1.jar:jarfile.somepackage.MyClass"));
    assertThat("type", systemUnderTest.getType(), is("class"));
    assertThat("name", systemUnderTest.getName(), is("somepackage.MyClass"));
    assertThat("resource type", systemUnderTest.getResourceType(), is("jar"));
    assertThat("resource name", systemUnderTest.getResourceName(), is("jarfile"));

    jar.setListMode(ListMode.COLLAPSED);
    ctx = new DiGraph(root);
    systemUnderTest = new NodeInfo(ctx.getAllNodes().get(0));
    assertThat("node name", systemUnderTest.getNodeName(), is("ear:dummy_1.jar:jarfile"));
    assertThat("type", systemUnderTest.getType(), is("jar"));
    assertThat("name", systemUnderTest.getName(), is("jarfile"));
    assertThat("resource type", systemUnderTest.getResourceType(), is("ear"));
    assertThat("resource name", systemUnderTest.getResourceName(), is("dummy_1"));

    ear.setListMode(ListMode.COLLAPSED);
    ctx = new DiGraph(root);
    systemUnderTest = new NodeInfo(ctx.getAllNodes().get(0));
    assertThat("node name", systemUnderTest.getNodeName(), is("ear:dummy_1"));
    assertThat("type", systemUnderTest.getType(), is("ear"));
    assertThat("name", systemUnderTest.getName(), is("dummy_1"));
    assertThat("resource type", systemUnderTest.getResourceType(), nullValue());
    assertThat("resource name", systemUnderTest.getResourceName(), nullValue());

  }
}
