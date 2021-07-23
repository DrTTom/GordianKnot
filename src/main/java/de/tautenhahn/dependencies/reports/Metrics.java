package de.tautenhahn.dependencies.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.tautenhahn.dependencies.analyzers.BasicGraphOperations;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ListModeUtil;
import de.tautenhahn.dependencies.parser.Pair;


/**
 * Gets some metrics about dependencies. Currently only functionality, fancy stuff will follow when needed.
 *
 * @author TT
 */
public class Metrics
{

  /**
   * ccd/acd/rcd (second index) for own classes, own packages, archives + class directories (first index)
   */
  final double[][] xcd = new double[3][3];

  final List<List<String>> worstElements = new ArrayList<>();

  final int[] numElements = new int[3];

  final double[] edgeDensity = new double[3];


  /**
   * Creates instance.
   *
   * @param root root node of component tree to analyze
   * @param filter specifies which of these nodes to consider
   */
  public Metrics(ContainerNode root, Filter filter)
  {
    ListModeUtil.showJarsAndOwnClasses(root);
    DiGraph ownClasses = new DiGraph(root.walkSubTree()
                                         .filter(n -> n instanceof ClassNode)
                                         .filter(n -> filter.isInFocus(n.getName())));
    computeValues(0, ownClasses);

    ListModeUtil.showJarsAndOwnPackages(root);
    DiGraph ownPackages = new DiGraph(root.walkSubTree()
                                          .filter(n -> ((ContainerNode)n).hasOwnContent())
                                          .filter(n -> filter.isInFocus(n.getName())));
    computeValues(1, ownPackages);

    ListModeUtil.showResourcesOnly(root);
    computeValues(2, new DiGraph(root.walkSubTree()));
  }

  private void computeValues(int i, DiGraph graph)
  {
    numElements[i] = graph.getAllNodes().size();
    edgeDensity[i] = BasicGraphOperations.getDensity(graph);
    Pair<int[], int[]> numbers = BasicGraphOperations.countDependsOnAndUsedFrom(graph);
    xcd[i][0] = BasicGraphOperations.ccd(numbers);
    xcd[i][1] = BasicGraphOperations.acd(numbers);
    xcd[i][2] = BasicGraphOperations.rcd(numbers);
    worstElements.add(graph.getAllNodes()
                           .stream()
                           .sorted((a, b) -> numbers.getFirst()[b.getIndex()]
                                             - numbers.getFirst()[a.getIndex()]
                                             + numbers.getSecond()[b.getIndex()]
                                             - numbers.getSecond()[a.getIndex()])
                           .limit(Math.min(Math.max(graph.getAllNodes().size() / 5, 1), 5))
                           .map(n -> n.getNode().getName())
                           .collect(Collectors.toList()));
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder("Component Dendency:\n               cumulative    average   relative");
    append(result, "own classes:   ", xcd[0]);
    append(result, "own packagess: ", xcd[1]);
    append(result, "archives:      ", xcd[2]);
    result.append("\n\nworst classes:  ")
          .append(worstElements.get(0))
          .append("\nworst packages: ")
          .append(worstElements.get(1))
          .append("\nworst archives: ")
          .append(worstElements.get(2));

    return result.toString();
  }

  @SuppressWarnings("boxing")
  private void append(StringBuilder result, String label, double... values)
  {
    result.append('\n')
          .append(label)
          .append(String.format(Locale.ENGLISH, "%10.2f %10.2f %10.2f", values[0], values[1], values[2]));
  }
}
