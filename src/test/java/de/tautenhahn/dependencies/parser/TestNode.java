package de.tautenhahn.dependencies.parser;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.parser.Node.ListMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

/**
 * Unit tests for several nodes.
 *
 * @author TT
 */
public class TestNode
{

    private static final String PKG_1 = "dir:example.de.tautenhahn.example";
    private static final String PKG_1_NESTED = PKG_1 + ".impl";
    private static final String PKG_2 = "someJar:.com.dummy";
    private static ContainerNode root;

    /**
     * Creates some dummy structure.
     */
    @BeforeAll
    public static void init()
    {
        root = ContainerNode.createRoot();
        for (String name : new String[] {PKG_1, PKG_1_NESTED, PKG_2})
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
        assertThat(node.getDisplayName()).as("display name").isEqualTo("de.tautenhahn.example");
        assertThat(node).as("inner node").isInstanceOf(ContainerNode.class);
        assertThat(node.getSimpleName()).as("simple name").isEqualTo("example");
        assertThat(node.getName()).isEqualTo(PKG_1);
        assertThat(node.toString()).startsWith(PKG_1);
        ClassNode dummy = (ClassNode) node.find("Dummy");
        assertThat(dummy.getName()).as("name of leaf").isEqualTo(PKG_1 + ".Dummy");
        assertThat(dummy.getClassName()).as("class name").isEqualTo("de.tautenhahn.example.Dummy");
        assertThat(dummy.getChildByName("egal")).as("child").isNull();
    }

    /**
     * Asserts that dependencies of non-collapsed nodes follow the direct children only while collapsed follow all
     * contained children. Having the setup, we check the generated predecessor relation as well.
     */
    @Test
    public void dependencies()
    {
        ClassNode one = (ClassNode) root.find(PKG_1 + ".Dummy");
        ClassNode nested = (ClassNode) root.find(PKG_1_NESTED + ".Dummy");
        ClassNode other = (ClassNode) root.find(PKG_2 + ".Dummy");
        ClassNode alien = root.createLeaf("alien");
        one.addSuccessor(other);
        nested.addSuccessor(alien);

        Node systemUnderTest = root.find(PKG_1);
        assertThat(systemUnderTest.walkSubTree().collect(Collectors.toList())).as("subtree expanded").hasSize(3);
        assertThat(systemUnderTest.hasOwnContent()).as("extended has own content").isFalse();
        systemUnderTest.setListMode(ListMode.LEAFS_COLLAPSED);
        assertThat(systemUnderTest.getSuccessors()).as("successors of package with collapsed classes").contains(other);
        assertThat(systemUnderTest.walkSubTree().collect(Collectors.toList())).as("subtree leafs collapsed").hasSize(2);
        assertThat(alien.getPredecessors()).as("direct predecessor").contains(nested);

        systemUnderTest.setListMode(ListMode.COLLAPSED);
        assertThat(systemUnderTest.getSuccessors())
            .as("successors of collapsed node")
            .containsExactlyInAnyOrder(other, alien);
        assertThat(systemUnderTest.walkSubTree().collect(Collectors.toList())).as("subtree of collapsed").isEmpty();
        assertThat(systemUnderTest.getParent().walkSubTree().collect(Collectors.toList()))
            .as("parents subtree")
            .hasSize(1);
        assertThat(alien.getPredecessors()).as("collapsed predecessor").contains(systemUnderTest);
    }

    /**
     * Checks whether the used method to expand many nodes at once really works.
     */
    @Test
    public void expandAll()
    {
        root.walkSubTree().forEach(n -> n.setListMode(ListMode.COLLAPSED));
        assertThat(root.walkSubTree().count()).as("number visible nodes").isEqualTo(2L);
        root.walkCompleteSubTree().forEach(n -> n.setListMode(ListMode.EXPANDED));
        assertThat(root.walkCompleteSubTree().count()).as("number visible nodes").isEqualTo(11L);
    }

    /**
     * Just checking the trivial stuff too.
     */
    @Test
    public void pairs()
    {
        Pair<String, Object> systemUnderTest = new Pair<>("a", this);
        assertThat(systemUnderTest).isEqualTo(systemUnderTest);
        assertThat(systemUnderTest).isNotEqualTo(systemUnderTest.getSecond());
        assertThat(systemUnderTest).isNotEqualTo(new Pair<>("b", "c"));
        assertThat(systemUnderTest.hashCode()).as("hash code").isEqualTo(new Pair<>("a", this).hashCode());
        assertThat(new Pair<>("a", "b").toString()).as("toString").isEqualTo("(a, b)");
    }
}
