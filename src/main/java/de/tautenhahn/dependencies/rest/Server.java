package de.tautenhahn.dependencies.rest;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.staticFiles;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.tautenhahn.dependencies.parser.Pair;
import de.tautenhahn.dependencies.rest.presentation.DisplayableDiGraph;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;


/**
 * The REST server. Supplies the graph for displaying and allows calling some commands. Current implementation
 * is for one session only.
 *
 * @author TT
 */
public class Server
{

  /**
   * This object should be put into the session when multiple sessions are supported.
   */
  ProjectView view;

  /**
   * Creates new instance to access given project view.
   *
   * @param view
   */
  public Server(ProjectView view)
  {
    this.view = view;
  }

  /**
   * Starts the server.
   */
  public void start()
  {
    staticFiles.location("frontend");
    allowCrossSiteCalls();
    JsonTransformer transformer = new JsonTransformer();
    get("view", (req, res) -> view.getDisplayableGraph(), transformer);
    get("view/name", (req, res) -> view.getProjectName());
    get("view/classpath", (req, res) -> view.getClassPath(), transformer);
    get("view/unrefReport", (req, res) -> view.getUnreferencedReport(), transformer);
    get("view/missingReport", (req, res) -> view.getMissingClassesReport(), transformer);
    get("view/metrics", (req, res) -> view.getMetrics(), transformer);
    get("view/node/:id", (req, res) -> view.getNodeInfo(req.params("id")), transformer);
    get("view/arc/:id", (req, res) -> view.getArcInfo(req.params("id")), transformer);
    get("view/activeFilters", (req, res) -> view.listActiveFilters(), transformer);
    get("view/nodesByName/:name", (req, res) -> view.getNodeIDs(req.params("name")), transformer);
    get("view/node/:id/listmode/:value", this::setListMode, transformer); // TODO change to put when
                                                                          // everything works!

    installFilterRoute("cycles", view::showOnlyCycles);
    installFilterRoute("none", view::showAll);
    installFilterRoute("resetListMode", view::resetListMode);
    get("view/filters/impliedBy/:id/:successors",
        (req, res) -> view.restrictToImpliedBy(Integer.parseInt(req.params("id")),
                                               Boolean.parseBoolean(req.params("successors"))),
        transformer);

  }

  private void installFilterRoute(String filter, Runnable modification) // NOPMD no threads here!
  {
    // TODO change to put when everything works!
    get("view/filters/" + filter, (req, res) -> {
      modification.run();
      return view.getDisplayableGraph();
    }, new JsonTransformer());
  }

  /**
   * @param req
   * @param res to fit the interface, unused on purpose
   */
  private Pair<DisplayableDiGraph, List<String>> setListMode(Request req, Response res) // NOPMD
  {
    String nodeName = view.changeListMode(Integer.parseInt(req.params("id")), req.params("value"));
    return new Pair<>(view.getDisplayableGraph(), view.getNodeIDs(nodeName));
  }

  /**
   * All structured output to front end is JSON.
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

  static void allowCrossSiteCalls()
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
