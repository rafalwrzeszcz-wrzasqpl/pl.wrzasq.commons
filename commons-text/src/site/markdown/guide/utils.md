<!---
# This file is part of the ChillDev-Web.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

You can use some of the features provided by **ChillDev-Web** also directly in the code, without need for using **Facelets** tags.

## TextUtils

Useful text routines are provided by the class `pl.chilldev.web.core.util.TextUtils`.

### truncate()

You can use `truncate()` method to truncate the text:

```java
String text = "Hello world!";
String short;
short = TextUtils.truncate(text, 7); // produces "Hello…"
short = TextUtils.truncate(text, 7, false); // produces "Hello w…"
short = TextUtils.truncate(text, 7, "."); // produces "Hello."
short = TextUtils.truncate(text, 7, ".", false); // produces "Hello w."
```

### stripTags()

To strip `HTML` markup from your code snippet you can use `stripTags()` method:

```java
String html = "<p>Hello <strong>world</strong>!</p>";
String text = TextUtils.stripTags(html); // produces just "Hello world!"
```

### format()

`format()` method is a static access to pre-configured instance of text formatter. You can set/change current text formatter with `setFormatter()` method.

```java
Formatter formatter = new Formatter();
formatter.registerFormatter("markdown", new MarkdownFormatter());

TextUtils.setFormatter(formatter);
String html = TextUtils.format("markdown", "**foo** _bar_");
```
