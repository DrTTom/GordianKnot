package de.tautenhahn.dependencies.core.analyzers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tautenhahn.dependencies.analyzers.ClassInterpreter;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.rest.Server;


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
    ClassNode server = root.createLeaf(Server.class.getName());
    ClassNode my = root.createLeaf(TestClassInterpreter.class.getName());
    ClassInterpreter systemUnderTest = new ClassInterpreter();
    assertTrue("server is main class", systemUnderTest.isRecognizedAsMainClass(server));
    assertFalse("and this class is not", systemUnderTest.isRecognizedAsMainClass(my));
  }

}
