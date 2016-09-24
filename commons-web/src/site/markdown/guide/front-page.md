<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Front page servlet filter

## Concept

Front page filter is capable to render response with page view around JSON API response. Thanks to it you can handle web page requests on top of your JSON (not necessarily RESTful) API transparently without backend web application and forward initial JSON response to your web client application. What's important here, is that filter will preserve (and also expose in the view) API HTTP response code.

Thanks to that you can build single-page rich-client web application, having your plain JSON API service serving websites with proper response codes and having initial API response already in your client application without the need to execute it over AJAX call.

## Configuration

You can configure view name used by the filter by setting property in you application context (usually by defining it in one of your property sources) named `chillDev.frontFilter.viewName`. For example in the properties file:

```
chillDev.frontPageFilter.viewName = layout/default
```

## Usage

To you this filter you need to add it to your servlet descriptor (usually `WEB-INF/web.xml`). As the filter is designed to be a **Spring** bean, you will usually have to use `DelegatingFilterProxy` as a placeholder, unless you provision your web-tier beans with **Spring** container.

```xml
    <filter>
        <filter-name>chillDevFrontPageFilter</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>chillDevFrontPageFilter</filter-name>
        <url-pattern>/*</url-pattern>
        <!-- by handling also ERROR dispatcher we can also handle application entry point for error pages -->
        <dispatcher>REQUEST</dispatcher>
        <dispatcher>ERROR</dispatcher>
    </filter-mapping>
```
