# Untangle the Gordian Knot!

This is a tool for analyzing the inner dependencies of your java software
on different levels. It can be used to display interactive
dependency graphs of classes, packages, package subtrees or archives
to help you understand what the structure of your application really is
and how you can make it more similar to what you want it to be. 

You may also run the analysis as part of your automatic system tests to
make sure that in an ongoing development process the structure stays clean.

Once finished, GordianKnot will be able to detect
- circular dependencies
- unused classes
- missing classes
- unused libraries
- libraries which may be candidates for replacement
- problems with packaging your classes into archives
- and more to come

Do not accept dependencies which are more tightly knit than the famous
example from greek mythology! Untangle it!



