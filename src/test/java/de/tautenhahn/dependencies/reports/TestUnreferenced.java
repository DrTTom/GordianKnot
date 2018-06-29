package de.tautenhahn.dependencies.reports;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
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
    ParsedClassPath classPath = ParsedClassPath.getCurrentClassPath();
    ContainerNode root = new ProjectScanner(filter).scan(classPath);
    Unreferenced systemUnderTest = Unreferenced.forProject(root, filter, classPath).withLimits(2, 2).create();
    String onlyClassUnsingGson = "Server$JsonTransformer";
    assertThat("report", systemUnderTest.toString(), containsString(onlyClassUnsingGson));
    assertThat("gson lib", systemUnderTest.getRarelyUsedJars().get(0).getNodeName(), startsWith("jar:"));
    assertThat("unref classes", systemUnderTest.getUnreferencedClasses(), empty());
    assertThat("unref jars", systemUnderTest.getUnreferencedJars(), not(nullValue()));
    assertThat("small contrib jars", systemUnderTest.getLittleUsedJars(), not(empty()));
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
