package de.tautenhahn.dependencies.rest.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Node.ListMode;
import org.junit.jupiter.api.Test;

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

        checkFirstNode(root, "ear:dummy_1.jar:jarfile.somepackage.MyClass", "class", "somepackage.MyClass", "jar",
            "jarfile");

        jar.setListMode(ListMode.COLLAPSED);
        checkFirstNode(root, "ear:dummy_1.jar:jarfile", "jar", "jarfile", "ear", "dummy_1");

        ear.setListMode(ListMode.COLLAPSED);
        NodeInfo systemUnderTest = checkFirstNode(root, "ear:dummy_1", "ear", "dummy_1", null, null);
        assertThat(systemUnderTest.getNumberExpandable()).as("expandable").isEqualTo(1);
        assertThat(systemUnderTest.getNumberCollapsable()).as("collapsable").isEqualTo(1);
        assertThat(systemUnderTest.getNumberContainedClasses()).as("classes").isEqualTo(1);
        assertThat(systemUnderTest.getListMode()).as("mode").isEqualTo("COLLAPSED");
    }

    /**
     * Gets an arc information.
     */
    @Test
    public void testArcs()
    {
        ContainerNode root = ContainerNode.createRoot();
        ClassNode c1 = root.createLeaf("dir:none.de.dummy.Class1");
        ClassNode c2 = root.createLeaf("dir:none.de.dummy.Class2");
        c1.addSuccessor(c2);
        DiGraph ctx = new DiGraph(root);
        ArcInfo systemUnderTest = new ArcInfo(ctx, "0-1");
        assertThat(systemUnderTest.getFrom().getName()).as("from").isEqualTo("de.dummy.Class1");
        assertThat(systemUnderTest.getTo().getName()).as("to").isEqualTo("de.dummy.Class2");
        assertThat(systemUnderTest.getReason()).as("reason").isNotEmpty();
    }

    private NodeInfo checkFirstNode(ContainerNode root, String nodeName, String type, String name, String resourceType,
                                    String resourceName)
    {
        DiGraph ctx = new DiGraph(root);
        NodeInfo systemUnderTest = new NodeInfo(ctx.getAllNodes().get(0));
        assertThat(systemUnderTest.getNodeName()).as("node name").isEqualTo(nodeName);
        assertThat(systemUnderTest.getType()).as("type").isEqualTo(type);
        assertThat(systemUnderTest.getName()).as("name").isEqualTo(name);
        assertThat(systemUnderTest.getResourceType()).as("resource type").isEqualTo(resourceType);
        assertThat(systemUnderTest.getResourceName()).as("resource name").isEqualTo(resourceName);
        return systemUnderTest;
    }
}
