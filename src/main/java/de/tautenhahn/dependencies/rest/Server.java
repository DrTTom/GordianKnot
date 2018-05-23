package de.tautenhahn.dependencies.rest;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.tautenhahn.dependencies.core.Node;
import de.tautenhahn.dependencies.core.ProjectAnalyzer;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;


/**
 * The REST server. Supplies the graph for displaying and allows calling some commands.
 *
 * @author TT
 */
public class Server
{

  public static void main(String... args)
  {
    allowCrossSiteCalls();
    get("view", Server::displayGraph, new JsonTransformer());
  }

  private static DisplayableDependencyGraph displayGraph(Request req, Response res)
  {
    ProjectAnalyzer analyzer = new ProjectAnalyzer();
    List<Path> classPath = Arrays.asList(Paths.get("build", "classes", "java", "main"),
                                         Paths.get("build", "classes", "java", "test"));
    Node root = analyzer.analyze(classPath);

    return new DisplayableDependencyGraph(root);
  }

  /**
   * All structured output top frontend is JSON.
   */
  static class JsonTransformer implements ResponseTransformer
  {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String render(Object model)
    {
      return gson.toJson(model);
    }

  }

  private static void allowCrossSiteCalls()
  {
    before((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Request-Method", "*");
      response.header("Access-Control-Allow-Headers", "*");
      // Note: this may or may not be necessary in your particular application
      response.type("application/json");
    });

    options("/*", (request, response) -> {

      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null)
      {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null)
      {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });
  }
}
