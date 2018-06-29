package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import de.tautenhahn.dependencies.reports.TestCyclicDependencies;
import de.tautenhahn.dependencies.rest.presentation.DisplayableClasspathEntry;
import de.tautenhahn.dependencies.rest.presentation.DisplayableDiGraph.VisNode;


/**
 * Unit test for the server. Requires that the server is not running in parallel.
 *
 * @author TT
 */
public class TestProjectView
{

  private static final String CLASSPATH = Paths.get("build", "classes", "java", "test")
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
    shownPath.get(0).setActive(false);
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
    int numberMain = "main".equals(nodes.get(0).getLabel()) ? 0 : 1;
    systemUnderTest.restrictToImpliedBy(numberMain, true);
    nodes = systemUnderTest.getDisplayableGraph().getNodes();
    assertThat("elements needed by main", nodes, hasSize(1));
    assertThat("different filters",
               ImpliedByNode.dependingOn("dir:test"),
               not(is(ImpliedByNode.requiredBy("dir:test"))));
    systemUnderTest.showOnlyCycles();

  }

  /**
   * Tests whether the visible nodes for an element are found in cases element itself is visible, parent is
   * collapsed or only children are visible.
   */
  @Test
  public void findRepresentingNodes()
  {
    ProjectView systemUnderTest = new ProjectView(CLASSPATH, "GordianKnot");
    String myNodeName = "dir:test." + getClass().getName();
    String packageNodeName = myNodeName.substring(0, myNodeName.lastIndexOf('.'));

    assertThat("labels representing this package",
               getLabels(systemUnderTest, packageNodeName),
               contains("rest"));
    assertThat("labels representing this class", getLabels(systemUnderTest, myNodeName), contains("rest"));
    List<String> labels = getLabels(systemUnderTest, "dir:test");
    assertThat("labels representing test directory", labels, hasItem("rest"));
    assertThat("labels representing test directory",
               labels,
               containsInAnyOrder("rest", "reports", "analyzers", "parser", "commontests"));
  }

  private List<String> getLabels(ProjectView systemUnderTest, String nodeName)
  {
    List<String> ids = systemUnderTest.getNodeIDs(nodeName);
    return systemUnderTest.getDisplayableGraph()
                          .getNodes()
                          .stream()
                          .filter(n -> ids.contains(n.getId()))
                          .map(VisNode::getLabel)
                          .collect(Collectors.toList());
  }

  private void changeListMode(ProjectView view, String label, String mode)
  {
    view.getDisplayableGraph()
        .getNodes()
        .stream()
        .filter(n -> label.equals(n.getLabel()))
        .map(n -> n.getId())
        .findAny()
        .ifPresent(id -> view.setListMode(Integer.parseInt(id), mode));
  }
}
