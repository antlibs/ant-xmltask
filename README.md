xmltask provides the facility for automatically editing XML files as part of an [Ant](https://ant.apache.org) build. Unlike the standard [filter](https://ant.apache.org/manual/Tasks/filter.html) task provided with Ant, it is XML-sensitive, but doesn't require you to define XSLTs.

Uses include:

* modifying configuration files for applications during builds
* inserting/removing information for J2EE deployment descriptors
* dynamically building Ant build.xml files during builds
* building and maintaining websites built with XHTML
* driving Ant via a meta build.xml to abstract out build processes
