package de.tautenhahn.dependencies;

import static spark.Spark.port;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Optional;

import de.tautenhahn.dependencies.parser.Pair;
import de.tautenhahn.dependencies.rest.ProjectView;
import de.tautenhahn.dependencies.rest.Server;
import spark.Spark;


/**
 * Command line interface to start the server.
 *
 * @author TT
 */
public final class Main
{

  static PrintStream out = System.out;

  static boolean fireFoxDisabled;

  private Main()
  { // no instances wanted
  }

  /**
   * Command line call.
   *
   * @param args
   */
  public static void main(String... args)
  {
    if (args.length == 0 || args[0].toLowerCase(Locale.ENGLISH).matches("--?h(elp)?"))
    {
      out.println("\"Gordian Knot\" dependency checker version 0.3 alpha"
                  + "\nUsage: GordianKnot <classpathToCheck> [projectName] [options]");
      return;
    }
    Pair<String, String> resolved = parseArgs(args);
    ProjectView view = new ProjectView(resolved.getFirst(), resolved.getSecond());
    Server instance = new Server(view);
    instance.start();
    String url = "http://localhost:" + port() + "/index.html";
    Spark.awaitInitialization();
    if (!startFireFox(url))
    {
      out.println("Server started, point your browser to " + url);
    }
  }


  private static boolean startFireFox(String url)
  {
    if (fireFoxDisabled)
    {
      return false;
    }
    try
    {
      Runtime.getRuntime().exec("firefox " + url);
      return true;
    }
    catch (Throwable t)
    {
      return false;
    }
  }


  private static Pair<String, String> parseArgs(String... args)
  {
    String pathDef = args[0];
    String name = args.length > 1 ? args[1] : "unknown";
    if (!pathDef.contains(":") && (pathDef.endsWith(".gradle") || pathDef.endsWith(".txt")))
    {
      try
      {
        Path src = Paths.get(pathDef).toRealPath();
        if (pathDef.endsWith(".gradle"))
        {
          Path workingDir = Optional.ofNullable(src.getParent())
                                    .orElseThrow(() -> new IllegalArgumentException("no parent directory for "
                                                                                    + src));
          name = replaceUnknown(name, String.valueOf(workingDir.getFileName()));
          pathDef = workingDir.resolve("build/classes/java/main") + ":"
                    + readFile(callGradle(src, workingDir));
        }
        else
        {
          name = replaceUnknown(name, String.valueOf(src.getFileName()).replace(".txt", ""));
          pathDef = readFile(src);
        }
      }
      catch (IOException e)
      {
        throw new IllegalArgumentException("cannot resolve " + pathDef, e);
      }
    }
    return new Pair<>(pathDef, name);
  }

  private static String replaceUnknown(String name, String newName)
  {
    return "unknown".equals(name) ? newName : name;
  }

  static Path callGradle(Path gradle, Path workingDir)
  {
    Path backup = workingDir.resolve(gradle.getFileName() + ".bak");
    try
    {
      Files.copy(gradle, backup, StandardCopyOption.REPLACE_EXISTING);
      changeBuildFileAndCall(gradle, "runtime", backup, workingDir);
    }
    catch (IOException | InterruptedException e)
    {
      throw new IllegalArgumentException("gradle call not successful", e);
    }
    return workingDir.resolve("build/classpath.txt");
  }

  private static void changeBuildFileAndCall(Path gradle, String configName, Path backup, Path workingDir)
    throws IOException, InterruptedException
  {
    String addition = "\ntask writeClasspath << {\n" + "    buildDir.mkdirs()\n"
                      + "    new File(buildDir, \"classpath.txt\").text = configurations." + configName
                      + ".asPath + \"\\n\"\n" + "}";
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
