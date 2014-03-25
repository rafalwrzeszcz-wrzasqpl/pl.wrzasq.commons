<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @author Rafał Wrzeszcz <rafal.wrzeszcz@wrzasq.pl>
# @copyright 2014 © by Rafał Wrzeszcz - Wrzasq.pl.
# @version 0.0.1
# @since 0.0.1
# @category ChillDev-Commons
# @subcategory Exception
-->

# Formatting

Often you need to present or log exceptions you need to format them somehow, and now always with full, default representation with stacktrack and such stuff. *ChillDev-Commons-Exception* provides `pl.chilldev.commons.exception.ExceptionFormatter` interface together with sample simple formatter (`ExceptionFormatter.SIMPLE_FORMAT`) - you can use the interface to implement your own formats or use pre-defined formatters as formatting filters, before printing exception info.

## Implementing own formatter

It's easy to implement own formatters as interface contains only one method:

```java
import pl.chilldev.commons.exception.ExceptionFormatter;

public class OnlyMessageExceptionFormatter
    implements ExceptionFormatter
{
    public String format(Throwable error)
    {
        return error.getMessage();
    }
}
```
