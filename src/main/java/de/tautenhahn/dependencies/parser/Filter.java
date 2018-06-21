package de.tautenhahn.dependencies.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;


/**
 * TODO: get the logic clean!
 *
 * @author TT
 */
public class Filter
{

  private final Collection<Pattern> ignoredClassNames = new ArrayList<>();

  private final Collection<Pattern> ignoredSources = new ArrayList<>();

  private final Collection<Pattern> focus = new ArrayList<>();


  /**
   * Creates an instance which by default focuses on all the classes given as class files in directories and
   * ignores java native classes.
   */
  public Filter()
  {
    ignoredClassNames.add(Pattern.compile("java\\..*"));
    ignoredSources.add(Pattern.compile(".*/jre/lib/.*"));
    ignoredSources.add(Pattern.compile(".*/build/resources/.*"));
    ignoredSources.add(Pattern.compile(".*/eclipse/configuration/.*/\\.cp"));
    focus.add(Pattern.compile("dir:.*"));
  }

  /**
   * Adds a regular expression for fully qualified class names to ignore. Matching classes are not analyzed,
   * dependencies to those classes are taken for granted.
   *
   * @param regex
   */
  public void addIgnoredClassName(String regex)
  {
    ignoredClassNames.add(Pattern.compile(regex));
  }

  /**
   * Returns true if name denotes a source not to be parsed.
   *
   * @param name
   */
  public boolean isIgnoredSource(String name)
  {
    return ignoredSources.stream().anyMatch(s -> s.matcher(name).matches());
  }

  /**
   * Returns true if name is the class name of an ignored class. Dependency to such classes are ignored as
   * well.
   *
   * @param name
   */
  public boolean isIgnoredClass(String name)
  {
    return ignoredClassNames.stream().anyMatch(p -> p.matcher(name).matches());
  }


  /**
   * Returns true if name denotes a class which should undergo all the analyzing procedures.
   *
   * @param name
   */
  public boolean isInFocus(String name)
  {
    return focus.stream().anyMatch(p -> p.matcher(name).matches());
  }

  /**
   * Returns true if name denotes a supporting class. These classes may be parsed to check the correct entries
   * of the class path but will be excluded from every other analysis. More precisely, if some required class
   * depends on this class, it is required to be present in the class path too. That is all we ever expect
   * from a supporting class.
   *
   * @param name
   */
  public boolean isSupporting(String name)
  {
    return !isIgnoredSource(name) && !isInFocus(name);
  }
}
