package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;


/**
 * Unit test for the server. Does not require any port.
 *
 * @author TT
 */
public class TestServer
{

  /**
   * Special sub-class which does not need a free port.
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
   *
   * @throws IOException
   */
  @Test
  public void help() throws IOException
  {
    try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); PrintStream out = new PrintStream(bout))
    {
      Server.out = out;
      Server.main("-H");
      assertThat("output", new String(bout.toByteArray(), StandardCharsets.UTF_8), containsString("Usage:"));
    }
  }

}
