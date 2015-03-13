<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2014 - 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# ChillDev-Commons

**ChillDev-Commons** is a general-purpose **Java library**.

[![Build Status](https://travis-ci.org/chilloutdevelopment/pl.chilldev.commons.svg)](https://travis-ci.org/chilloutdevelopment/pl.chilldev.commons)
[![Coverage Status](https://coveralls.io/repos/chilloutdevelopment/pl.chilldev.commons/badge.png?branch=develop)](https://coveralls.io/r/chilloutdevelopment/pl.chilldev.commons)
[![Dependency Status](https://www.versioneye.com/user/projects/533c81027bae4b3fa50001f2/badge.png)](https://www.versioneye.com/user/projects/533c81027bae4b3fa50001f2)

# Requirements

**Note:** These libraries require **Java 7**.

Whenever any of contained libraries need to do anything related with logging they rely on [*SLF4J*](http://www.slf4j.org/), so you need to include logging facility implementation in your project (we recommend [*Logback*](http://logback.qos.ch/)).

Anything else? Well, you need *Git* and *Maven* to checkout snapshot sources if you want to work on the project. But if you want to use the library, then there are no other dependencies.

Only one exception is `commons-daemon` library which depends on [*Apache Commons Daemon*](http://commons.apache.org/proper/commons-daemon/) but it makes no sense to build it without that.

# Installation

You can use libraries in your `pom.xml` by defining following dependencies:

```
<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-concurrent</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-daemon</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-exception</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-text</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>
```

You can of course pick only those parts that you need directly.

# Modules

**ChillDev-Commons** consists of various sub-modules, each of which is a separate `.jar` library:

-   [**Concurrent**](http://chilloutdevelopment.github.io/pl.chilldev.commons/commons-concurrent/): with concurrency and asynchronous goods,
-   [**Daemon**](http://chilloutdevelopment.github.io/pl.chilldev.commons/commons-daemon/): helper routines for handling *Apache Commons Daemon* services,
-   [**Exception**](http://chilloutdevelopment.github.io/pl.chilldev.commons/commons-exception/): with exceptions handling and processing routines.
-   [**Text**](http://chilloutdevelopment.github.io/pl.chilldev.commons/commons-text/): various text processing utilities.

# Resources

-   [GitHub page with API documentation](http://chilloutdevelopment.github.io/pl.chilldev.commons)
-   [Issues tracker](https://github.com/chilloutdevelopment/pl.chilldev.commons/issues)
-   [Maven packages](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22pl.chilldev.commons%22)
-   [Chillout Development @ GitHub](https://github.com/chilloutdevelopment)
-   [Chillout Development @ Facebook](http://www.facebook.com/chilldev)
-   [Post on Wrzasq.pl](http://wrzasq.pl/blog/chilldev-commons-java-library.html)

# Contributing

Do you want to help improving this project? Simply *fork* it and post a pull request. You can do everything on your own, you don't need to ask if you can, just do all the awesome things you want!

This project is published under [MIT license](https://github.com/chilloutdevelopment/pl.chilldev.commons/tree/master/LICENSE).

# Authors

**pl.chilldev.commons** is brought to you by [Chillout Development](http://chilldev.pl).

List of contributors:

-   [Rafał "Wrzasq" Wrzeszcz](https://github.com/rafalwrzeszcz) ([wrzasq.pl](http://wrzasq.pl)).
