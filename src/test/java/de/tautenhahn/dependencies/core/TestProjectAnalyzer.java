package de.tautenhahn.dependencies.core;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.tautenhahn.dependencies.core.Node.ListMode;


/**
 * Unit tests analyzing current build directory. If running in eclipse, make sure to call "gradle compileTest"
 * before.
 *
 * @author TT
 */
public class TestProjectAnalyzer
{

  /**
   * Asserts that this test is found when analyzing the current project.
   */
  @Test
  public void analyzeMe()
  {
    ProjectAnalyzer systemUnderTest = new ProjectAnalyzer();
    List<Path> classPath = Arrays.asList(Paths.get("build", "classes", "java", "main"),
                                         Paths.get("build", "classes", "java", "test"));
    InnerNode root = (InnerNode)systemUnderTest.analyze(classPath);
    Node myNode = root.find("test:.de.tautenhahn.dependencies.core.TestProjectAnalyzer");
    assertThat("Analyzer depends on",
               myNode.getSuccessors(),
               hasItem(root.find("main:.de.tautenhahn.dependencies.core.ProjectAnalyzer")));

    Node corePackage = root.find("main:.de.tautenhahn.dependencies.core");
    Node testCorePackage = root.find("main:.de.tautenhahn.dependencies.core");
    testCorePackage.setListMode(ListMode.LEAFS_COLLAPSED);
    assertThat("needs core package", corePackage.getPredecessors(), hasItem(testCorePackage));
  }

}
