<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2014 - 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# ChillDev-Commons

**ChillDev-Commons** is a general-purpose **Java library**.

[![Build Status](https://travis-ci.com/chilloutdevelopment/pl.chilldev.commons.svg)](https://travis-ci.org/chilloutdevelopment/pl.chilldev.commons)
[![Coverage Status](https://coveralls.io/repos/chilloutdevelopment/pl.chilldev.commons/badge.png?branch=develop)](https://coveralls.io/r/chilloutdevelopment/pl.chilldev.commons)
[![Known Vulnerabilities](https://snyk.io/test/github/chilloutdevelopment/pl.chilldev.commons/badge.svg)](https://snyk.io/test/github/chilloutdevelopment/pl.chilldev.commons)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/chilloutdevelopment/pl.chilldev.commons) [![Join the chat at https://gitter.im/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons](https://badges.gitter.im/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons.svg)](https://gitter.im/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Requirements

**Note:** These libraries require **Java 8**.

Whenever any of contained libraries need to do anything related with logging they rely on [*SLF4J*](https://www.slf4j.org/), so you need to include logging facility implementation in your project (we recommend [*Logback*](https://logback.qos.ch/)).

Anything else? Well, you need *Git* and *Maven* to checkout snapshot sources if you want to work on the project. But if you want to use the library, then there are no other dependencies.

Only one exception is `commons-daemon` library which depends on [*Apache Commons Daemon*](https://commons.apache.org/proper/commons-daemon/) but it makes no sense to build it without that.

# Installation

You can use libraries in your `pom.xml` by defining following dependencies:

```xml
<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-aws</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-client</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-daemon</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-data</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-db</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-jwt</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-text</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.chilldev.commons</groupId>
    <artifactId>commons-web</artifactId>
    <version>${pl.chilldev.commons.version}</version>
</dependency>
```

You can of course pick only those parts that you need directly.

# Modules

**ChillDev-Commons** consists of various sub-modules, each of which is a separate `.jar` library:

-   [**AWS**](https://chilloutdevelopment.github.io/pl.chilldev.commons/commons-aws/): helper routines for eorking with *Amazon Web Services*,
-   [**Client**](https://chilloutdevelopment.github.io/pl.chilldev.commons/commons-client/): web service client building utilities,
-   [**Daemon**](https://chilloutdevelopment.github.io/pl.chilldev.commons/commons-daemon/): helper routines for handling *Apache Commons Daemon* services,
-   [**Data**](https://chilloutdevelopment.github.io/pl.chilldev.commons/commons-data/): helper routines for handling *Spring Data* structures,
-   [**DB**](https://chilloutdevelopment.github.io/pl.chilldev.commons/commons-db/): base database and model-related structures and routines,
-   [**JWT**](https://chilloutdevelopment.github.io/pl.chilldev.commons/commons-jwt/): *JSON Web Tokens* utilities,
-   [**Text**](https://chilloutdevelopment.github.io/pl.chilldev.commons/commons-text/): various text processing utilities.
-   [**Web**](https://chilloutdevelopment.github.io/pl.chilldev.commons/commons-web/): web-apps related stuff.

# Resources

-   [GitHub page with API documentation](https://chilloutdevelopment.github.io/pl.chilldev.commons)
-   [Contribution guide](https://github.com/chilloutdevelopment/pl.chilldev.commons/blob/develop/CONTRIBUTING.md)
-   [Issues tracker](https://github.com/chilloutdevelopment/pl.chilldev.commons/issues)
-   [Maven packages](https://search.maven.org/search?q=g:pl.chilldev.commons)
-   [Chillout Development @ GitHub](https://github.com/chilloutdevelopment)
-   [Chillout Development @ Facebook](https://www.facebook.com/chilldev)
-   [Post on Wrzasq.pl](https://wrzasq.pl/blog/chilldev-commons-java-library.html)

# Authors

This project is published under [MIT license](https://github.com/chilloutdevelopment/pl.chilldev.commons/tree/master/LICENSE).

**pl.chilldev.commons** is brought to you by [Chillout Development](https://chilldev.pl).

List of contributors:

-   [Rafał "Wrzasq" Wrzeszcz](https://github.com/rafalwrzeszcz) ([wrzasq.pl](https://wrzasq.pl)).
