<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# JSON extras

As the name says, [**JSON-RPC**](http://www.jsonrpc.org/specification) utilizes [**JSON**](http://json.org/) for data representation. By analogy, [**JSON-RPC 2.0 Base**](http://software.dzhuvinov.com/json-rpc-2.0-base.html) librar which handles RPC layer utilizes [**json-smart**](https://github.com/netplex/json-smart-v2) library for JSON serialization. Unfortunately it is very limited. It's not simple to fill all the gaps and refactor it from outside, but some improvements can be done. This package contains so additional extras that can be used out of the box.

## String-dumping writer

`pl.chilldev.commons.jsonrpc.json.writer.StringDumpingWriter` allows to register serializer for any type that should be dumped into JSON using it's string representation - for example `java.util.UUID`:

```java
import java.util.UUID;

import net.minidev.json.JSONValue;

import org.joda.time.DateTime;

import pl.chilldev.commons.jsonrpc.json.writer.StringDumpingWriter;

public class App
{
    public static void main(String[] args)
    {
        JSONValue.defaultWriter.registerWriter(new StringDumpingWriter(), UUID.class);
        JSONValue.defaultWriter.registerWriter(new StringDumpingWriter(), DateTime.class);
    }
}
```
