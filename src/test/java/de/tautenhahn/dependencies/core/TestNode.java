package de.tautenhahn.dependencies.core;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;



/**
 * Unit tests for several nodes.
 * 
 * @author TT
 */
public class TestNode
{

  private static InnerNode root;

  private static final String PKG_1 = "example:.de.tautenhahn.example";

  private static final String PKG_1_NESTED = PKG_1 + ".impl";

  private static final String PKG_2 = "someJar:.com.dummy";

  @BeforeClass
  public static void init()
  {
    root = InnerNode.createRoot();
    for ( String name : new String[]{PKG_1, PKG_1_NESTED, PKG_2} )
    {
      root.createInnerChild(name).createLeaf("Dummy");
    }
  }

  /**
   * Asserts that nodes can be addressed by using fully qualified names.
   */
  @Test
  public void useNames()
  {
    Node node = root.find(PKG_1);
    assertThat("inner node", node, instanceOf(InnerNode.class));
    assertThat("simple name", node.getSimpleName(), equalTo("example"));
    assertThat("name", node.getName(), equalTo(PKG_1));
    assertThat("toString", node.toString(), startsWith("InnerNode"));
    assertThat("name of leaf", node.find("Dummy").getName(), equalTo(PKG_1 + ".Dummy"));
  }


  /**
   * Asserts that dependencies of non-collapsed nodes follow the direct children only while collapsed follow
   * all contained children. Having the setup, we check the generated predecessor relation as well.
   */
  @Test
  public void dependencies()
  {
    Leaf one = (Leaf)root.find(PKG_1 + ".Dummy");
    Leaf nested = (Leaf)root.find(PKG_1_NESTED + ".Dummy");
    Leaf other = (Leaf)root.find(PKG_2 + ".Dummy");
    Leaf alien = root.createLeaf("alien");
    one.addSuccessor(other);
    nested.addSuccessor(alien);

    Node systemUnderTest = root.find(PKG_1);
    assertThat("successors of non-collapsed node", systemUnderTest.getSuccessors(), contains(other));
    assertThat("subtree", systemUnderTest.walkSubTree().collect(Collectors.toList()), hasSize(3));
    assertThat("direct predecessor", alien.getPredecessors(), contains(nested));

    systemUnderTest.setCollapsed(true);
    assertThat("successors of collapsed node", systemUnderTest.getSuccessors(), contains(other, alien));
    assertThat("subtree of collapsed", systemUnderTest.walkSubTree().collect(Collectors.toList()), empty());
    assertThat("parents subtree",
               systemUnderTest.getParent().walkSubTree().collect(Collectors.toList()),
               hasSize(1));
    assertThat("collapsed predecessor", alien.getPredecessors(), contains(systemUnderTest));
  }

}
