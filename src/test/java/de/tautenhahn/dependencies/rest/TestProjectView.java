package de.tautenhahn.dependencies.rest;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.reports.TestCyclicDependencies;
import de.tautenhahn.dependencies.rest.presentation.DisplayableClasspathEntry;
import de.tautenhahn.dependencies.rest.presentation.DisplayableDiGraph.VisNode;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit test for the server. Requires that the server is not running in parallel.
 *
 * @author TT
 */
public class TestProjectView
{

    private static final String CLASSPATH =
        Paths.get("build", "classes", "java", "test").toAbsolutePath().toString() + ":" + Paths
            .get("build", "classes", "java", "main")
            .toAbsolutePath()
            .toString();

    /**
     * Asserts view elements can be obtained.
     */
    @Test
    public void checkElements()
    {
        ProjectView systemUnderTest = new ProjectView(CLASSPATH, "GordianKnot");
        assertThat(systemUnderTest.getNodeInfo("0")).as("some node").isNotNull();
        assertThat(systemUnderTest.getArcInfo("0-1")).as("some arc").isNotNull();
        List<DisplayableClasspathEntry> shownPath = systemUnderTest.getClassPath();
        assertThat(shownPath.get(0).getFullPath()).as("full class path").contains("build/classes");
        assertThat(shownPath.get(0).isActive()).as("class path element active").isTrue();
        shownPath.get(0).setActive(false);
        assertThat(shownPath.get(0).getLabel()).as("label").isNotEqualTo(shownPath.get(1).getLabel());
        assertThat(systemUnderTest.getUnreferencedReport()).as("report").isNotNull();
        assertThat(systemUnderTest.getProjectName()).as("name").isEqualTo("GordianKnot");
        changeListMode(systemUnderTest, "reports", "COLLAPSE_PARENT");
        systemUnderTest.restrictToImpliedBy(0, false);
        systemUnderTest.showOnlyCycles();
        assertThat(systemUnderTest.listActiveFilters()).as("filter names").hasSize(2);
    }

    /**
     * Asserts that various filters can be applied.
     */
    @Test
    public void applyFilters()
    {
        assertThat(TestCyclicDependencies.class).as("just creating a cycle to report").isNotNull();
        ProjectView systemUnderTest = new ProjectView(CLASSPATH, "GordianKnot");
        changeListMode(systemUnderTest, "rest", "EXPANDED");
        changeListMode(systemUnderTest, "rest", "EXPANDED");
        systemUnderTest.showOnlyCycles();

        String result = new Server.JsonTransformer().render(systemUnderTest.getDisplayableGraph());
        assertThat(result).as("view result").contains("reports").doesNotContain("JSonTransformer");
        systemUnderTest.showAll();
        systemUnderTest.collapseAll();
        List<VisNode> nodes = systemUnderTest.getDisplayableGraph().getNodes();
        assertThat(nodes).as("resource nodes").hasSize(2);
        int numberMain = "main".equals(nodes.get(0).getLabel()) ? 0 : 1;
        systemUnderTest.restrictToImpliedBy(numberMain, true);
        nodes = systemUnderTest.getDisplayableGraph().getNodes();
        assertThat(nodes).as("elements needed by main").hasSize(1);
        assertThat(ImpliedByNode.dependingOn("dir:test"))
            .as("different filters")
            .isNotEqualTo(ImpliedByNode.requiredBy("dir:test"));
        systemUnderTest.showOnlyCycles();
    }

    /**
     * Tests whether the visible nodes for an element are found in cases element itself is visible, parent is collapsed
     * or only children are visible.
     */
    @Test
    public void findRepresentingNodes()
    {
        ProjectView systemUnderTest = new ProjectView(CLASSPATH, "GordianKnot");
        String myNodeName = "dir:test." + getClass().getName();
        String packageNodeName = myNodeName.substring(0, myNodeName.lastIndexOf('.'));

        assertThat(getLabels(systemUnderTest, packageNodeName)).as("labels representing this package").contains("rest");
        assertThat(getLabels(systemUnderTest, myNodeName)).as("labels representing this class").contains("rest");
        List<String> labels = getLabels(systemUnderTest, "dir:test");
        assertThat(labels).as("labels representing test directory").
            containsExactlyInAnyOrder("rest", "reports", "analyzers", "parser", "commontests", "dependencies",
                "presentation");
    }

    private List<String> getLabels(ProjectView systemUnderTest, String nodeName)
    {
        List<String> ids = systemUnderTest.getNodeIDs(nodeName);
        return systemUnderTest
            .getDisplayableGraph()
            .getNodes()
            .stream()
            .filter(n -> ids.contains(n.getId()))
            .map(VisNode::getLabel)
            .collect(Collectors.toList());
    }

    private void changeListMode(ProjectView view, String label, String mode)
    {
        view
            .getDisplayableGraph()
            .getNodes()
            .stream()
            .filter(n -> label.equals(n.getLabel()))
            .map(VisNode::getId)
            .findAny()
            .ifPresent(id -> view.changeListMode(Integer.parseInt(id), mode));
    }
}
