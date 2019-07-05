package de.tautenhahn.dependencies.parser;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for creating names.
 *
 * @author TT
 */
public class TestUniqueNameTree
{

    /**
     * Asserts that the names are short as wanted and unique.
     */
    @Test
    public void createNames()
    {
        UniqueNameTree systemUnderTest = new UniqueNameTree();
        Path projectAMain = Paths.get("irgendwo", "projectA", "build", "main");
        Path projectATest = Paths.get("irgendwo", "projectA", "build", "test");
        Path projectBMain = Paths.get("irgendwo", "projectB", "build", "main");
        systemUnderTest.add(projectAMain);
        systemUnderTest.add(projectATest);
        systemUnderTest.add(projectBMain);

        Map<Path, String> names = systemUnderTest.createNames();
        assertThat(names.get(projectATest)).isEqualTo("test");
        assertThat(names.get(projectAMain)).isEqualTo("projectA_main");
    }

    /**
     * Asserts that in case of only one entry its name is not deleted.
     */
    @Test
    public void uniqueEntry()
    {
        UniqueNameTree systemUnderTest = new UniqueNameTree();
        Path projectAMain = Paths.get("irgendwo", "projectA", "build", "main");
        systemUnderTest.add(projectAMain);
        Map<Path, String> names = systemUnderTest.createNames();
        assertThat(names.get(projectAMain)).as("unique dir name").isEqualTo("main");
    }
}
