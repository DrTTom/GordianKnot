package de.tautenhahn.dependencies.parser;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;


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
    assertThat("unique dir name", names.get(projectATest), is("test"));
    assertThat("unique dir name", names.get(projectAMain), is("projectA_main"));
  }

}
