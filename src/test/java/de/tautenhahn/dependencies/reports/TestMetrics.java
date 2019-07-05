package de.tautenhahn.dependencies.reports;

import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import org.junit.jupiter.api.Test;

/**
 * Unit test for getting metrics.
 *
 * @author TT
 */
public class TestMetrics
{

    /**
     * Creates metrics for minimal project and checks the values.
     */
    @Test
    public void computeMetrics()
    {
        ContainerNode root = ContainerNode.createRoot();
        ClassNode fromJar = root.createLeaf("jar:dummy.DummyClass");
        ClassNode classA = root.createLeaf("dir:source.de.tautenhahn.a.A");
        ClassNode classA1 = root.createLeaf("dir:source.de.tautenhahn.a.A1");
        ClassNode classB = root.createLeaf("dir:source.de.tautenhahn.b.B");
        for (int i = 0; i < 10; i++)
        {
            root.createLeaf("dir:source.de.tautenhahn.c" + i + ".Impl");
        }
        Metrics systemUnderTest = new Metrics(root, new Filter());
        assertThat(systemUnderTest.toString()).as("report string").
            contains("archives:            2.00       1.00       0.67");
        classA1.addSuccessor(classA);
        classA1.addSuccessor(fromJar);
        classA1.addSuccessor(classB);
        classA.addSuccessor(classB);
        systemUnderTest = new Metrics(root, new Filter());
        assertThat(systemUnderTest.worstElements.get(0)).as("worst classes").contains("dir:source.de.tautenhahn.a.A1");
    }
}
