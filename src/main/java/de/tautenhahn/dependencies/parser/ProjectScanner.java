package de.tautenhahn.dependencies.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;


/**
 * Analyzes a project and builds the dependency structure.
 *
 * @author TT
 */
public class ProjectScanner
{

  private final Map<String, ClassNode> classFirstSeenAt = new HashMap<>();

  private final Map<ClassNode, Collection<String>> deps = new HashMap<>();

  private final ContainerNode root = ContainerNode.createRoot();



  /**
   * Creates instance for one-time use. TODO: set includes static, create hidden instances.
   *
   * @param includes regular expressions for absolute node names.
   */
  public ProjectScanner(String... includes)
  {
    // TODO!
  }

  /**
   * Runs the dependency analysis for all classes in the class path with match some include. All other classes
   * are considered only as needed by included classes but not analyzed themselves.
   *
   * @param classPath paths to jar files or build directories.
   * @return root node of the created graph.
   */
  public ContainerNode scan(Collection<Path> classPath)
  {
    classPath.stream().forEach(this::handleInput);
    for ( Entry<ClassNode, Collection<String>> entry : deps.entrySet() )
    {
      entry.getValue()
           .stream()
           .map(n -> n.replace('/', '.'))
           .map(classFirstSeenAt::get)
           .filter(Objects::nonNull)
           .forEach(n -> entry.getKey().addSuccessor(n));
    }
    return root;
  }

  private void handleInput(Path path)
  {
    try
    {
      if (isFile(path, ".jar"))
      {
        // TODO: handle Zip
      }
      else if (Files.isDirectory(path))
      {
        Files.walk(path).filter(p -> isFile(p, ".class")).forEach(p -> handleClassFile(p, path));
      }
    }
    catch (IOException e)
    {
      // TODO:
      e.printStackTrace();
    }
  }


  private boolean isFile(Path path, String suffix)
  {
    return path.getFileName().toString().endsWith(suffix) && Files.isRegularFile(path);
  }

  private void handleClassFile(Path clazz, Path root)
  {
    String className = root.relativize(clazz).toString().replace(".class", "").replace('/', '.');
    String source = root.getFileName().toString().replace('.', '_');
    ClassNode node = this.root.createLeaf(source + ":." + className);
    classFirstSeenAt.put(className, node);
    // TODO: stop if no include applies

    try (InputStream in = new FileInputStream(clazz.toFile()))
    {
      DependencyParser parser = new DependencyParser();
      deps.put(node, parser.listDependencies(className, in));
    }
    catch (IOException e)
    {
      // TODO:
      e.printStackTrace();
    }
  }

}
