package de.tautenhahn.dependencies.reports;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.slf4j.simple.SimpleLoggerConfiguration;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
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
    Filter filter = new Filter();
    filter.addIgnoredClassName(".*\\.Alien");
    ContainerNode root = new ProjectScanner(filter).scan(ParsedClassPath.getCurrentClassPath());
    Unreferenced.ReportConfig cfg = new Unreferenced.ReportConfig();
    cfg.addNeededElements("org\\.eclipse\\.jdt\\.internal.*");
    cfg.setLoader(Thread.currentThread().getContextClassLoader());
    Unreferenced systemUnderTest = new Unreferenced(root, cfg);
    String onlyClassUnsingGson = "Server$JsonTransformer";
    assertThat("report", systemUnderTest.toString(), containsString(onlyClassUnsingGson));
    // TODO:
    // System.out.println(systemUnderTest.getUnreferencedClasses());

    // assertThat("unref classes", systemUnderTest.getUnreferencedClasses(), empty());
    // assertThat("unref jars", systemUnderTest.getUnreferencedJars(), empty());
  }

  /**
   * Application needs slf4j-simple at runtime but source code does not. Referencing one class from that jar
   * to get a "contributes too few classes" warning.
   */
  public SimpleLoggerConfiguration getJustADummy()
  {
    return null;
  }
}
