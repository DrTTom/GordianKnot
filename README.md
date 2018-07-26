# Untangle the Gordian Knot!
![Logo](src/main/resources/frontend/knot.png)

This is a tool for analyzing the inner dependencies of your java software
on different levels. It can be used to display interactive
dependency graphs of classes, packages, package subtrees or archives
to help you understand what the structure of your application really is
and how you can make it more similar to what you want it to be.

You may also run the analysis as part of your automatic system tests to
make sure that in an ongoing development process the structure stays clean.

GordianKnot is able to detect
- circular dependencies
- unused classes
- unused libraries
- missing classes
- libraries which may be candidates for replacement

Once finished, it will also report
- problems with packaging your classes into archives
- security problems due to unclean interfaces between components
- and more to come

To develop a software which is understandable and safe, keep an eye on its inner dependencies.
Do not wait until the entanglement becomes famous!

# Usage as Application

Get the class path of your software. You can always print it out directly from inside your application.
Log the following expression or print it out:

```java
System.getEnv("java.class.path")
```
With maven call something like `mvn dependency:build-classpath -Dmdep.outputFile=cp.txt`.

If you are using gradle, you could call `println sourceSets.main.runtimeClasspath.asPath` to get the path.
However, GordianKnot will do that for you, just specify your gradle build file instead of the class path
itself in the call described below.

Once you got the class path, call

```
GordianKnot <classpath> [project-name]
```
where `classpath` is the class path of your software (output of above expression) and `project-name` 
is the name of your project.
To avoid very long command line parameters, specify as `classpath` the name of a text file (ending with ".txt") containing
the class path. In that case, `project-name` defaults to the name of that file.

This starts a local web server and displays a link to the output page.

# Usage as Tests

As long as there is no official repository used, call `gradle publishToMavenLocal` in this project and add reposity 
`mavenLocal()` to your target gradle project (for maven you may skip this entry).
Include the following dependency into the test compile class path of your project:

```
de.tautenhahn.dependencies:GordianKnotTest:0.3
```
The following tests classes are provided:

```
de.tautenhahn.dependencies.commontests.TestCycles
de.tautenhahn.dependencies.commontests.TestUnreferencedElements
more to come
```
See API documentation of these classes to find out whether some adaption of the tests is needed for your project and how to achieve that. Add the tests to your project, usually by adding them to some test suite.

In case a test fails, verbose information abut the reason will be written into the log file.

# Coming features

- class path elements can be un-selected (needed ?)
- specified nodes can be hidden
- diverse help texts and tool tips
- some colored traffic lights on the reports page
- standard license
- separate test lib with pom available in some repository
