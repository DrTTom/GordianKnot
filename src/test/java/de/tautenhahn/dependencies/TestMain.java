package de.tautenhahn.dependencies;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Service;
import spark.Spark;

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
    @BeforeAll
    public static void setUpStatic()
    {
        Main.firefoxEnabled = false;
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
        assertThat(output).contains("Usage:");
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
            assertThat(output).contains(":" + TEST_PORT + "/index.html");
            Spark.awaitInitialization();
            assertThat(new URL("http://localhost:" + TEST_PORT + "/view/node/0/listmode/EXPANDED").getContent())
                .as("reponse body")
                .isNotNull();
            URL url = new URL("http://localhost:" + TEST_PORT + "/view/name");
            try (InputStream insRes = url.openStream();
                 BufferedReader r = new BufferedReader(new InputStreamReader(insRes, StandardCharsets.UTF_8)))
            {
                assertThat(r.readLine()).as("name").isEqualTo("DummyProject");
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("OPTIONS");
            assertThat(conn.getHeaderField("Allow")).isNull();;
        } finally
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
            assertThat(new URL("http://localhost:" + TEST_PORT + "/view/classpath").getContent()).isNotNull();
            URL url = new URL("http://localhost:" + TEST_PORT + "/view/classpath");
            try (InputStream insRes = url.openStream(); Scanner s = new Scanner(insRes, "UTF-8");
                 Scanner scannerRes = s.useDelimiter("\t"))
            {
                assertThat(scannerRes.next()).as("classpath").contains("build/classes/java/main");
            }
        } finally
        {
            Spark.stop();
        }
    }
}
