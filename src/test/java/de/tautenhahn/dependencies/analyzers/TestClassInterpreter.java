package de.tautenhahn.dependencies.analyzers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tautenhahn.dependencies.Main;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;


/**
 * Unit test for {@link ClassInterpreter}
 *
 * @author TT
 */
public class TestClassInterpreter
{

  /**
   * Make sure main classes can be recognized.
   */
  @Test
  public void mainClass()
  {
    ContainerNode root = ContainerNode.createRoot();
    ClassNode server = root.createLeaf(Main.class.getName());
    ClassNode my = root.createLeaf(TestClassInterpreter.class.getName());
    ClassInterpreter systemUnderTest = new ClassInterpreter();
    assertTrue("Main is main class", systemUnderTest.isRecognizedAsMainClass(server));
    assertFalse("and this class is not", systemUnderTest.isRecognizedAsMainClass(my));
  }

}
