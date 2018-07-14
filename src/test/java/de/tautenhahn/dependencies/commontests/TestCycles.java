package de.tautenhahn.dependencies.commontests;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import de.tautenhahn.dependencies.reports.CyclicDependencies;


/**
 * Unit test for detecting cycles in the current product structure. Test depends on own project, make sure to
 * call gradle assemble before executing this test!
 * 
 * @author TT
 */
public class TestCycles
{

  private static final Logger LOG = LoggerFactory.getLogger(TestCycles.class);

  /**
   * Asserts that your package dependencies do not have more cycles than expected. Hopefully, you expect 0,
   * otherwise override {@link #getAllowedPackageSeverity()}.
   */
  @SuppressWarnings("boxing")
  @Test
  public void cyclicPackageDependencies()
  {
    Filter filter = new Filter();
    // Ingore the one cyclic dependency my project creates on purpose for testing:
    filter.addIgnoredClassName("de.tautenhahn.dependencies.rest.TestProjectView");
    ContainerNode root = new ProjectScanner(filter).scan(ParsedClassPath.getCurrentClassPath());
    CyclicDependencies packageCycles = CyclicDependencies.findForPackages(root);
    LOG.info("Analyzed package dependencies \n{}", packageCycles);
    assertThat("Number of arcs creating cyclic package dependencies",
               packageCycles.getSeverity(),
               lessThanOrEqualTo(getAllowedPackageSeverity()));
  }

  /**
   * Asserts that your created jars do not have cyclic dependencies. Otherwise, clean up the packing of your
   * jars. <br>
   * This test is usually inactive, override {@link #getOwnJars()} to activate his test.
   * 
   * @throws IOException
   */
  @SuppressWarnings("boxing")
  @Test
  public void cyclicJarDependencies() throws IOException
  {
    String ownJars = getOwnJars();
    assumeTrue("test makes only sense if at least two jars are specified", ownJars.contains(":"));
    ContainerNode root = new ProjectScanner(new Filter()).scan(new ParsedClassPath(ownJars));
    CyclicDependencies packageCycles = CyclicDependencies.findForPackages(root);
    LOG.info("Analyzed jar dependencies of {} \n {}", ownJars, packageCycles);
    assertThat("Number of arcs creating cyclic package dependencies",
               packageCycles.getSeverity(),
               lessThanOrEqualTo(getAllowedPackageSeverity()));
  }

  /**
   * Override this value if you have some unavoidable cycle in your package dependencies or if your product
   * has more cycles than you can repair now and you want to prevent things from getting worse.
   */
  protected int getAllowedPackageSeverity()
  {
    return 0;
  }

  /**
   * Returns a path which contains all jars created by your application. Example implementation fits default
   * gradle project, override to get that path correctly.<br>
   * Do not forget that your build system must build the jars before running a test on them.
   * 
   * @throws IOException
   */
  protected String getOwnJars() throws IOException
  {
    return String.join(":",
                       Files.list(Paths.get("build", "libs"))
                            .filter(p -> p.getFileName().toString().matches(".*\\.[jwer]ar"))
                            .map(Path::toAbsolutePath)
                            .map(Path::toString)
                            .toArray(String[]::new));
  }

}
