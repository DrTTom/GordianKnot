package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

import de.tautenhahn.dependencies.reports.TestCyclicDependencies;


/**
 * Unit test for the server. Requires that the server is not running in parallel.
 *
 * @author TT
 */
public class TestServer
{

  /**
   * Special sub-class which does not neet a free port.
   */
  static class InactiveServer extends Server
  {

    @Override
    void startSpark()
    {
      // deactivated to allow unit tests without using port
    }
  }

  /**
   * Just calling the main method.
   */
  @Test
  public void help()
  {
    Server.main("-H");
  }

  /**
   * Simple smoke test until other routes are up.
   */
  @Test
  public void getView()
  {
    assertThat("just creating a cycle to report", TestCyclicDependencies.class, notNullValue());
    Server server = new InactiveServer();
    server.init(Paths.get("build", "classes", "java", "test").toAbsolutePath().toString(), "dummy");
    server.showOnlyCycles();

    String result = new Server.JsonTransformer().render(server.getDisplayableGraph(null, null));
    assertThat("view result", result, containsString("rest"));
  }

}
