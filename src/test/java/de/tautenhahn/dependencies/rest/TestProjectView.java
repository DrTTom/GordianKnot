package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

import de.tautenhahn.dependencies.reports.TestCyclicDependencies;


/**
 * Unit test for the server. Requires that the server is not running in parallel.
 *
 * @author TT
 */
public class TestProjectView
{

  public static final String CLASSPATH = Paths.get("build", "classes", "java", "test")
                                              .toAbsolutePath()
                                              .toString()
                                         + ":"
                                         + Paths.get("build", "classes", "java", "main")
                                                .toAbsolutePath()
                                                .toString();

  /**
   * Asserts view elements can be obtained.
   */
  @Test
  public void getView()
  {
    assertThat("just creating a cycle to report", TestCyclicDependencies.class, notNullValue());
    ProjectView systemUnderTest = new ProjectView(CLASSPATH, "GordianKnot");
    changeListMode(systemUnderTest, "rest", "EXPANDED");
    changeListMode(systemUnderTest, "rest", "EXPANDED");
    systemUnderTest.showOnlyCycles();

    String result = new Server.JsonTransformer().render(systemUnderTest.getDisplayableGraph());
    assertThat("view result", result, containsString("TestServer"));
    assertThat("view result", result, not(containsString("JSonTransformer")));

    systemUnderTest.showAll();
    assertThat("some node", systemUnderTest.getNodeInfo("0"), notNullValue());
    assertThat("some arc", systemUnderTest.getArcInfo("0-1"), notNullValue());
    assertThat("name", systemUnderTest.getProjectName(), is("GordianKnot"));

  }

  private void changeListMode(ProjectView view, String label, String mode)
  {
    view.getDisplayableGraph()
        .getNodes()
        .stream()
        .filter(n -> "rest".equals(n.label))
        .map(n -> n.id)
        .findAny()
        .ifPresent(id -> view.setListMode(Integer.parseInt(id), "EXPANDED"));
  }
}
