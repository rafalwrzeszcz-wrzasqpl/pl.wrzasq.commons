<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2018 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

Sometimes different API endpoints may return different media types (or maybe they can return negotiated content). With
single decoder it's impossible to handle multiple response types. You would need to return plain `feign.Response` and
operate on it.

To help with this problem you can use `pl.wrzasq.commons.client.codec.DelegateDecoder`. It allows you to register
type-specific decoders:

```kotlin
val fallback = StringDecoder()
val decoder = DelegateDecoder(fallback)
decoder.registerTypeDecoder("application/json", JacksonDecoder())
decoder.registerTypeDecoder("application/xml", YourXmlDecoder())
```

Then you can use different response types:

```kotlin
interface YourClient {
    @RequestLine("GET /users")
    @Headers("Accept: application/json")
    fun getUsersFromJson(): List<User> // this will use JacksonDecoder

    @RequestLine("GET /users")
    @Headers("Accept: application/xml")
    fun getUsersFromXml(): List<User> // this will use YourXmlDecoder

    @RequestLine("GET /users")
    @Headers("Accept: text/plain")
    fun getUsersFromPlainText(): List<User> // this will use StringDecoder - will fail, but just to keep pattern
}
```
