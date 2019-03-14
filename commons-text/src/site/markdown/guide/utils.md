<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2016, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

## TextUtils

Useful text routines are provided by the class `pl.wrzasq.commons.text.html.Utils`.

### truncate()

You can use `truncate()` method to truncate the text:

```java
String text = "Hello world!";
String short;
short = Utils.truncate(text, 7); // produces "Hello…"
short = Utils.truncate(text, 7, false); // produces "Hello w…"
short = Utils.truncate(text, 7, "."); // produces "Hello."
short = Utils.truncate(text, 7, ".", false); // produces "Hello w."
```

### format()

`format()` method is a static access to pre-configured instance of text formatter. You can set/change current text formatter with `setFormatter()` method.

```java
Formatter formatter = new Formatter();
formatter.registerFormatter("markdown", new MarkdownFormatter());

Utils.setFormatter(formatter);
String html = TextUtils.format("markdown", "**foo** _bar_");
```
