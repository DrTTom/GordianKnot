package de.tautenhahn.dependencies.reports;


import static org.assertj.core.api.Assertions.assertThat;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import org.junit.jupiter.api.Test;
import org.slf4j.simple.SimpleLoggerConfiguration;


/**
 * Unit test for finding unreferenced stuff and libraries which possibly can be replaced.
 */
public class TestUnreferenced
{

    /**
     * Creates a report about current project.
     */
    @Test
    public void report()
    {
        Filter filter = new Filter();
        filter.addIgnoredClassName(".*\\.Alien");
        ParsedClassPath classPath = ParsedClassPath.getCurrentClassPath();
        ContainerNode root = new ProjectScanner(filter).scan(classPath);
        Unreferenced systemUnderTest = Unreferenced.forProject(root, filter, classPath).withLimits(2, 2).create();
        String onlyClassUnsingGson = "Server$JsonTransformer";
        assertThat(systemUnderTest.toString()).as("report").contains(onlyClassUnsingGson);
        assertThat(systemUnderTest.getRarelyUsedJars().get(0).getNodeName()).as("gson lib").startsWith("jar:");
        assertThat(systemUnderTest.getUnreferencedClasses()).as("unref classes").isEmpty();
        assertThat(systemUnderTest.getUnreferencedJars()).as("unref jars").isNotNull();
        assertThat(systemUnderTest.getLittleUsedJars()).as("small contrib jars").isNotEmpty();
    }

    /**
     * Application needs slf4j-simple at runtime but source code does not. Referencing one class from that jar to get a
     * "contributes too few classes" warning.
     */
    public SimpleLoggerConfiguration getJustADummy()
    {
        return null;
    }
}
