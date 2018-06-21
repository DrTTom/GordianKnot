package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import de.tautenhahn.dependencies.reports.TestCyclicDependencies;
import de.tautenhahn.dependencies.rest.DisplayableDiGraph.VisNode;
import de.tautenhahn.dependencies.rest.presentation.DisplayableClasspathEntry;


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
    ProjectView systemUnderTest = new ProjectView(CLASSPATH, "GordianKnot");
    assertThat("some node", systemUnderTest.getNodeInfo("0"), notNullValue());
    assertThat("some arc", systemUnderTest.getArcInfo("0-1"), notNullValue());
    List<DisplayableClasspathEntry> shownPath = systemUnderTest.getClassPath();
    assertThat("class path element", shownPath.get(0).getFullPath(), containsString("build/classes"));
    assertTrue("class path element active", shownPath.get(0).isActive());
    assertThat("label", shownPath.get(0).getLabel(), not(is(shownPath.get(1).getLabel())));
    assertThat("report", systemUnderTest.getUnreferencedReport(), notNullValue());
    assertThat("name", systemUnderTest.getProjectName(), is("GordianKnot"));
    changeListMode(systemUnderTest, "reports", "COLLAPSE_PARENT");
    systemUnderTest.restrictToImpliedBy(0, false);
    systemUnderTest.showOnlyCycles();
    assertThat("filter names", systemUnderTest.listActiveFilters(), hasSize(2));
  }

  /**
   * Asserts that various filters can be applied.
   */
  @Test
  public void applyFilters()
  {
    assertThat("just creating a cycle to report", TestCyclicDependencies.class, notNullValue());
    ProjectView systemUnderTest = new ProjectView(CLASSPATH, "GordianKnot");
    changeListMode(systemUnderTest, "rest", "EXPANDED");
    changeListMode(systemUnderTest, "rest", "EXPANDED");
    systemUnderTest.showOnlyCycles();

    String result = new Server.JsonTransformer().render(systemUnderTest.getDisplayableGraph());
    assertThat("view result", result, containsString("reports"));
    assertThat("view result", result, not(containsString("JSonTransformer")));
    systemUnderTest.showAll();
    systemUnderTest.collapseAll();
    List<VisNode> nodes = systemUnderTest.getDisplayableGraph().getNodes();
    assertThat("resource nodes", nodes, hasSize(2));
    int numberMain = "main".equals(nodes.get(0).label) ? 0 : 1;
    systemUnderTest.restrictToImpliedBy(numberMain, true);
    nodes = systemUnderTest.getDisplayableGraph().getNodes();
    assertThat("elements needed by main", nodes, hasSize(1));
  }


  private void changeListMode(ProjectView view, String label, String mode)
  {
    view.getDisplayableGraph()
        .getNodes()
        .stream()
        .filter(n -> label.equals(n.label))
        .map(n -> n.id)
        .findAny()
        .ifPresent(id -> view.setListMode(Integer.parseInt(id), mode));
  }
}
