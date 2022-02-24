<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2014 - 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# WrzasqPl-Commons

**WrzasqPl-Commons** is a general-purpose **Java library**.

[![Build Status](https://github.com/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons/actions/workflows/build.yaml/badge.svg)](https://github.com/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons/actions)

# Requirements

**Note:** These libraries require **Java 11**.

Whenever any of contained libraries need to do anything related with logging they rely on
[*SLF4J*](https://www.slf4j.org/), so you need to include logging facility implementation in your project (we recommend
[*Logback*](https://logback.qos.ch/)).

Anything else? Well, you need *Git* and *Maven* to check out snapshot sources if you want to work on the project.
However, if you want to use the library, then there are no other dependencies.

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
    <artifactId>commons-text</artifactId>
    <version>${pl.wrzasq.commons.version}</version>
</dependency>
```

You can of course pick only those parts that you need directly.

# Modules

**WrzasqPl-Commons** consists of various sub-modules, each of which is a separate `.jar` library:

-   [**AWS**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-aws/): helper routines for working
    with *Amazon Web Services*,
-   [**Client**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-client/): web service client
    building utilities,
-   [**Text**](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons/commons-text/): various text processing
    utilities.

# Resources

-   [GitHub page with API documentation](https://rafalwrzeszcz-wrzasqpl.github.io/pl.wrzasq.commons)
-   [Contribution guide](https://github.com/rafalwrzeszcz-wrzasqpl/.github/blob/master/CONTRIBUTING.md)
-   [Issues tracker](https://github.com/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons/issues)
-   [Maven packages](https://search.maven.org/search?q=g:pl.wrzasq.commons)
-   [Wrzasq.pl @ GitHub](https://github.com/rafalwrzeszcz-wrzasqpl)
-   [Wrzasq.pl @ Facebook](https://www.facebook.com/wrzasqpl)
-   [Wrzasq.pl @ LinkedIn](https://www.linkedin.com/company/wrzasq-pl/)
-   [Post on Wrzasq.pl](https://wrzasq.pl/blog/chilldev-commons-java-library.html)

# Authors

This project is brought to you by [Rafał Wrzeszcz - Wrzasq.pl](https://wrzasq.pl) and published under
[MIT license](https://github.com/rafalwrzeszcz-wrzasqpl/pl.wrzasq.commons/tree/master/LICENSE).

List of contributors:

-   [Rafał "Wrzasq" Wrzeszcz](https://github.com/rafalwrzeszcz) ([wrzasq.pl](https://wrzasq.pl)).
