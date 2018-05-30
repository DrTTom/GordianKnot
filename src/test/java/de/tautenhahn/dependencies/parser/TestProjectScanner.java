package de.tautenhahn.dependencies.parser;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.tautenhahn.dependencies.parser.Node.ListMode;


/**
 * Unit tests analyzing current build directory. If running in eclipse, make sure to call "gradle assemble"
 * before.
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
    List<Path> classPath = Arrays.asList(Paths.get("build", "classes", "java", "main"),
                                         Paths.get("build", "classes", "java", "test"));
    ContainerNode root = systemUnderTest.scan(classPath);

    Node testNode = root.find("dir:test." + TestProjectScanner.class.getName());
    Node scannerNode = root.find("dir:main." + ProjectScanner.class.getName());
    assertThat("This test class depends on", testNode.getSuccessors(), hasItem(scannerNode));

    Node corePackage = scannerNode.getParent();
    Node testCorePackage = testNode.getParent();
    testCorePackage.setListMode(ListMode.LEAFS_COLLAPSED);
    corePackage.setListMode(ListMode.LEAFS_COLLAPSED);
    assertThat("needs core package", corePackage.getPredecessors(), hasItem(testCorePackage));
    assertThat("reason",
               testCorePackage.getDependencyReason(corePackage),
               hasItem(new Pair<>(testNode, scannerNode)));
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
    List<Path> classPath = ClassPathUtils.getClassPath();
    // classPath = Collections.singletonList(classPath.get(3));
    ContainerNode root = systemUnderTest.scan(classPath);
    String junitJar = "jar:junit-4_12_jar.";
    String hamcrestJar = "jar:hamcrest-all-1_3_jar.";
    Node assertNode = root.find(junitJar + "org.junit.Assert");
    Node matcherAssertNode = root.find(hamcrestJar + "org.hamcrest.MatcherAssert");
    assertThat("predecessors", matcherAssertNode.getPredecessors(), hasItem(assertNode));
    assertThat("duration", System.currentTimeMillis() - startTime, lessThan(5000L));
  }

}
