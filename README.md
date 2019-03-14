<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2014 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# WrzasqPl-Commons

**WrzasqPl-Commons** is a general-purpose **Java library**.

[![Build Status](https://travis-ci.com/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons.svg)](https://travis-ci.org/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons)
[![Coverage Status](https://coveralls.io/repos/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons/badge.png?branch=develop)](https://coveralls.io/r/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons)
[![Known Vulnerabilities](https://snyk.io/test/github/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons/badge.svg)](https://snyk.io/test/github/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons)

# Requirements

**Note:** These libraries require **Java 8**.

Whenever any of contained libraries need to do anything related with logging they rely on [*SLF4J*](https://www.slf4j.org/), so you need to include logging facility implementation in your project (we recommend [*Logback*](https://logback.qos.ch/)).

Anything else? Well, you need *Git* and *Maven* to checkout snapshot sources if you want to work on the project. But if you want to use the library, then there are no other dependencies.

Only one exception is `commons-daemon` library which depends on [*Apache Commons Daemon*](https://commons.apache.org/proper/commons-daemon/) but it makes no sense to build it without that.

# Installation

You can use libraries in your `pom.xml` by defining following dependencies:

```xml
<dependency>
    <groupId>pl.wrzasq.commons</groupId>
    <artifactId>commons-aws</artifactId>
    <version>${pl.wrzasq.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.wrzasq.commons</groupId>
    <artifactId>commons-client</artifactId>
    <version>${pl.wrzasq.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.wrzasq.commons</groupId>
    <artifactId>commons-daemon</artifactId>
    <version>${pl.wrzasq.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.wrzasq.commons</groupId>
    <artifactId>commons-data</artifactId>
    <version>${pl.wrzasq.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.wrzasq.commons</groupId>
    <artifactId>commons-db</artifactId>
    <version>${pl.wrzasq.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.wrzasq.commons</groupId>
    <artifactId>commons-jwt</artifactId>
    <version>${pl.wrzasq.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.wrzasq.commons</groupId>
    <artifactId>commons-text</artifactId>
    <version>${pl.wrzasq.commons.version}</version>
</dependency>

<dependency>
    <groupId>pl.wrzasq.commons</groupId>
    <artifactId>commons-web</artifactId>
    <version>${pl.wrzasq.commons.version}</version>
</dependency>
```

You can of course pick only those parts that you need directly.

# Modules

**WrzasqPl-Commons** consists of various sub-modules, each of which is a separate `.jar` library:

-   [**AWS**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-aws/): helper routines for eorking with *Amazon Web Services*,
-   [**Client**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-client/): web service client building utilities,
-   [**Daemon**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-daemon/): helper routines for handling *Apache Commons Daemon* services,
-   [**Data**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-data/): helper routines for handling *Spring Data* structures,
-   [**DB**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-db/): base database and model-related structures and routines,
-   [**JWT**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-jwt/): *JSON Web Tokens* utilities,
-   [**Text**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-text/): various text processing utilities.
-   [**Web**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-web/): web-apps related stuff.

# Resources

-   [GitHub page with API documentation](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons)
-   [Contribution guide](https://github.com/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons/blob/develop/CONTRIBUTING.md)
-   [Issues tracker](https://github.com/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons/issues)
-   [Maven packages](https://search.maven.org/search?q=g:pl.wrzasq.commons)
-   [Wrzasq.pl @ GitHub](https://github.com/rafalwrzeszcz-wrzasqpl)
-   [Wrzasq.pl @ Facebook](https://www.facebook.com/wrzasqpl)
-   [Post on Wrzasq.pl](https://wrzasq.pl/blog/chilldev-commons-java-library.html)

# Authors

This project is published under [MIT license](https://github.com/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons/tree/master/LICENSE).

**pl.wrzasq.commons** is brought to you by [Rafał Wrzeszcz - Wrzasq.pl](https://wrzasq.pl).

List of contributors:

-   [Rafał "Wrzasq" Wrzeszcz](https://github.com/rafalwrzeszcz) ([wrzasq.pl](https://wrzasq.pl)).
