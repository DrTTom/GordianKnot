package de.tautenhahn.dependencies.core;

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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;


/**
 * Unit tests for finding out which classes are referenced by a given class.
 *
 * @author TT
 */
public class TestDepParser
{

  /**
   * Assert that our tool list same dependencies as jdeps does.
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

    DepParser systemUnderTest = new DepParser();
    start = System.currentTimeMillis();
    try (InputStream classContent = clazz.getResourceAsStream(clazz.getSimpleName() + ".class"))
    {
      Collection<String> myResult = systemUnderTest.listDependencies(clazz.getName(), classContent);
      long myDuration = System.currentTimeMillis() - start;
      List<String> ignored = Arrays.asList("java/util/Enumeration"); // TODO: thats in a method signature not
                                                                     // referenced inside the CP
      jdepResult.stream()
                .map(this::extractClassName)
                .filter(n -> !ignored.contains(n))
                .forEach(n -> assertTrue(n + " not listed", myResult.remove(n)));

      assertThat("listed deps not mentioned by jDeps", myResult, empty());
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
    DepParser systemUnderTest = new DepParser();
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
    return line.substring(first, Math.min(line.length() - 1, line.indexOf(" ", first + 1))).replace(".", "/");
  }

  private Collection<String> callJdeps(Class<?> clazz) throws IOException
  {
    String javaHome = System.getenv("JAVA_HOME");
    String command = javaHome == null ? "jdeps" : Paths.get(javaHome, "bin", "jdeps").toString();
    ProcessBuilder builder = new ProcessBuilder(command, "-v", clazz.getName());
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
    return result;
  }

}
