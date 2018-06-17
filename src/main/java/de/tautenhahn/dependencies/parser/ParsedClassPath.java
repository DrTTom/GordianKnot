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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Wraps the class path, provides it as collections of Path objects with hopefully good names.
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
    Arrays.stream(classpath.split(File.pathSeparator)).map(Paths::get).forEach(this::registerEntry);
    setupNames();
  }

  /**
   * Sorry for the ugly code but it is still fast enough.
   */
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
    Collection<URL> urls = Stream.concat(sourceFolders.stream(), archives.stream())
                                 .map(this::toUrl)
                                 .collect(Collectors.toList());
    // start work-around for JAVA 8:
    ClassLoader parent = null;
    try
    {
      Method getter = ClassLoader.class.getMethod("getPlatformClassLoader");
      parent = (ClassLoader)getter.invoke(ClassLoader.class);
    }
    catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
      | InvocationTargetException e)
    {
      parent = Thread.currentThread().getContextClassLoader();
    }
    // end work-around
    return new URLClassLoader(urls.toArray(new URL[0]), parent);
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
}
