package de.tautenhahn.dependencies.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Collectors;


/**
 * Wraps assumptions about existing components and their dependencies. A component may be any set of code the
 * user wants to consider as a unit.
 *
 * @author TT
 */
public class ComponentsDesign
{

  private final Map<String, String> componentByPackage = new TreeMap<>((a, b) -> b.length() - a.length());

  private final Map<String, List<String>> allowedCompSuccs = new HashMap<>();

  /**
   * Creates instance and parses component definition from given configuration file.
   *
   * @param conf
   * @throws IOException
   */
  public ComponentsDesign(URL conf) throws IOException
  {
    Map<String, List<String>> succPatterns = new HashMap<>();
    Map<String, List<String>> predPatterns = new HashMap<>();
    List<String> components = new ArrayList<>();

    try (InputStream ins = conf.openStream(); Scanner s = new Scanner(ins, "UTF-8"))
    {
      String component = "UNASSIGNED";
      while (s.hasNextLine())
      {
        String line = s.nextLine().trim();
        if (isComponentDef(line))
        {
          component = line.substring(1, line.length() - 1).trim();
          components.add(component);
        }
        else if (line.matches("\\w+(\\.\\w+)*"))
        {
          componentByPackage.put(line, component);
        }
        else if (line.startsWith("->") || line.startsWith("<-"))
        {
          Map<String, List<String>> map = line.charAt(0) == '-' ? succPatterns : predPatterns;
          map.computeIfAbsent(component, k -> new ArrayList<>()).add(line.substring(2).trim());
        }
        else if (!line.isEmpty() && !line.startsWith("//"))
        {
          throw new IOException("Undefined line in components definition: '" + line + "'");
        }
      }
    }
    setupInterfaces(components, succPatterns, predPatterns);
  }

  private boolean isComponentDef(String line)
  {
    return line.length() > 2 && line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']';
  }

  private void setupInterfaces(List<String> components,
                               Map<String, List<String>> succPatterns,
                               Map<String, List<String>> predPatterns)
  {
    succPatterns.forEach((c, p) -> getSuccList(c).addAll(getMatches(p, components)));
    predPatterns.forEach((c, p) -> getMatches(p, components).forEach(pred -> getSuccList(pred).add(c)));
    // Maybe warn if some of the interfaces have been declared in both and some only in one of the
    // respective components
  }

  private List<String> getSuccList(String c)
  {
    return allowedCompSuccs.computeIfAbsent(c, k -> new ArrayList<>());
  }

  private Collection<String> getMatches(List<String> patterns, List<String> components)
  {
    // could be optimized!
    return patterns.stream()
                   .flatMap(p -> components.stream()
                                           .filter(c -> p.equals(c) || c.matches(p.replace("*", ".*"))))
                   .distinct()
                   .collect(Collectors.toList());
  }

  /**
   * Returns the name of the component for given class name.
   */
  public String getComponentName(String className)
  {
    return componentByPackage.entrySet()
                             .stream()
                             .filter(e -> className.startsWith(e.getKey()))
                             .map(Map.Entry::getValue)
                             .findFirst()
                             .orElse("UNASSIGNED");
  }
}
