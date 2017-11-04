<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Usage

When working with micro-services having a common and scalable security layer across entire infrastructure is critical. For **HTTP** based services such security layer very often relies on `Authorization` layer (like **OAuth**, **JWT** and other technologies). As such security layer is often unified across your entire architecture, in some cases you may simply forward the incoming authorization string to access downstream resources. For instance if you have API gateway service in place that forwards the calls to downstream domain services it should be transparent for the client.

`pl.chilldev.commons.client.interceptor.AuthorizationForwarder` class is designed to work with **Spring** application to execute downstream calls on behalf on enduser:

```java
interface UsersClient
{
    @RequestLine("GET /me")
    User getCurrentProfile();
}

Collection<Consumer<Feign.Builder>> yourConfigurators = new ArrayList<>();
yourConfigurators.add(
    builder -> builder
        // about obtaining the bean read below
        .requestInterceptor(yourAuthorizationForwarderBean)
);

FeignClientFactory factory = new BaseFeignClientFactory(yourConfigurators);

UsersClient usersClient = factory.createClient(UsersClient.class, "http://users.internal");

@Controller
public class ProfileController
{
    @Autowired
    private UsersClient usersClient;

    @RequestMapping("/profile")
    public String profile(Model model)
    {
        model.put("profile", this.usersClient.getCurrentProfile());
    }
}
```

Sometimes you may however want to specify custom authorization for the request, for example when you execute request on behalf of your service, not your user. If the `Authorization` header is already specified, it won't be overriden:

```java
interface UsersClient
{
    // this method will use incoming header value
    @RequestLine("GET /me")
    User getCurrentProfile();

    // this method will use header value specified by parameter
    @RequestLine("GET /me")
    @Headers("Authorization: {apiKey}")
    User getApplicationProfile(@Param("apiKey") String apiKey);
}
```

## Obtaining the bean

`AuthorizationForwarder` is designed to work as a **Spring** [request-scope bean](https://docs.spring.io/spring/docs/5.0.1.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-other). The class is annotated so you can use component scanning to automatically instantiate it:

```java
@Configuration
@ComponentScan("pl.chilldev.commons.client")
public class FeignConfiguration
{
}
```
