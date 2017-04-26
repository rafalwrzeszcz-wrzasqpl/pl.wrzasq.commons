<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Conversion utils

Various conversion methods are provided by `pl.chilldev.commons.data.ConvertUtils` class as static methods.

You can convert `org.springframework.data.domain.Sort` model into *URL* parameters using `extractSort()` method. It converts model into set of string parameters:

```java
String queryString = "sort=" + String.join(",", ConvertUtils.extractSort(sort));
```

Another useful method is `buildPageFromResources()` - you can use it to return paged HATEOAS resources into **Spring Data** `Page` model (this can be used directly in your **Feign** client for example):

```java
    @RequestLine("GET /tracks/search/users?user={user}")
    PagedResources<TrackTransfer> getUserTracks(@Param("user") String user, Pageable request);

    default Page<TrackTransfer> getUserTracksPage(String user, Pageable request)
    {
        return FeignClientFactory.buildPageFromResources(this.getUserTracks(user, request), request);
    }
```
