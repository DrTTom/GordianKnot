package de.tautenhahn.dependencies.parser;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 * Wraps the class path, provides it as collections of Path objects with distinctive short names.
 *
 * @author TT
 */
public class ParsedClassPath
{

  private final List<Path> sourceFolders = new ArrayList<>();

  private final List<Path> archives = new ArrayList<>();

  private final List<Path> entries = new ArrayList<>();

  private final Map<Path, String> names = new HashMap<>();

  private final Map<Path, String> problems = new HashMap<>();

  /**
   * Returns a name to describe the entry, hopefully a comprehensive short one.
   *
   * @param entry
   */
  public String getName(Path entry)
  {
    return names.get(entry);
  }

  /**
   * Returns a collection of Path instances representing given class path.
   *
   * @param classpath
   */
  public ParsedClassPath(String classpath)
  {
    Arrays.stream(classpath.split(File.pathSeparator, -1)).map(Paths::get).forEach(this::registerEntry);
    setupNames();
  }

  private void setupNames()
  {
    UniqueNameTree tree = new UniqueNameTree();
    entries.forEach(tree::add);
    names.putAll(tree.createNames());
  }

  private void registerEntry(Path entry)
  {
    entries.add(entry);
    if (Files.isRegularFile(entry) && Files.isReadable(entry))
    {
      archives.add(entry);
    }
    else if (Files.isDirectory(entry) && Files.isExecutable(entry))
    {
      sourceFolders.add(entry);
    }
    else
    {
      problems.put(entry, "not readable");
    }
  }

  /**
   * Returns the class path elements as List.
   */
  public static ParsedClassPath getCurrentClassPath()
  {
    return new ParsedClassPath(System.getProperty("java.class.path"));
  }

  /**
   * Returns a new class loader using this class path only (in Java 9 or higher) or current class path plus
   * this (earlier java versions) <br>
   * Warning: Loading lots of classes for a big project is not a suitable way to analyze the project. Use only
   * to analyze some special classes.
   */
  public ClassLoader createClassLoader()
  {
    URL[] resources = Stream.concat(sourceFolders.stream(), archives.stream())
                            .map(this::toUrl)
                            .toArray(URL[]::new);
    PrivilegedAction<ClassLoader> create = () -> new URLClassLoader(resources, getParent());
    return AccessController.doPrivileged(create);
  }

  /** Work-around for java 8 not having a platform class loader */
  private ClassLoader getParent()
  {
    try
    {
      Method getter = ClassLoader.class.getMethod("getPlatformClassLoader");
      return (ClassLoader)getter.invoke(ClassLoader.class);
    }
    catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
      | InvocationTargetException e)
    {
      return Thread.currentThread().getContextClassLoader();
    }
  }

  private URL toUrl(Path entry)
  {
    try
    {
      return entry.toUri().toURL();
    }
    catch (MalformedURLException e) // cannot happen because JVM does correct escaping
    {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * @return the sourceFolders
   */
  public List<Path> getSourceFolders()
  {
    return sourceFolders;
  }

  /**
   * @return the archives
   */
  public List<Path> getArchives()
  {
    return archives;
  }

  /**
   * @return the entries
   */
  public List<Path> getEntries()
  {
    return entries;
  }

  @Override
  public int hashCode()
  {
    return archives.hashCode() + 3 * sourceFolders.hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null || ParsedClassPath.class != obj.getClass())
    {
      return false;
    }
    ParsedClassPath other = (ParsedClassPath)obj;
    return archives.equals(other.archives) && sourceFolders.equals(other.sourceFolders);
  }
}
