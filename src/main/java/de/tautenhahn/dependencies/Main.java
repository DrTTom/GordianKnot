package de.tautenhahn.dependencies;

import static spark.Spark.port;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import de.tautenhahn.dependencies.parser.GradleAdapter;
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

  static boolean firefoxEnabled=true;

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
    if (firefoxEnabled)
    {
      try
      {
        Runtime.getRuntime().exec("firefox " + url);
        return true;
      }
      catch (RuntimeException|IOException t)
      {
        t.printStackTrace(out);
      }
    }
    return false;
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
          GradleAdapter adapter = new GradleAdapter(src);
          name = adapter.getProjectName();
          pathDef = adapter.getClassPath("runtime");
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
