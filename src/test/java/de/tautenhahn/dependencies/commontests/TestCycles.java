package de.tautenhahn.dependencies.commontests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.reports.CyclicDependencies;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit test for detecting cycles in the current product structure. Test depends on own project, make sure to call
 * gradle assemble before executing this test!
 *
 * @author TT
 */
public class TestCycles
{

    private static final Logger LOG = LoggerFactory.getLogger(TestCycles.class);

    /**
     * Asserts that your package dependencies do not have more cycles than expected. Hopefully, you expect 0, otherwise
     * override {@link #getAllowedPackageSeverity()}.
     */
    @Test
    public final void cyclicPackageDependencies()
    {
        Filter filter = new Filter();
        // Ingore the one cyclic dependency my project creates on purpose for testing:
        filter.addIgnoredClassName("de.tautenhahn.dependencies.rest.TestProjectView");
        ContainerNode root = ProjectCache.getScannedProject(ParsedClassPath.getCurrentClassPath(), filter);
        CyclicDependencies packageCycles = CyclicDependencies.findForPackages(root);
        LOG.info("Analyzed package dependencies \n{}", packageCycles);
        assertThat(packageCycles.getSeverity())
            .as("Number of arcs creating cyclic package dependencies")
            .isLessThanOrEqualTo(getAllowedPackageSeverity());
    }

    /**
     * Asserts that your created jars do not have cyclic dependencies. Otherwise, clean up the packing of your jars.
     * <br> This test is usually inactive, override {@link #getOwnJars()} to activate his test.
     *
     * @throws IOException
     */
    @Test
    public final void cyclicJarDependencies() throws IOException
    {
        String ownJars = getOwnJars();
        assumeTrue(ownJars.contains(":"), "test makes only sense if at least two jars are specified");
        ContainerNode root = ProjectCache.getScannedProject(new ParsedClassPath(ownJars), new Filter());
        CyclicDependencies packageCycles = CyclicDependencies.findForPackages(root);
        LOG.info("Analyzed jar dependencies of {} \n {}", ownJars, packageCycles);
        assertThat(packageCycles.getSeverity())
            .as("Number of arcs creating cyclic jar dependencies")
            .isLessThanOrEqualTo(getAllowedPackageSeverity());
    }

    /**
     * Override this value if you have some unavoidable cycle in your package dependencies or if your product has more
     * cycles than you can repair now and you want to prevent things from getting worse.
     */
    protected int getAllowedPackageSeverity()
    {
        return 0;
    }

    /**
     * Returns a path which contains all jars created by your application. Example implementation fits default gradle
     * project, override to get that path correctly.<br> Do not forget that your build system must build the jars before
     * running a test on them.
     *
     * @throws IOException
     */
    protected String getOwnJars() throws IOException
    {
        return String.join(":", Files
            .list(Paths.get("build", "libs"))
            .filter(p -> String.valueOf(p.getFileName()).matches(".*\\.[jwer]ar"))
            .map(Path::toAbsolutePath)
            .map(Path::toString)
            .toArray(String[]::new));
    }
}
