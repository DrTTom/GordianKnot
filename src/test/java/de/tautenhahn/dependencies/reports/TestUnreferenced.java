package de.tautenhahn.dependencies.reports;

import org.junit.Test;

import de.tautenhahn.dependencies.parser.ClassPathUtils;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ProjectScanner;


public class TestUnreferenced
{

  @Test
  public void report()
  {
    ContainerNode root = new ProjectScanner(new Filter()).scan(ClassPathUtils.getClassPath());
    Unreferenced.ReportConfig cfg = new Unreferenced.ReportConfig();
    cfg.setLoader(Thread.currentThread().getContextClassLoader());
    Unreferenced systemUnderTest = new Unreferenced(root, cfg);

    System.out.println(systemUnderTest);
  }
}
