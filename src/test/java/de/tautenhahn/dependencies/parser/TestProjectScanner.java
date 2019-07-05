package de.tautenhahn.dependencies.parser;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.parser.Node.ListMode;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

/**
 * Unit tests analyzing current build directory. If running in eclipse, make sure to call "gradle assemble" before.
 *
 * @author TT
 */
public class TestProjectScanner
{

    /**
     * Asserts that this test is found when analyzing the current project.
     */
    @Test
    public void analyzeMe()
    {
        ProjectScanner systemUnderTest = new ProjectScanner(new Filter());
        ParsedClassPath classPath = new ParsedClassPath(
            Paths.get("build", "classes", "java", "main").toString() + ":" + Paths
                .get("build", "classes", "java", "test")
                .toString());
        ContainerNode root = systemUnderTest.scan(classPath);

        Node testNode = root.find("dir:test." + TestProjectScanner.class.getName());
        Node scannerNode = root.find("dir:main." + ProjectScanner.class.getName());
        assertThat(testNode.getSuccessors()).as("This test class depends on").contains(scannerNode);

        Node corePackage = scannerNode.getParent();
        Node testCorePackage = testNode.getParent();
        testCorePackage.setListMode(ListMode.LEAFS_COLLAPSED);
        corePackage.setListMode(ListMode.LEAFS_COLLAPSED);
        assertThat(corePackage.getPredecessors()).as("needs core package").contains(testCorePackage);
        assertThat(testCorePackage.getDependencyReason(corePackage))
            .as("reason")
            .contains(new Pair<>(testNode, scannerNode));
    }

    /**
     * Checks whether parsing is fast enough to handle projects jar files.
     */
    @SuppressWarnings("boxing")
    @Test
    public void checkJars()
    {
        long startTime = System.currentTimeMillis();
        ProjectScanner systemUnderTest = new ProjectScanner(new Filter());

        ContainerNode root = systemUnderTest.scan(ParsedClassPath.getCurrentClassPath());
        assertThat(System.currentTimeMillis() - startTime).as("duration").isLessThan(5000L);

        String junitJar = "jar:junit-4_12_jar.";
        String hamcrestJar = "jar:hamcrest-all-1_3_jar.";
        Node assertNode = root.find(junitJar + "org.junit.Assert");
        Node matcherAssertNode = root.find(hamcrestJar + "org.hamcrest.MatcherAssert");
        assertThat(matcherAssertNode.getPredecessors()).as("predecessors").contains(assertNode);
    }
}
