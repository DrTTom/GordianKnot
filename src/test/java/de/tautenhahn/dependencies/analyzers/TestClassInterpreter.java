package de.tautenhahn.dependencies.analyzers;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.Main;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import org.junit.jupiter.api.Test;

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
        assertThat(systemUnderTest.isRecognizedAsMainClass(server)).as("Main is main class").isTrue();
        assertThat(systemUnderTest.isRecognizedAsMainClass(my)).as("and this class is not").isFalse();
    }
}
