<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2016 - 2017, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

Text formatter feature holds formatters for different text formats allowing generating **(X)HTML** code from various
source types.

## Usage

Central class for text processing is `pl.wrzasq.commons.text.TextFormatter` - it aggregates multiple format handlers.
You can use its instance to format source text in any registered format:

```kotlin
val formatter = Formatter()
formatter.registerFormatter("plain", PlainTextFormatter())
formatter.registerFormatter("html", HtmlFormatter())
formatter.registerFormatter("markdown", MarkdownFormatter())

// formats using plain text
val plainText = formatter.format("plain", "<foo bar>")

// formats using HTML
val html = formatter.format("html", "<p>foo</p>")

// formats using Markdown
val markdown = formatter.format("markdown", "**foo**")

// throws exception because of unknown format
val unknown = formatter.format("unknown", "foo")
```

## Supported formats

### Plain text

Handled by `pl.wrzasq.commons.text.formatter.PlainTextFormatter`. It returns HTML snippet that with all HTML special
characters replaced by entities to avoid their interpretation. Also replaces new line characters with `<br/>` to map all
lines.

```kotlin
val formatHandler = PlainTextFormatter()

// generates "foo &lt;bar&gt;<br/>baz"
formatHandler.transform("foo <bar>\nbaz")
```

### (X)HTML

Handled by `pl.wrzasq.commons.text.formatter.HtmlFormatter`. Since we operate in (X)HTML by default this formatter does
literally nothing - returns input text untouched.

```kotlin
val formatHandler = HtmlFormatter()

// returns same input text
formatHandler.transform("foo <bar>\nbaz")
```

### Markdown

Handled by `pl.wrzasq.commons.text.formatter.MarkdownFormatter`. Formats
[Markdown](https://daringfireball.net/projects/markdown/syntax) into HTML. It uses [Pegdown](http://pegdown.org) with
all available extensions.

```kotlin
val formatHandler = MarkdownFormatter()

// genrates "<p><strong>foo</strong> <em>bar</em></p>"
formatHandler.transform("**foo** _bar_")
```

## Custom implementation

Apart from existing formatters there is an interface `pl.wrzasq.commons.text.formatter.FormatterInterface`. It can be
used for implementing any custom format handler:

```kotlin
class MyTextFormatter : FormatterInterface {
    fun transform(text: String): String {
        return "<p>" + text.replaceAll("\n", "</p><p>") + "</p>"
    }
}
```
