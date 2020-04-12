package de.tautenhahn.dependencies.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Finds out which classes are referenced from a given class. Instances are for one time use!
 *
 * @author TT
 */
public final class ClassAndDependencyInfo
{

  private static final Logger LOG = LoggerFactory.getLogger(ClassAndDependencyInfo.class);

  private static final int MAGIC = 0xCAFEBABE;

  private static final int MAX_SUPPORTED_VERSION = 57;

  private static final byte CONSTANT_UTF8 = 1;

  private static final byte CONSTANT_INTEGER = 3;

  private static final byte CONSTANT_FLOAT = 4;

  private static final byte CONSTANT_LONG = 5;

  private static final byte CONSTANT_DOUBLE = 6;

  private static final byte CONSTANT_CLASS = 7;

  private static final byte CONSTANT_STRING = 8;

  private static final byte CONSTANT_FIELDREF = 9;

  private static final byte CONSTANT_METHODREF = 10;

  private static final byte CONSTANT_INTERFACEMETHODREF = 11;

  private static final byte CONSTANT_NAMEANDTYPE = 12;

  private static final byte CONSTANT_METHODHANDLE = 15;

  private static final byte CONSTANT_METHODTYPE = 16;

  private static final byte CONSTANT_INVOKEDYNAMIC = 18;

  private transient String[] strings;

  private Map<Integer, Integer> classIndex;

  private List<Integer> methodOrFieldDescriptorIndex;

  private List<Integer> stringIndex;

  private byte[] buf = new byte[1024];

  private final String expectedClassName;

  private static final Pattern FIND_CLASS_NAME = Pattern.compile("L(\\w+(/\\w+)*(\\$\\w+)?)(<[^>]+>)?;");

  private String className;

  private final Set<String> dependsOn = new HashSet<>();

  /**
   * Parse class content and return new instance.
   *
   * @param ins class content
   * @param name expected class name
   * @throws IOException
   */
  public static ClassAndDependencyInfo parse(InputStream ins, String name) throws IOException
  {
    LOG.debug("parsing {}", name);
    return new ClassAndDependencyInfo(ins, name);
  }

  private ClassAndDependencyInfo(InputStream ins, String name) throws IOException
  {
    this.expectedClassName = name;
    try (DataInputStream data = new DataInputStream(ins))
    {
      if (data.readInt() != MAGIC)
      {
        throw new IllegalArgumentException("not a class file (bad magic)");
      }
      skip(data, 2); // minor version
      int version = data.readShort();
      if (version > MAX_SUPPORTED_VERSION)
      {
        throw new IllegalArgumentException("classes major version " + version + " unsupported");
      }
      int poolSize = data.readShort();
      strings = new String[poolSize + 1];
      classIndex = new HashMap<>();
      methodOrFieldDescriptorIndex = new ArrayList<>();
      stringIndex = new ArrayList<>();
      int readItems = 0;

      for ( int i = 1 ; i < poolSize ; i += readItems )
      {
        readItems = readPoolEntry(i, data);
      }
      skip(data, 2); // access flags;
      className = strings[classIndex.get(Integer.valueOf(data.readShort())).intValue()].replace('/', '.');
      if (!className.equals(expectedClassName))
      {
        throw new IllegalArgumentException("Class " + className + " found but expected " + expectedClassName);
      }
    }
    registerReferencedStrings();
  }


  /**
   * Treats all the found strings which are not string constant and match the regex as field or method
   * descriptors. Note that the usage of NameAndType is not consistent, later parts of the class file
   * reference name and descriptor strings separately. We do not want to read the whole class file for
   * performance reasons.
   */
  private void registerReferencedStrings()
  {
    classIndex.values()
              .stream()
              .map(i -> strings[i.intValue()])
              .filter(s -> s.charAt(0) != '[')
              .map(n -> n.replace('/', '.'))
              .forEach(dependsOn::add);
    for ( int i = 1 ; i < strings.length ; i++ )
    {
      Integer index = Integer.valueOf(i);
      if (strings[i] != null && !stringIndex.contains(index))
      {
        addClassNames(strings[i], dependsOn);
      }
    }
    dependsOn.remove(className);
  }


  /**
   * Switch block for the different kinds of tags. Size was defined by Sun, this method just follows.
   */
  private int readPoolEntry(int index, DataInputStream data) throws IOException // NOPMD
  {
    byte tag = data.readByte();
    switch (tag)
    {
      case CONSTANT_UTF8:
        readStringValue(index, data);
        break;
      case CONSTANT_INTEGER: // not interesting
      case CONSTANT_FLOAT: // dito
      case CONSTANT_FIELDREF: // contains index of ClassInfo and NameAndType which are parsed anyway
      case CONSTANT_METHODREF: // dito
      case CONSTANT_INTERFACEMETHODREF: // dito
      case CONSTANT_INVOKEDYNAMIC:
        skip(data, 4);
        break;
      case CONSTANT_NAMEANDTYPE:
        skip(data, 2); // name
        methodOrFieldDescriptorIndex.add(Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_LONG: // not interesting
      case CONSTANT_DOUBLE: // not interesting
        skip(data, 8);
        return 2; // oracle agrees that this was a poor choice
      case CONSTANT_STRING: // String constant, do not touch!
        stringIndex.add(Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_METHODTYPE:
        methodOrFieldDescriptorIndex.add(Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_CLASS:
        classIndex.put(Integer.valueOf(index), Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_METHODHANDLE: // not interesting
        skip(data, 3);
        break;
      default:
        throw new IllegalArgumentException(expectedClassName
                                           + " is not a class, constant pool contains illegal tag " + tag);
    }
    return 1;
  }

  private void skip(InputStream ins, int num) throws IOException
  {
    int skipped = 0;
    while (skipped < num)
    {
      skipped += (int)ins.skip(num - skipped);
    }
  }



  static void addClassNames(String methodDecriptor, Collection<String> classNames)
  {
    Matcher m = FIND_CLASS_NAME.matcher(methodDecriptor);
    while (m.find())
    {
      classNames.add(m.group(1).replace('/', '.'));
    }
  }


  private void readStringValue(int index, DataInputStream data) throws IOException
  {
    int length = 0x0000FFFF & data.readShort();
    if (buf.length < length)
    {
      buf = new byte[length];
    }
    int readBytes = 0;
    while (readBytes < length)
    {
      int readThisTime = data.read(buf, readBytes, length - readBytes);
      if (readThisTime == -1)
      {
        throw new IllegalArgumentException("not a class, stream ends inexpectedly");
      }
      readBytes += readThisTime;
    }
    strings[index] = new String(buf, 0, length, StandardCharsets.UTF_8);
  }

  /**
   * Returns the name of parsed class.
   */
  public String getClassName()
  {
    return className;
  }

  /**
   * Returns names of classes this class depends on.
   */
  public Collection<String> getDependencies()
  {
    return Collections.unmodifiableCollection(dependsOn);
  }
}
