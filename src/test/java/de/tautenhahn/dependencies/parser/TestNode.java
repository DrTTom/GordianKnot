package de.tautenhahn.dependencies.parser;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tautenhahn.dependencies.parser.Node.ListMode;



/**
 * Unit tests for several nodes.
 *
 * @author TT
 */
public class TestNode
{

  private static ContainerNode root;

  private static final String PKG_1 = "dir:example.de.tautenhahn.example";

  private static final String PKG_1_NESTED = PKG_1 + ".impl";

  private static final String PKG_2 = "someJar:.com.dummy";

  /**
   * Creates some dummy structure.
   */
  @BeforeClass
  public static void init()
  {
    root = ContainerNode.createRoot();
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
    assertThat("display name", node.getDisplayName(), is("de.tautenhahn.example"));
    assertThat("inner node", node, instanceOf(ContainerNode.class));
    assertThat("simple name", node.getSimpleName(), equalTo("example"));
    assertThat("name", node.getName(), equalTo(PKG_1));
    assertThat("toString", node.toString(), startsWith("ContainerNode"));
    ClassNode dummy = (ClassNode)node.find("Dummy");
    assertThat("name of leaf", dummy.getName(), equalTo(PKG_1 + ".Dummy"));
    assertThat("class name", dummy.getClassName(), equalTo("de.tautenhahn.example.Dummy"));
    assertThat("child", dummy.getChildByName("egal"), nullValue());
  }


  /**
   * Asserts that dependencies of non-collapsed nodes follow the direct children only while collapsed follow
   * all contained children. Having the setup, we check the generated predecessor relation as well.
   */
  @Test
  public void dependencies()
  {
    ClassNode one = (ClassNode)root.find(PKG_1 + ".Dummy");
    ClassNode nested = (ClassNode)root.find(PKG_1_NESTED + ".Dummy");
    ClassNode other = (ClassNode)root.find(PKG_2 + ".Dummy");
    ClassNode alien = root.createLeaf("alien");
    one.addSuccessor(other);
    nested.addSuccessor(alien);

    Node systemUnderTest = root.find(PKG_1);
    assertThat("subtree expanded", systemUnderTest.walkSubTree().collect(Collectors.toList()), hasSize(3));
    assertFalse("extended has own content", systemUnderTest.hasOwnContent());
    systemUnderTest.setListMode(ListMode.LEAFS_COLLAPSED);
    assertThat("successors of non-collapsed node", systemUnderTest.getSuccessors(), contains(other));
    assertThat("subtree leafs collapsed",
               systemUnderTest.walkSubTree().collect(Collectors.toList()),
               hasSize(2));
    assertThat("direct predecessor", alien.getPredecessors(), contains(nested));

    systemUnderTest.setListMode(ListMode.COLLAPSED);
    assertThat("successors of collapsed node",
               systemUnderTest.getSuccessors(),
               containsInAnyOrder(other, alien));
    assertThat("subtree of collapsed", systemUnderTest.walkSubTree().collect(Collectors.toList()), empty());
    assertThat("parents subtree",
               systemUnderTest.getParent().walkSubTree().collect(Collectors.toList()),
               hasSize(1));
    assertThat("collapsed predecessor", alien.getPredecessors(), contains(systemUnderTest));
  }

  /**
   * Checks whether the used method to expand many nodes at once really works.
   */
  @SuppressWarnings("boxing")
  @Test
  public void expandAll()
  {
    root.walkSubTree().forEach(n -> n.setListMode(ListMode.COLLAPSED));
    assertThat("number visible nodes", root.walkSubTree().count(), is(2L));
    root.walkCompleteSubTree().forEach(n -> n.setListMode(ListMode.EXPANDED));
    assertThat("number visible nodes", root.walkCompleteSubTree().count(), is(11L));
  }

  /**
   * Just checking the trivial stuff too.
   */
  @SuppressWarnings("boxing")
  @Test
  public void pairs()
  {
    Pair<String, Object> systemUnderTest = new Pair<>("a", this);
    assertThat("equals to itself", systemUnderTest, equalTo(systemUnderTest));
    assertThat("equals to wrong type object", systemUnderTest, not(equalTo(systemUnderTest.getSecond())));
    assertThat("equals to different Pair", systemUnderTest, not(equalTo(new Pair<>("b", "c"))));
    assertThat("hash code", systemUnderTest.hashCode(), equalTo(new Pair<>("a", this).hashCode()));
    assertThat("string", new Pair<>("a", "b").toString(), equalTo("(a, b)"));
  }

}
