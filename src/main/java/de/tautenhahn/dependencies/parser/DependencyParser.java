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

  private static final byte CONSTANT_Utf8 = 1;

  private static final byte CONSTANT_Integer = 3;

  private static final byte CONSTANT_Float = 4;

  private static final byte CONSTANT_Long = 5;

  private static final byte CONSTANT_Double = 6;

  private static final byte CONSTANT_Class = 7;

  private static final byte CONSTANT_String = 8;

  private static final byte CONSTANT_FieldRef = 9;

  private static final byte CONSTANT_MethodRef = 10;

  private static final byte CONSTANT_InterfaceMethodRef = 11;

  private static final byte CONSTANT_NameAndType = 12;

  private static final byte CONSTANT_MethodHandle = 15;

  private static final byte CONSTANT_MethodType = 16;

  private static final byte CONSTANT_InvokeDynamic = 18;

  private String[] strings;

  private List<Integer> classIndex;

  private List<Integer> methodDescriptorIndex;

  private byte[] buf = new byte[1024];

  /**
   * Returns a list of classes the given class depends on.
   *
   * @param name
   * @param ins
   * @throws IOException
   */
  public Collection<String> listDependencies(String name, InputStream ins) throws IOException
  {
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

      classIndex.stream().map(i -> strings[i]).filter(s -> !s.startsWith("[")).forEach(result::add);
      // methodDescriptorIndex.stream().map(i -> strings[i]).forEach(n -> addClassNames(n, result));
      result.remove(name.replace(".", "/"));
      return result;
    }
  }

  private int readPoolEntry(int index, DataInputStream data) throws IOException
  {
    byte tag = data.readByte();
    switch (tag)
    {
      case CONSTANT_Utf8:
        readStringValue(index, data);
        break;
      case CONSTANT_Integer:
      case CONSTANT_Float:
      case CONSTANT_FieldRef:
      case CONSTANT_MethodRef:
      case CONSTANT_InterfaceMethodRef:
      case CONSTANT_InvokeDynamic:
        skip(data, 4);
        break;
      case CONSTANT_NameAndType:
        skip(data, 2);
        methodDescriptorIndex.add(Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_Long:
      case CONSTANT_Double:
        skip(data, 8);
        return 2; // oracle agrees that this was a poor choice
      case CONSTANT_String:
        skip(data, 2);
        break;
      case CONSTANT_MethodType:
        methodDescriptorIndex.add(Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_Class:
        classIndex.add(Integer.valueOf(data.readShort()));
        break;
      case CONSTANT_MethodHandle:
        skip(data, 3);
        break;
      default:
        throw new IllegalArgumentException("not a class, constant pool contains illegal tag " + tag);
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

  Pattern pattern = Pattern.compile("L(\\w+(/\\w+)*(\\$\\w+)?)(<[^>]+>)?;");

  void addClassNames(String methodDecriptor, Collection<String> classNames)
  {
    Matcher m = pattern.matcher(methodDecriptor);
    int pos = 0;
    while (m.find())
    {
      pos = m.end();
      classNames.add(m.group(1));
    }
  }


  private void readStringValue(int index, DataInputStream data) throws IOException
  {
    int length = data.readShort();
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
