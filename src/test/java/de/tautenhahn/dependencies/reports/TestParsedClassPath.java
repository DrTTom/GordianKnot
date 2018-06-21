package de.tautenhahn.dependencies.reports;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import de.tautenhahn.dependencies.parser.ParsedClassPath;


/**
 * Unit test for {@link ParsedClassPath}. Requires Java 10 if independent class loader shall be checked.
 *
 * @author TT
 */
public class TestParsedClassPath
{

  /**
   * Obtain the current class path setting - useful if tests are included in a project.
   */
  @Test
  public void getClassPath()
  {
    assertThat("current class path", ParsedClassPath.getCurrentClassPath().getEntries(), not(empty()));
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
    assertThat("loaded class from outside current class path",
               alien.getDeclaredMethod("dummyMethod", String.class),
               not(nullValue()));
  }

  /**
   * Asserts that created class loader has no access to elements of current class path.
   */
  @Test(expected = ClassNotFoundException.class)
  public void loadClassNegative() throws ClassNotFoundException
  {
    assumeThat("java version", System.getProperty("java.version"), startsWith("10."));
    createAlienLoader().loadClass(this.getClass().getName());
  }

  private ClassLoader createAlienLoader()
  {
    Path otherClasses = Paths.get("src", "test", "resources", "alienClasses").toAbsolutePath();
    return new ParsedClassPath(otherClasses.toString()).createClassLoader();
  }


}
