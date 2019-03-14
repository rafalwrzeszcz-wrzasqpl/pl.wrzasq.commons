<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

Sometimes different API endpoints may return different media types (or maybe they can return negotiated content). With
single decoder it's impossible to handle multiple response types. You would need to return plain `feign.Response` and
operate on it.

To help with this problem you can use `pl.wrzasq.commons.client.codec.DelegateDecoder`. It allows you to register
type-specific decoders:

```java
Decoder fallback = new StringDecoder();
DelegateDecoder decoder = new DelegateDecoder(fallback);
decoder.registerTypeDecoder("application/json", new JacksonDecoder());
decoder.registerTypeDecoder("application/xml", new YourXmlDecoder());
```

Then you can use different response types:

```java
interface YourClient
{
    @RequestLine("GET /users")
    @Headers("Accept: application/json")
    List<User> getUsersFromJson(); // this will use JacksonDecoder

    @RequestLine("GET /users")
    @Headers("Accept: application/xml")
    List<User> getUsersFromXml(); // this will use YourXmlDecoder

    @RequestLine("GET /users")
    @Headers("Accept: text/plain")
    List<User> getUsersFromPlainText(); // this will use StringDecoder - will of course fail, but just to keep pattern
}
```
