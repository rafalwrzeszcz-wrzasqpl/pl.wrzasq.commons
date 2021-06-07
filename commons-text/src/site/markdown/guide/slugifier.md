<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# URL slug generation

Slugifier is an object responsible for generating URL slug representation of given input. Usually it's used to generate
URL-friendly resource identifiers. To describe this feature `pl.wrzasq.commons.text.slugifier.Slugifier` interface has
been introduced. It declares two methods, both named `slugify()`. One variant is responsible for generating output from
single string and second one - from an array of strings.

## Default implementation

The simplest implementation is provided by `pl.wrzasq.commons.text.slugifier.SimpleSlugifier`. It does the following
stuff:

-   transcodes all known characters to ASCII charset;
-   drops all non-word (not a letter or a digit) characters;
-   replaces all whitespaces and special characters by specified delimiter (`-` by default), always single;
-   drops leading and trailing `-` characters;
-   lowercase the result.

Here are some examples:

Initial value | Generated slug
--- | ---
`FOO` | `foo`
`Chillout Development` | `chillout-development`
`Chillóut -- Devęlopment` | `chillout-development`
`-=[ TEST ]=-` | `test`

You can change the delimiter also if you want:

```kotlin
import pl.wrzasq.commons.text.slugifier.SimpleSlugifier

// default separator: "-"
val slugifier = SimpleSlugifier()
// separator of your choice
val customSlugifier = SimpleSlugifier("::")
```
