package de.tautenhahn.dependencies.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import spark.Spark;


/**
 * Unit test for the server. Does require port free port.
 *
 * @author TT
 */
public class TestServer
{

  private static final int TEST_PORT = 4765;

  private static String withFakedConsole(Runnable task) throws IOException // NOPMD no threads here!
  {
    try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(bout, true, "UTF-8"))
    {
      Server.out = out;
      task.run();
      return new String(bout.toByteArray(), StandardCharsets.UTF_8);
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
    String output = withFakedConsole(() -> Server.main("-H"));
    assertThat("output", output, containsString("Usage:"));
  }

  /**
   * Asserts that the project name can be specified by input file.
   * 
   * @throws IOException
   */
  @Test
  public void readTextFile() throws IOException
  {
    try
    {
      Spark.port(TEST_PORT);
      String output = withFakedConsole(() -> Server.main("src/test/resources/DummyProject.txt"));
      assertThat("output", output, containsString(":" + TEST_PORT + "/index.html"));
      Spark.awaitInitialization();
      assertThat("just calling",
                 new URL("http://localhost:" + TEST_PORT + "/view/node/0/listmode/EXPANDED").getContent(),
                 notNullValue());
      URL url = new URL("http://localhost:" + TEST_PORT + "/view/name");
      try (InputStream ins = url.openStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(ins, StandardCharsets.UTF_8)))
      {
        assertThat("name", r.readLine(), is("DummyProject"));
      }
    }
    finally
    {
      Spark.stop();
    }
  }
}
