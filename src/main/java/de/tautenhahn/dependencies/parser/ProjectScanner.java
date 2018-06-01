package de.tautenhahn.dependencies.parser;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Analyzes a project and builds the dependency structure.
 *
 * @author TT
 */
public class ProjectScanner
{

  private final Map<String, ClassNode> classFirstSeenAt = new Hashtable<>();

  private final Map<ClassNode, Collection<String>> deps = new HashMap<>();

  private final ContainerNode root = ContainerNode.createRoot();

  private final Filter filter;


  /**
   * Creates instance for one-time use. TODO: set includes static, create hidden instances.
   *
   * @param includes regular expressions for absolute node names.
   */
  public ProjectScanner(Filter filter)
  {
    this.filter = filter;
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
    classPath.stream().parallel().forEach(this::handleInput);
    for ( Entry<ClassNode, Collection<String>> entry : deps.entrySet() )
    {
      entry.getValue()
           .stream()
           .map(n -> n.replace('/', '.'))
           .map(classFirstSeenAt::get)
           // TODO set the other deps into missing collection
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
        DependencyParser parser = new DependencyParser();
        ContainerNode jarNode = root.createInnerChild("jar:"
                                                      + path.getFileName().toString().replace(".", "_"));
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(path.toFile())))
        {
          ZipEntry entry = zip.getNextEntry();
          while (entry != null)
          {
            if (entry.getName().endsWith(".class") && !entry.getName().endsWith("module-info.class"))
            {
              String className = entry.getName().replace(".class", "").replace('/', '.');
              ClassNode node = jarNode.createLeaf(className);
              classFirstSeenAt.put(className, node);
              deps.put(node, parser.listDependencies(className, new NonClosingStream(zip)));
            }
            entry = zip.getNextEntry();
          }
        }
      }
      else if (Files.isDirectory(path))
      {
        Files.walk(path)
             .filter(p -> isFile(p, ".class") && !path.getFileName().toString().equals("module-info.class"))
             .forEach(p -> handleClassFile(p, path));
      }
    }
    catch (IOException e)
    {
      // TODO:
      e.printStackTrace();
    }
  }

  private static class NonClosingStream extends FilterInputStream
  {

    NonClosingStream(InputStream in)
    {
      super(in);
    }

    @Override
    public void close() throws IOException
    {
      // not closing stream on purpose
    }

  }

  private String getContextDirName(Path path)
  {
    return "dir:" + path.getFileName().toString().replace('.', '_');
  }

  private boolean isFile(Path path, String suffix)
  {
    return path.getFileName().toString().endsWith(suffix) && Files.isRegularFile(path);
  }

  private void handleClassFile(Path clazz, Path root)
  {
    String className = root.relativize(clazz).toString().replace(".class", "").replace('/', '.');
    if (filter.isIgnoredClass(className))
    {
      return;
    }
    String source = root.getFileName().toString().replace('.', '_');
    String nodeName = "dir:" + source + "." + className;
    if (filter.isIgnoredSource(nodeName))
    {
      return;
    }
    ClassNode node = this.root.createLeaf(nodeName);
    classFirstSeenAt.put(className, node);
    try (InputStream in = new FileInputStream(clazz.toFile()))
    { // TODO: allow filter to switch off parsing the dependencies of supporting nodes.
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
