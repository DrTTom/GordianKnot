package de.tautenhahn.dependencies.parser;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import de.tautenhahn.dependencies.parser.DependencyParser;


/**
 * Unit tests for finding out which classes are referenced by a given class.
 *
 * @author TT
 */
public class TestDepParser
{

  /**
   * Asserts that our tool lists the same dependencies as jdeps does. Classes referenced in inherited methods
   * are not listed. Either
   * <ul>
   * <li>accept that because computation of closures and cycles will not be affected</li>
   * <li>parse larger parts of the class to get missing references</li>
   * <li>use every string which fits the format and is not flagged a constant</li>
   * </ul>
   *
   * @throws IOException
   */
  @SuppressWarnings("boxing")
  @Test
  public void listDeps() throws IOException
  {
    Class<?> clazz = ConcurrentHashMap.class;
    long start = System.currentTimeMillis();
    Collection<String> jdepResult = callJdeps(clazz);
    long durationJDep = System.currentTimeMillis() - start;

    DependencyParser systemUnderTest = new DependencyParser();
    start = System.currentTimeMillis();
    try (InputStream classContent = clazz.getResourceAsStream(clazz.getSimpleName() + ".class"))
    {
      Collection<String> myResult = systemUnderTest.listDependencies(clazz.getName(), classContent);
      long myDuration = System.currentTimeMillis() - start;
      List<String> ignored = Arrays.asList("java/util/Enumeration", "java/util/Collection");
      jdepResult.stream()
                .map(this::extractClassName)
                .filter(Objects::nonNull)
                .filter(n -> !ignored.contains(n))
                .forEach(n -> assertTrue(n + " not listed", myResult.remove(n)));

      assertThat("listed deps not mentioned by jDeps",
                 myResult,
                 anyOf(empty(), contains("java/util/Collection")));
      assertThat("duration", myDuration, lessThanOrEqualTo(durationJDep / 20));
    }
  }

  /**
   * Assert that class names are taken from method descriptors.
   */
  @Test
  public void extractClassNames()
  {
    String input = "(Ljava/util/concurrent/ConcurrentHashMap$BulkTask<V>;III[Ljava/util/concurrent/ConcurrentHashMap$Node;Ljava/util/concurrent/ConcurrentHashMap$ReduceValuesTask;Ljava/util/function/BiFunction;)V";
    List<String> result = new ArrayList<>();
    DependencyParser systemUnderTest = new DependencyParser();
    systemUnderTest.addClassNames(input, result);
    assertThat(result,
               containsInAnyOrder("java/util/concurrent/ConcurrentHashMap$BulkTask",
                                  "java/util/concurrent/ConcurrentHashMap$Node",
                                  "java/util/concurrent/ConcurrentHashMap$ReduceValuesTask",
                                  "java/util/function/BiFunction"));
  }

  private String extractClassName(String line)
  {
    int first = line.indexOf(">") + 2;
    int end = line.indexOf(" ", first + 1);
    return first < 0 || end < 0 ? null : line.substring(first, end).replace(".", "/");
  }

  /**
   * Call jdeps for a copy of the class because with Java 10 it no longer accepts class names.
   *
   * @param clazz
   * @return process output.
   * @throws IOException
   */
  private Collection<String> callJdeps(Class<?> clazz) throws IOException
  {
    String javaHome = System.getenv("JAVA_HOME");
    String command = javaHome == null ? "jdeps" : Paths.get(javaHome, "bin", "jdeps").toString();
    Path tempfile = Paths.get("tempfile.class");
    try (InputStream ins = clazz.getResourceAsStream(clazz.getSimpleName() + ".class"))
    {
      Files.copy(ins, tempfile);
    }
    ProcessBuilder builder = new ProcessBuilder(command, "-v", tempfile.toAbsolutePath().toString());
    Process run = builder.start();
    Collection<String> result = new ArrayList<>();
    try (InputStream is = run.getInputStream();
      BufferedReader buff = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
    {
      String s = buff.readLine();
      while (s != null)
      {
        result.add(s);
        s = buff.readLine();
      }
    }
    Files.delete(tempfile);
    return result;
  }

}
