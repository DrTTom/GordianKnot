package de.tautenhahn.dependencies.parser;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Unit tests for finding out which classes are referenced by a given class.
 *
 * @author TT
 */
public class TestClassAndDependencyInfo
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
    Collection<String> outputLines = callJdeps(clazz);
    long durationJDep = System.currentTimeMillis() - start;
    Collection<String> fromJDeps = outputLines.stream()
                                              .map(this::extractClassName)
                                              .filter(Objects::nonNull)
                                              .collect(Collectors.toSet());

    start = System.currentTimeMillis();
    try (InputStream classContent = clazz.getResourceAsStream(clazz.getSimpleName() + ".class"))
    {
      ClassAndDependencyInfo systemUnderTest = ClassAndDependencyInfo.parse(classContent, clazz.getName());
      long myDuration = System.currentTimeMillis() - start;
      Collection<String> fromMe = systemUnderTest.getDependencies();
      Set<String> notListedByMe = new HashSet<>(fromJDeps);
      notListedByMe.removeAll(fromMe);

      Set<String> addedByMe = new HashSet<>(fromMe);
      addedByMe.removeAll(fromJDeps);

      assertThat("dependencies not found but listed by jDeps", notListedByMe, empty());
      assertThat("dependencies added but not listed by jDeps", addedByMe, empty());
      assertThat("duration", myDuration, lessThanOrEqualTo(durationJDep / 20));
    }
  }

  /**
   * Parses an example class and checks that field, annotation, argument, return value and variable types are
   * listed while String constants and generic parameter types are ignored.
   *
   * @throws IOException
   */
  @Test
  public void newDependencyParser() throws IOException
  {
    try (InputStream classContent = ExampleClass.class.getResourceAsStream(ExampleClass.class.getSimpleName()
                                                                           + ".class"))
    {
      ClassAndDependencyInfo systemUnderTest = ClassAndDependencyInfo.parse(classContent,
                                                                            ExampleClass.class.getName());
      assertThat("parsed class name", systemUnderTest.getClassName(), is(ExampleClass.class.getName()));
      assertThat("dependencies",
                 systemUnderTest.getDependencies(),
                 containsInAnyOrder(Logger.class.getName(),
                                    LoggerFactory.class.getName(),
                                    HashMap.class.getName(), // NOPMD: need class name, not type
                                    String.class.getName(),
                                    Supplier.class.getName(),
                                    List.class.getName(),
                                    Boolean.class.getName(),
                                    Object.class.getName(),
                                    Deprecated.class.getName(),
                                    Class.class.getName()));
    }
  }

  /**
   * Asserts that an exception is thrown if class has not the expected name.
   *
   * @throws IOException
   */
  @Test(expected = IllegalArgumentException.class)
  public void wrongClassName() throws IOException
  {
    try (
      InputStream ins = ExampleClass.class.getResourceAsStream(ExampleClass.class.getSimpleName() + ".class"))
    {
      ClassAndDependencyInfo.parse(ins, "de.tautenhahn.DifferentFile");
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
    ClassAndDependencyInfo.addClassNames(input, result);
    assertThat("parsed class names",
               result,
               containsInAnyOrder("java.util.concurrent.ConcurrentHashMap$BulkTask",
                                  "java.util.concurrent.ConcurrentHashMap$Node",
                                  "java.util.concurrent.ConcurrentHashMap$ReduceValuesTask",
                                  "java.util.function.BiFunction"));
  }

  private String extractClassName(String line)
  {
    int first = line.indexOf('>') + 2;
    int end = line.indexOf(" ", first + 1);
    return first < 0 || end < 0 ? null : line.substring(first, end);
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
    Files.deleteIfExists(tempfile);
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
