package de.tautenhahn.dependencies.reports;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import org.junit.jupiter.api.Test;

/**
 * Unit test for finding missing class references.
 *
 * @author TT
 */
public class TestMissingClasses
{

    /**
     * Assert that report looks OK.
     */
    @Test
    public void packages()
    {
        Filter filter = new Filter();
        ProjectScanner scanner = new ProjectScanner(filter);
        ContainerNode root = scanner.scan(ParsedClassPath.getCurrentClassPath());
        MissingClasses systemUnderTest = new MissingClasses(root, filter);

        assertThat(systemUnderTest.toString()).as("report for current project with high test coverage").isBlank();
    }
}
