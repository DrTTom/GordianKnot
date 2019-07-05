package de.tautenhahn.dependencies.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit test for {@link ParsedClassPath}. Requires Java 10 if independent class loader shall be checked.
 *
 * @author TT
 */
public class TestParsedClassPath
{

    /**
     * Obtain the current class path setting - useful if tests are included in a project. Note that class path in IDE
     * may differ from the one in gradle.
     */
    @Test
    public void obtainCurrentClassPath()
    {
        ParsedClassPath systemUnderTest = ParsedClassPath.getCurrentClassPath();
        assertThat(systemUnderTest.getEntries()).as("current class path").isNotEmpty();
        assertThat(systemUnderTest.getArchives()).as("referenced jar files").isNotEmpty();
        assertThat(systemUnderTest.getSourceFolders()).as("referenced directories").isNotEmpty();
    }

    /**
     * Asserts that created class loader has access to elements of given class path.
     *
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void loadClassPositive() throws ClassNotFoundException, NoSuchMethodException, SecurityException
    {
        Class<?> alien = createAlienLoader().loadClass("Alien");
        assertThat(alien.getDeclaredMethod("dummyMethod", String.class))
            .as("loaded class from outside current class path")
            .isNotNull();
    }

    /**
     * Asserts that created class loader has no access to elements of current class path.
     */
    @Test
    public void loadClassNegative() throws ClassNotFoundException
    {
        //assumeThat("java version", System.getProperty("java.version"), startsWith("10."));
        assertThatThrownBy(() -> createAlienLoader().loadClass(this.getClass().getName())).isInstanceOf(
            ClassNotFoundException.class);
    }

    private ClassLoader createAlienLoader()
    {
        Path otherClasses = Paths.get("src", "test", "resources", "alienClasses").toAbsolutePath();
        return new ParsedClassPath(otherClasses.toString()).createClassLoader();
    }
}
