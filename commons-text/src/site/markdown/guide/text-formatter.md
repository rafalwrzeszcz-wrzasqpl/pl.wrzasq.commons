<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2016 - 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

Text formatter feature holds formatters for different text formats allowing to generate **(X)HTML** code from various source types.

## Usage

Central class for text processing is `pl.chilldev.commons.text.TextFormatter` - it aggregates multiple format handlers. You can use it's instance to format source text in any registred format:

```java
Formatter formatter = new Formatter();
formatter.registerFormatter("plain", new PlainTextFormatter());
formatter.registerFormatter("html", new HtmlFormatter());
formatter.registerFormatter("markdown", new MarkdownFormatter());

// formats using plain text
String plainText = formatter.format("plain", "<foo bar>");

// formats using HTML
String html = formatter.format("html", "<p>foo</p>");

// formats using Markdown
String markdown = formatter.format("markdown", "**foo**");

// throws exception because of unknown format
String unknown = formatter.format("unknown", "foo");
```

## Supported formats

### Plain text

Handled by `pl.chilldev.commons.text.formatter.PlainTextFormatter`. It returns HTML snippet that with all HTML special characters replaced by entities to avoid their interpretation. Also replaces new line characters with `<br/>` to map all lines.

```java
FormatterInterface formatHandler = new PlainTextFormatter();

// generates "foo &lt;bar&gt;<br/>baz"
formatHandler.transform("foo <bar>\nbaz");
```

### (X)HTML

Handled by `pl.chilldev.commons.text.formatter.HtmlFormatter`. Since we operate in (X)HTML by default this formatter does literally nothing - returns input text untouched.

```java
FormatterInterface formatHandler = new HtmlFormatter();

// returns same input text
formatHandler.transform("foo <bar>\nbaz");
```

### Markdown

Handled by `pl.chilldev.commons.text.formatter.MarkdownFormatter`. Formats [Markdown](https://daringfireball.net/projects/markdown/syntax) into HTML. It uses [Pegdown](http://pegdown.org) with all available extensions.

```java
FormatterInterface formatHandler = new MarkdownFormatter();

// genrates "<p><strong>foo</strong> <em>bar</em></p>"
formatHandler.transform("**foo** _bar_");
```

## Custom implementation

Apart from existing formatters there is an interface `pl.chilldev.commons.text.formatter.FormatterInterface`. It can be used for implementing any custom format handler:

```java
class MyTextFormatter implements FormatterInterface
{
    public String transform(String text)
    {
        return "<p>" + text.replaceAll("\n", "</p><p>") + "</p>";
    }
}
```
