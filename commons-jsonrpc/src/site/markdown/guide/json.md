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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import net.minidev.json.JSONValue;

import pl.chilldev.commons.jsonrpc.json.writer.DateTimeWriter;

public class App
{
    public static void main(String[] args)
    {
        JSONValue.defaultWriter.registerWriter(
            new DateTimeWriter(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            OffsetDateTime.class
        );
    }
}
```

## DateTime writer

`pl.chilldev.commons.jsonrpc.json.writer.DateTimeWriter` allows for setting default formatter for Java8 Time API objects by specifying `DateTimeFormatter`:

```java
import java.util.UUID;

import net.minidev.json.JSONValue;

import pl.chilldev.commons.jsonrpc.json.writer.StringDumpingWriter;

public class App
{
    public static void main(String[] args)
    {
        JSONValue.defaultWriter.registerWriter(new StringDumpingWriter(), UUID.class);
    }
}
```

## Sort writer

`pl.chilldev.commons.jsonrpc.json.writer.SortWriter` is capable of dumping **Spring Data** `Sort` objects into JSON. They are dumped as a list of two-element arrays, where first element is property name and second one is sorting direction:

```java
Sort value = new Sort(
    new Sort.Order(Sort.Direction.ASC, "id"),
    new Sort.Order(Sort.Direction.DESC, "name")
);

// will become
//[["id","ASC"],["name","DESC"]]
// this is the format that our ParamsRetriver recognizes when parsing the params from JSON
```

Usage is analogical to other writers:

```java
import net.minidev.json.JSONValue;

import org.springframework.data.domain.Sort;

import pl.chilldev.commons.jsonrpc.json.writer.SortWriter;

public class App
{
    public static void main(String[] args)
    {
        JSONValue.defaultWriter.registerWriter(new SortWriter(), Sort.class);
    }
}
```

## Enhanced params retriever

`pl.chilldev.commons.jsonrpc.json.ParamsRetriever` is an extension of `com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever` (we only support named parameters!) which provides extraction methods for some additional common types:

-   `java.util.UUID` via `.getUuid()` and `.getOptUuid()`;
-   `org.springframework.data.domain.Sort` via `getSort()`: it extracts list of order criteria (of form `[["field1","ASC|DESC"], ["field2","ASC|DESC"], ["fieldN","ASC|DESC"]]`) into `spring-data-core` object;
-   `org.springframework.data.domain.Pageable` via `getPageable()`: it extracts page request specification by composing it from page number, page limit and sort parameters.

**Note:** Even though `ParamsRetriever` class provides methods for `Sort`, `Pageable` etc., `spring-data-core` is marked as optional dependency to reduce footprint.

### Beans

Our extended parameters retriever class also provides `.getBean()` method which allows for unserializing properties as a bean object.

```java
import pl.chilldev.commons.jsonrpc.json.ParamsRetriever;

// …somewhere in the code
ParamsRetriever params = new ParamsRetriever(request); // can also be constructed by passing a map directly

/*
assume the request:
{"id":1,"name":"Chillout Development"}
*/
MyBean myBean = params.getBean(MyBean.class);

// you can also fetch bean from the parameter sub-scope

/*
assume the request:
{"data":{"id":1,"name":"Chillout Development"}}
*/
MyBean myBean = params.getBean("data", MyBean.class);
```

**Note:** We use [**Jackson**](http://wiki.fasterxml.com/JacksonHome) for data binding.
