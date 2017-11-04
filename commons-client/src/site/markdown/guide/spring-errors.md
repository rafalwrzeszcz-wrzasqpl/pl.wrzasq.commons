<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

When using **Feign** clients in **Spring** application you may find it convenient to rely on one set of exception classes. With `spring-web` package providing `HttpClientErrorException` and `HttpServerErrorException` classes you can easily detect and sometimes even handle more transparently **HTTP** error codes returned from clients. To automate the process you can apply `pl.chilldev.commons.client.codec.SpringErrorDecoder`:

```java
Collection<Consumer<Feign.Builder>> yourConfigurators = new ArrayList<>();
yourConfigurators.add(
    builder -> builder
        .errorDecoder(
            new SpringErrorDecoder(new ErrorDecoder.Default())
        )
);

FeignClientFactory factory = new BaseFeignClientFactory(yourConfigurators);

YourClient client = factory.createClient(YourClient.class, "http://service1.internal");
```

Thanks to that you can expose HTTP error codes from client upstream:

```java
@Controller
public class YourController
{
    @Autowired
    YourClient client;

    @RequestMapping("/")
    public void yourAction()
    {
        this.client.yourMethod();
    }

    @ExceptionHandler(SpringClientErrorException.class)
    public void handleClientError()
    {
        // your handling
    }
}
```

**Note:** Both `HttpClientErrorException` and `HttpServerErrorException` extend same base class `HttpStatusCodeException`.
