package de.tautenhahn.dependencies.reports;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import de.tautenhahn.dependencies.rest.TestProjectView;


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
  @SuppressWarnings("boxing")
  @Test
  public void packages()
  {
    assertThat("just creating a cylcle to report", TestProjectView.class, notNullValue());
    ProjectScanner scanner = new ProjectScanner(new Filter());
    ContainerNode root = scanner.scan(new ParsedClassPath(Paths.get("build", "classes", "java", "test")
                                                               .toString()));
    CyclicDependencies systemUnderTest = CyclicDependencies.findForPackages(root);
    assertThat("report",
               systemUnderTest.toString(),
               containsString("TestCyclicDependencies -> TestProjectView"));
    assertThat("severity", systemUnderTest.getSeverity(), equalTo(2));
  }

  /**
   * Assert that report works for jars. This test uses that references junit and hamcrest jar are not
   * separated properly.
   */
  @Test
  public void jars()
  {
    ProjectScanner scanner = new ProjectScanner(new Filter());
    ContainerNode root = scanner.scan(ParsedClassPath.getCurrentClassPath());
    assertThat("report",
               CyclicDependencies.findForJars(root).toString(),
               containsString("(org.junit.Assert -> org.hamcrest.Matcher"));
  }
}
