package de.tautenhahn.dependencies.reports;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import de.tautenhahn.dependencies.rest.TestProjectView;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

/**
 * Unit test for finding cyclic package dependencies.
 *
 * @author TT
 */
public class TestCyclicDependencies
{

    /**
     * Assert that report mentions causing classes.
     */
    @Test
    public void packages()
    {
        assertThat(TestProjectView.class).isNotNull();
        ProjectScanner scanner = new ProjectScanner(new Filter());
        ContainerNode root =
            scanner.scan(new ParsedClassPath(Paths.get("build", "classes", "java", "test").toString()));
        CyclicDependencies systemUnderTest = CyclicDependencies.findForPackages(root);
        assertThat(systemUnderTest.toString()).as("report").contains("TestCyclicDependencies -> TestProjectView");
        assertThat(systemUnderTest.getSeverity()).as("severity").isEqualTo(2);
    }

    /**
     * Assert that report works for jars. This test uses that references junit and hamcrest jar are not separated
     * properly.
     */
    @Test
    public void jars()
    {
        ProjectScanner scanner = new ProjectScanner(new Filter());
        ContainerNode root = scanner.scan(ParsedClassPath.getCurrentClassPath());
        assertThat(CyclicDependencies.findForJars(root).toString()).isEmpty();
    }
}
