package de.tautenhahn.dependencies.core;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Unit tests for several nodes.
 * 
 * @author TT
 */
public class TestNode
{

  private static InnerNode root;

  @BeforeClass
  public static void init()
  {
    root = InnerNode.createRoot();
    for ( int i = 0 ; i < 3 ; i++ )
    {
      InnerNode child = root.createInnerChild("node" + i);
    }
  }

  /**
   * Asserts that dependencies of non-collapsed nodes follow the direct children only.
   */
  @Test
  public void nonCollapsedDependencies()
  {
    // TODO: need simple way to create and navigate complex structures.
  }

}
