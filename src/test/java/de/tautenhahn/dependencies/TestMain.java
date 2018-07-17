package de.tautenhahn.dependencies;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import spark.Service;
import spark.Spark;


/**
 * Unit test for the main class and the server. Does require free port.
 *
 * @author TT
 */
public class TestMain
{

  private static final int TEST_PORT = 4765;

  /**
   * Use special port and do not start browser.
   */
  @BeforeClass
  public static void setUpStatic()
  {
    Main.fireFoxDisabled = true;
    Spark.port(TEST_PORT);
  }

  private static String withFakedConsole(Runnable task) throws IOException // NOPMD no threads here!
  {

    try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(bout, true, "UTF-8"))
    {
      Main.out = out;
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
    String output = withFakedConsole(() -> Main.main("-H"));
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
      Service.ignite();
      String output = withFakedConsole(() -> Main.main("src/test/resources/DummyProject.txt"));
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
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("OPTIONS");
      assertThat("header", conn.getHeaderField("Allow"), nullValue());
    }
    finally
    {
      Spark.stop();
    }
  }

  /**
   * Asserts that gradle can be called to access runtime class path.
   */
  @Test
  public void useGradle() throws IOException
  {
    try
    {
      Service.ignite();
      Main.main("build.gradle");
      Spark.awaitInitialization();
      assertThat("just calling",
                 new URL("http://localhost:" + TEST_PORT + "/view/classpath").getContent(),
                 notNullValue());
      URL url = new URL("http://localhost:" + TEST_PORT + "/view/classpath");
      try (InputStream ins = url.openStream();
        Scanner s = new Scanner(ins, "UTF-8");
        Scanner scanner = s.useDelimiter("\t"))
      {
        assertThat("classpath", scanner.next(), containsString("build/classes/java/main"));
      }
    }
    finally
    {
      Spark.stop();
    }
  }
}
