package de.tautenhahn.dependencies.testutils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Allows access to the current class path.
 * 
 * @author TT
 */
public final class ClassPathUtils
{

  private ClassPathUtils()
  {
    // utility class
  }

  /**
   * Returns the class path elements as List.
   */
  public static List<String> getClassPath()
  {
    return Arrays.asList(System.getProperty("java.class.path").split(java.io.File.pathSeparator));
  }

  /**
   * Returns a new class loader using specified class path. <br>
   * Warning: Loading lots of classes for a big project is not a suitable way to analyze the project. Use only
   * to analyze some special classes.
   * 
   * @param classPath
   */
  public static ClassLoader createClassLoader(List<String> classPath)
  {
    Collection<URL> urls = classPath.stream().map(ClassPathUtils::toUrl).collect(Collectors.toList());
    return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
  }

  private static URL toUrl(String pathName)
  {
    try
    {
      return new File(pathName).toURI().toURL();
    }
    catch (MalformedURLException e) // cannot happen because JVM does correct escaping
    {
      throw new IllegalArgumentException(e);
    }
  }
}
