package de.tautenhahn.dependencies.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


/**
 * Wraps assumptions about existing components and their dependencies. A component may be any set of code the
 * user wants to consider as a unit.
 *
 * @author TT
 */
public class ComponentsDesign
{

  private final Map<String, String> componentByPackage = new TreeMap<>((a, b) -> b.length() - a.length());

  // TODO: define wanted component dependencies;
  // private final Map<String, List<String>> componentDependencies = new HashMap<>();

  /**
   * Creates instance and parses component definition from given configuration file.
   *
   * @param conf
   * @throws IOException
   */
  public ComponentsDesign(URL conf) throws IOException
  {
    try (InputStream ins = conf.openStream(); Scanner s = new Scanner(ins, "UTF-8"))
    {
      // TODO handle wrong input!
      String component = null;
      while (s.hasNextLine())
      {
        String line = s.nextLine().trim();
        if (line.isEmpty())
        {
          continue;
        }

        if (line.charAt(0) == '[')
        {
          component = line.substring(1, line.length() - 1);
        }
        else
        {
          componentByPackage.put(line, component);
        }
      }
    }
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
