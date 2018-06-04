package de.tautenhahn.dependencies.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Finds out which classes are referenced from a given class. Instances are not thread-safe!
 *
 * @author TT
 */
public class DependencyParser
{

  private static final int MAGIC = 0xCAFEBABE;

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

  private String[] strings;

  private List<Integer> classIndex;

  private List<Integer> methodDescriptorIndex;

  private byte[] buf = new byte[1024];

  private String name;

  Pattern pattern = Pattern.compile("L(\\w+(/\\w+)*(\\$\\w+)?)(<[^>]+>)?;");

  /**
   * Returns a list of classes the given class depends on.
   *
   * @param className
   * @param ins
   * @throws IOException
   */
  @SuppressWarnings("boxing")
  public Collection<String> listDependencies(String className, InputStream ins) throws IOException
  {
    this.name = className;
    try (DataInputStream data = new DataInputStream(ins))
    {
      if (data.readInt() != MAGIC)
      {
        throw new IllegalArgumentException("not a class file (bad magic)");
      }
      skip(data, 4);
      // TODO: check version: System.out.println("Version: " + data.readShort() + "." + data.readShort());
      int poolSize = data.readShort();
      strings = new String[poolSize + 1];
      classIndex = new ArrayList<>();
      methodDescriptorIndex = new ArrayList<>();
      int readItems = 0;

      for ( int i = 1 ; i < poolSize ; i += readItems )
      {
        readItems = readPoolEntry(i, data);
      }
      HashSet<String> result = new HashSet<>();

      // Trying to parse all the strings to get those not referenced inside constant pool:
      for ( int i = 1 ; i < poolSize ; i++ )
      {
        if (strings[i] != null && strings[i].matches("\\(.*\\).*"))
        {
          addClassNames(strings[i], result);
        }
      }
      // end debug code

      classIndex.stream().map(i -> strings[i]).filter(s -> s.charAt(0) != '[').forEach(result::add);
      // methodDescriptorIndex.stream().map(i -> strings[i]).forEach(n -> addClassNames(n, result));
      result.remove(className.replace(".", "/"));
      return result;
    }
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
      case CONSTANT_INTEGER:
      case CONSTANT_FLOAT:
      case CONSTANT_FIELDREF:
      case CONSTANT_METHODREF:
      case CONSTANT_INTERFACEMETHODREF:
      case CONSTANT_INVOKEDYNAMIC:
        skip(data, 4);
        break;
      case CONSTANT_NAMEANDTYPE:
        skip(data, 2);
        methodDescriptorIndex.add(Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_LONG:
      case CONSTANT_DOUBLE:
        skip(data, 8);
        return 2; // oracle agrees that this was a poor choice
      case CONSTANT_STRING:
        skip(data, 2);
        break;
      case CONSTANT_METHODTYPE:
        methodDescriptorIndex.add(Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_CLASS:
        classIndex.add(Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_METHODHANDLE:
        skip(data, 3);
        break;
      default:
        throw new IllegalArgumentException(name + " is not a class, constant pool contains illegal tag "
                                           + tag);
    }
    return 1;
  }

  private void skip(InputStream ins, int num) throws IOException
  {
    int skipped = 0;
    while (skipped < num)
    {
      skipped += ins.skip(num - skipped);
    }
  }



  void addClassNames(String methodDecriptor, Collection<String> classNames)
  {
    Matcher m = pattern.matcher(methodDecriptor);
    while (m.find())
    {
      classNames.add(m.group(1));
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
}
