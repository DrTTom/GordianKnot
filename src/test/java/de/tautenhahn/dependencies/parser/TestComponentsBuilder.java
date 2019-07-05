package de.tautenhahn.dependencies.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

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
    @BeforeAll
    public static void readComponentsDesign() throws IOException
    {
        components = new ComponentsDesign(TestComponentsBuilder.class.getResource("/components.conf"));
    }

    /**
     * Check whether component definition is used.
     */
    @Test
    public void assignClassToComponent()
    {
        assertThat(components.getComponentName("de.tautenhahn.dependencies.analyzers.special.Irgendwas"))
            .as("subpackage")
            .isEqualTo("SpecialAnalyzers");
        assertThat(components.getComponentName("de.tautenhahn.dependencies.analyzers.Irgendwas")).as("base package").
            isEqualTo("Core");
    }

    /**
     * Checks that a tree with components can be build and contains necessary dependencies.
     */
    @Test
    public void buildTree()
    {
        ProjectScanner scanner = new ProjectScanner(new Filter());
        ParsedClassPath classPath = new ParsedClassPath(Paths.get("build", "classes", "java", "main").toString());
        ContainerNode root = scanner.scan(classPath);

        ComponentsBuilder systemUnderTest = new ComponentsBuilder(components);

        ContainerNode otherRoot = systemUnderTest.addComponents(root);
        // TODO: correct class name as soon as node types are sorted out
        Node a = getByName(otherRoot, "Core." + ComponentsBuilder.class.getName()).get();
        Node b = getByName(otherRoot, "Core." + ClassNode.class.getName()).get();
        assertThat(a.getSuccessors()).as("dependency").contains(b);
    }

    private Optional<Node> getByName(ContainerNode n, String name)
    {
        return n.walkCompleteSubTree().filter(l -> l.getName().equals(name)).findAny();
    }
}
