package de.tautenhahn.dependencies.testutils;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.tautenhahn.dependencies.rest.TestServer;



public class TestCyclicDependencies
{

  /**
   * TODO: make it a real test
   */
  @Test
  public void printDependency()
  {
    assertThat("just creating a cylcle to report", TestServer.class, notNullValue());
    System.out.println(new CyclicDependencies().getPackageCycleReport());
  }
}
