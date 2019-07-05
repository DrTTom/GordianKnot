package de.tautenhahn.dependencies.reports;

import org.junit.jupiter.api.Test;

/**
 * Checks components report about this project.
 *
 * @author TT
 */
public class TestArchitecturalMatch
{

    /**
     * TODO
     */
    @Test
    public void smoke()
    {
        ArchitecturalMatch systemUnderTest = new ArchitecturalMatch(null, null);
        systemUnderTest.toString();
    }
}
