package de.tautenhahn.dependencies.commontests;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.ReferenceChecker;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;


/**
 * Unit tests to find unreferenced elements in your class path and own classes. Override the non-final methods
 * for fine-tuning these tests.
 * 
 * @author TT
 */
public class TestUnreferencedElements
{

  private final ParsedClassPath classpathTest = ParsedClassPath.getCurrentClassPath();
  // private ParsedClassPath classpathRuntime;
  // private ParsedClassPath classpathCompile;

  private final Filter filter = new Filter();
  // TODO: make test parameterized for different class pathes!

  /**
   * Asserts that there are no unused classes in your project.
   */
  @Test
  public final void checkClassUsage()
  {
    ContainerNode root = ProjectCache.getScannedProject(classpathTest, filter);
    ReferenceChecker checker = new ReferenceChecker(root, filter, classpathTest);
    checker.addKnownNeededClasses(getKnownEntryClassNames());
    List<String> unref = checker.getUnrefClasses()
                                .stream()
                                .map(ClassNode::getClassName)
                                .collect(Collectors.toList());
    assertThat("unreferenced classes", unref, empty());
  }

  /**
   * Returns an array of fully qualified names of entry classes of your project. You have to specify only
   * those classes which are not automatically recognized as entry classes.
   */
  protected String[] getKnownEntryClassNames()
  {
    return new String[0];
  }
}
