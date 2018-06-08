package de.tautenhahn.dependencies.reports;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.tautenhahn.dependencies.parser.ClassPathUtils;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ProjectScanner;


/**
 * Unit test for finding unreferenced stuff and libraries which possibly can be replaced.
 */
public class TestUnreferenced
{

  /**
   * Creates a report about current project.
   */
  @Test
  public void report()
  {
    ContainerNode root = new ProjectScanner(new Filter()).scan(ClassPathUtils.getClassPath());
    Unreferenced.ReportConfig cfg = new Unreferenced.ReportConfig();
    cfg.setLoader(Thread.currentThread().getContextClassLoader());
    Unreferenced systemUnderTest = new Unreferenced(root, cfg);
    String onlyClassUnsingGson = "Server$JsonTransformer";
    assertThat("report", systemUnderTest.toString(), containsString(onlyClassUnsingGson));
  }
}
