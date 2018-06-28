package de.tautenhahn.dependencies.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import de.tautenhahn.dependencies.reports.Unreferenced;
import de.tautenhahn.dependencies.rest.presentation.DisplayableClasspathEntry;


/**
 * Wraps all data of the current project which can be accessed via the REST server.
 *
 * @author TT
 */
public class ProjectView
{

  private final ContainerNode root;

  private final Collection<ViewFilter> filters = new LinkedHashSet<>();

  private DiGraph currentGraph;

  private DisplayableDiGraph currentlyShown;

  private final String projectName;


  private final ParsedClassPath classPath;

  private final Unreferenced unrefReport;

  /**
   * Creates instance for given class path and project name.
   *
   * @param classPath
   * @param name
   */
  public ProjectView(String classPath, String name)
  {
    this.classPath = new ParsedClassPath(classPath);
    Filter filter = new Filter();
    // TODO: parsedPath.removeIf(p -> filter.isIgnoredSource(p.toString()));
    ProjectScanner analyzer = new ProjectScanner(filter);
    root = analyzer.scan(this.classPath);
    unrefReport = Unreferenced.forProject(root, filter, this.classPath).create();
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
   * Collapses all nodes except the virtual root. May be useful with large multi-projects.
   */
  public final void collapseAll()
  {
    root.walkCompleteSubTree().forEach(n -> n.setListMode(ListMode.COLLAPSED));
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
    List<ViewFilter> toRemove = new ArrayList<>();
    for ( ViewFilter fn : filters )
    {
      if (fn.isApplicable(graph))
      {
        graph = fn.apply(graph);
      }
      else
      {
        toRemove.add(fn);
      }
    }
    filters.removeAll(toRemove);
    currentGraph = graph;
    currentlyShown = new DisplayableDiGraph(graph);
  }

  /**
   * Hides everything except nodes and arcs which are part of a cyclic dependency.
   */
  public void showOnlyCycles()
  {
    filters.add(new CyclesOnly());
    computeGraph();
  }

  /**
   * removes all restricting filters
   */
  public void showAll()
  {
    if (!filters.isEmpty())
    {
      filters.clear();
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
  public List<DisplayableClasspathEntry> getClassPath()
  {
    Filter filter = new Filter(); // TODO: configure the filter!
    return classPath.getEntries()
                    .stream()
                    .filter(p -> !filter.isIgnoredSource(p.toString()))
                    .map(p -> new DisplayableClasspathEntry(p, classPath))
                    .collect(Collectors.toList());
  }

  /**
   * Returns the report about unreferenced elements.
   */
  public Unreferenced getUnreferencedReport()
  {
    return unrefReport;
  }

  /**
   * Restrict current graph to predecessors/successors of some specified node. Non-Persistent operation,
   * vanishes with next graph change.
   *
   * @param nodeIndex
   * @param successors
   */
  public DisplayableDiGraph restrictToImpliedBy(int nodeIndex, boolean successors)
  {
    IndexedNode start = currentGraph.getAllNodes().get(nodeIndex);
    String startName = start.getNode().getName();
    ImpliedByNode filter = successors ? ImpliedByNode.requiredBy(startName)
      : ImpliedByNode.dependingOn(startName);
    filters.add(filter);
    computeGraph();
    return currentlyShown;
  }

  /**
   * Returns the name of all active filters.
   */
  public List<String> listActiveFilters()
  {
    return filters.stream().map(ViewFilter::getName).collect(Collectors.toList());
  }

  /**
   * Returns the IDs of all nodes representing the element specified by node name in current graph. This may
   * be the node itself, its children or some collapsed ancestor.
   *
   * @param nodeName
   * @return empty list if node is not represented.
   */
  public List<String> getNodeIDs(String nodeName)
  {
    Node listed = Optional.ofNullable(root.find(nodeName)).map(Node::getListedContainer).orElse(null);
    if (listed == null)
    {
      return Collections.emptyList();
    }
    Map<Node, String> shownIds = currentGraph.getAllNodes()
                                             .stream()
                                             .collect(Collectors.toMap(IndexedNode::getNode,
                                                                       n -> Integer.toString(n.getIndex())));

    return replaceByNodesHavingContent(listed).map(shownIds::get)
                                              .filter(Objects::nonNull)
                                              .collect(Collectors.toList());
  }

  private Stream<Node> replaceByNodesHavingContent(Node node)
  {
    if (node.hasOwnContent())
    {
      return Stream.of(node);
    }
    return ((ContainerNode)node).getChildren().stream().flatMap(this::replaceByNodesHavingContent);
  }
}
