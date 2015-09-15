<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# URL slug generation

Slugifier is an object responsible for generating URL slug representation of given input. Usually it's used to generate URL-friendly resource identifiers. To describe this feature `pl.chilldev.commons.text.slugifier.Slugifier` interface has been introduced. It declares two methods, both named `slugify()`. One variant is responsible for generating output from single string and second one - from array of strings.

## Default implementation

The simplest implementation is provided by `pl.chilldev.commons.text.slugifier.SimpleSlugifier`. It does the following stuff:

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

You can change the delimiter with `setDelimiter()` call:

```java
import pl.chilldev.commons.text.slugifier.SimpleSlugifier;
import pl.chilldev.commons.text.slugifier.Slugifier;

public class SlugifierFactory
{
    public static Slugifier createSlugifier(String delimiter)
    {
        SimpleSlugifier slugifier = new SimpleSlugifier();
        slugifier.setDelimiter(delimiter);
        return slugifier;
    }
}
```
