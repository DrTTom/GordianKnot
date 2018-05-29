package de.tautenhahn.dependencies.reports;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.util.Collections;

import org.junit.Test;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import de.tautenhahn.dependencies.rest.TestServer;


/**
 * Unit test for finding cyclic package dependencies.
 *
 * @author TT
 */
public class TestCyclicDependencies
{

  /**
   * TODO: make it a real test
   */
  @Test
  public void report()
  {
    assertThat("just creating a cylcle to report", TestServer.class, notNullValue());
    ProjectScanner scanner = new ProjectScanner(new Filter());
    ContainerNode root = scanner.scan(Collections.singletonList(Paths.get("build",
                                                                          "classes",
                                                                          "java",
                                                                          "test")));
    assertThat("report",
               CyclicPackageDependencies.findFor(root).toString(),
               containsString("TestCyclicDependencies -> TestServer"));

  }
}
