package de.tautenhahn.dependencies.parser;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Just an example to parse. No special meaning here.
 *
 * @author TT
 */
public class ExampleClass
{

  private static final Logger LOG = LoggerFactory.getLogger(ExampleClass.class);

  static final String NOT_A_DEPENDENCY = "(Ljava/util/concurrent/ConcurrentHashMap;)I";

  /**
   * A field with generic type.
   */
  public HashMap<String, Certificate> genericField; // NOPMD this is not a functional class



  /**
   * A method referencing a type as parameter and in its body.
   */
  @Deprecated
  public String returnSomething(Supplier<ArrayList<String>> parameter)
  {
    LOG.debug("java.util.ConcurrentModificationException");
    List<String> value = parameter.get();
    return Boolean.toString(value.isEmpty());
  }
}
