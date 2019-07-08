package de.tautenhahn.dependencies.reports;

import de.tautenhahn.dependencies.parser.ComponentsDesign;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import de.tautenhahn.dependencies.parser.TestComponentsBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Checks components report about this project.
 *
 * @author TT
 */
public class TestArchitecturalMatch
{

    /**
     * TODO
     */
    @Test
    public void smoke() throws IOException
    {
        ComponentsDesign assumption = new ComponentsDesign(TestComponentsBuilder.class.getResource("/components.conf"));
        ProjectScanner scanner = new ProjectScanner(new Filter());
        ContainerNode root = scanner.scan(ParsedClassPath.getCurrentClassPath());

        ArchitecturalMatch systemUnderTest = new ArchitecturalMatch(assumption, root);
        systemUnderTest.toString();
    }
}
