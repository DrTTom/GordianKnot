package de.tautenhahn.dependencies.parser;

import static org.hamcrest.Matchers.hasItem;
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

    Node testNode = root.find("test:." + TestProjectScanner.class.getName());
    Node scannerNode = root.find("main:." + ProjectScanner.class.getName());
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

}
