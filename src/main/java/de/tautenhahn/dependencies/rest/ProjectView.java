package de.tautenhahn.dependencies.rest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.tautenhahn.dependencies.analyzers.CycleFinder;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ClassPathUtils;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import de.tautenhahn.dependencies.reports.Unreferenced;


/**
 * Wraps all data of the current project which can be accessed via the REST server.
 *
 * @author TT
 */
public class ProjectView
{

  private final ContainerNode root;

  private final List<Function<DiGraph, DiGraph>> operations = new ArrayList<>();

  private DiGraph currentGraph;

  private DisplayableDiGraph currentlyShown;

  private final String projectName;

  private final List<String> pathElements;

  private final Unreferenced unrefReport;

  /**
   * Creates instance for given class path and project name.
   *
   * @param classPath
   * @param name
   */
  public ProjectView(String classPath, String name)
  {
    List<Path> parsedPath = ClassPathUtils.parseClassPath(classPath);
    Filter filter = new Filter();
    parsedPath.removeIf(p -> filter.isIgnoredSource(p.toString()));
    pathElements = parsedPath.stream().map(Path::toString).collect(Collectors.toList());
    ProjectScanner analyzer = new ProjectScanner(filter);
    root = analyzer.scan(parsedPath);
    unrefReport = new Unreferenced(root, new Unreferenced.ReportConfig());
    resetListMode();
    projectName = name;
  }

  /**
   * Resets the list mode to display jars and source packages.
   */
  public final void resetListMode()
  {
    root.walkCompleteSubTree().forEach(n -> n.setListMode(n.getSimpleName().startsWith("jar:")
      ? ListMode.COLLAPSED : ListMode.LEAFS_COLLAPSED));
    computeGraph();
  }

  /**
   * Changes the list mode of a specified node or its parent.
   *
   * @param nodeNumber
   * @param value
   */
  public void setListMode(int nodeNumber, String value)
  {
    Node node = currentGraph.getAllNodes().get(nodeNumber).getNode();
    if ("COLLAPSE_PARENT".equals(value))
    {
      if (node.getParent().getParent() != null)
      {
        node.getParent()
            .setListMode(node instanceof ClassNode ? ListMode.LEAFS_COLLAPSED : ListMode.COLLAPSED);
      }
      computeGraph();
    }
    else
    {
      ListMode mode = ListMode.valueOf(value);

      if (node.getListMode() != mode)
      {
        node.setListMode(mode);
        computeGraph();
      }
    }
  }


  /**
   * Returns additional information about a node.
   *
   * @param nodeId
   */
  public NodeInfo getNodeInfo(String nodeId)
  {
    return new NodeInfo(currentGraph, Integer.parseInt(nodeId));
  }

  /**
   * Returns additional information about an arc.
   *
   * @param arcId
   */
  public ArcInfo getArcInfo(String arcId)
  {
    return new ArcInfo(currentGraph, arcId);
  }

  /**
   * Returns the graph to be shown.
   */
  DisplayableDiGraph getDisplayableGraph()
  {
    if (currentlyShown == null)
    {
      computeGraph();
    }
    return currentlyShown;
  }

  private void computeGraph()
  {
    DiGraph graph = new DiGraph(root);
    for ( Function<DiGraph, DiGraph> fn : operations )
    {
      graph = fn.apply(graph);
    }
    currentGraph = graph;
    currentlyShown = new DisplayableDiGraph(graph);
  }

  private DiGraph restrictToCycles(DiGraph input)
  {
    for ( IndexedNode n : input.getAllNodes() )
    {
      String name = n.getNode().getSimpleName();
      int pos = name.indexOf('$');
      if (pos > 0)
      {
        String outerName = name.substring(0, pos);
        n.getSuccessors()
         .stream()
         .filter(s -> s.getNode().getSimpleName().equals(outerName))
         .findAny()
         .ifPresent(s -> input.removeArc(n, s));
      }
    }
    return new CycleFinder(input).createGraphFromCycles();
  }

  /**
   * Hides everything except nodes and arcs which are part of a cyclic dependency.
   */
  public void showOnlyCycles()
  {
    operations.add(this::restrictToCycles);
    computeGraph();
  }

  /**
   * removes all restricting filters
   */
  public void showAll()
  {
    boolean change = !operations.isEmpty();
    operations.clear();
    if (change)
    {
      computeGraph();
    }
  }

  /**
   * Returns the project name.
   */
  public String getProjectName()
  {
    return Optional.ofNullable(projectName).orElse("(no name given)");
  }

  /**
   * Returns List of class path elements as absolute path.
   */
  public List<String> getPathElements()
  {
    return pathElements;
  }

  /**
   * Returns the report about unreferenced elements.
   */
  public Unreferenced getUnreferencedReport()
  {
    return unrefReport;
  }
}
