package de.tautenhahn.dependencies.parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;


/**
 * Helper class to get class path information out of Gradle files. TODO: extract interface and write adapter
 * for other build systems.
 *
 * @author TT
 */
public class GradleAdapter
{

  private final Path workingDir;

  private Path gradle;

  /**
   * Creates instance to analyze gradle project specified by build file.
   *
   * @param gradleFile
   * @throws IOException
   */
  public GradleAdapter(Path gradleFile)
  {
    try
    {
      gradle = gradleFile.toRealPath();
    }
    catch (IOException e)
    {
      throw new IllegalArgumentException("file " + gradleFile, e);
    }
    workingDir = Optional.ofNullable(gradle.getParent())
                         .orElseThrow(() -> new IllegalArgumentException("no parent directory for "
                                                                         + gradle));

  }

  /**
   * Returns the project name
   */
  public String getProjectName()
  {
    return String.valueOf(workingDir.getFileName());
  }

  /**
   * Returns the class path for specified configuration.
   *
   * @param configuration
   */
  public String getClassPath(String configuration)
  {
    StringBuilder result = new StringBuilder();
    result.append(workingDir.resolve("build/classes/java/main")).append(':');
    if (configuration.contains("test"))
    {
      result.append(workingDir.resolve("build/classes/java/test")).append(':');
    }
    result.append(readFile(callGradle(configuration)));
    return result.toString();
  }

  private Path callGradle(String configuration)
  {
    Path backup = workingDir.resolve(gradle.getFileName() + ".bak");
    try
    {
      Files.copy(gradle, backup, StandardCopyOption.REPLACE_EXISTING);
      changeBuildFileAndCall(configuration, backup);
    }
    catch (IOException | InterruptedException e)
    {
      throw new IllegalArgumentException("gradle call not successful", e);
    }
    return workingDir.resolve("build/classpath.txt");
  }

  private void changeBuildFileAndCall(String configName, Path backup) throws IOException, InterruptedException
  {
    String addition= "\ntask writeClasspath {\n"
                   + "   doLast {\n"
                   + "      buildDir.mkdirs()\n"
                   + "      new File(buildDir, \"classpath.txt\").text = configurations." + configName + ".asPath + \"\\n\"\n"
                   + "   }\n}";

    try
    {
      Files.write(gradle, addition.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
      Process proc = new ProcessBuilder("gradle", "writeClasspath").directory(workingDir.toFile()).start();
      proc.waitFor();
    }
    finally
    {
      Files.move(backup, gradle, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static String readFile(Path path)
  {
    try
    {
      return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
    catch (IOException e)
    {
      throw new IllegalArgumentException("cannot read " + path, e);
    }
  }
}
