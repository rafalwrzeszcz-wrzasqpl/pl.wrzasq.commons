<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# JWT, JWK and friends

First let's explain most of these (just briefly):

-   **JWT** stands for *JSON Web Token*, it's a type of access token that encapsulates user data in an encrypted JSON structure;
-   **JWK** stands for *JSON Web Key*, it's format of storing encryption keys, usually used to verify **JWT** tokens;
-   **JWKS** stands for *JSON Web Key Set*, it's a storage of multiple **JWK** keys, usually exposed publicly at known location.

When working with **JWT** in **Java** we usually use [Auth0 JWT library](https://github.com/auth0/java-jwt) - it's provides full set of feature. Moreover [**Auth0**](https://auth0.com/) is a great cloud service for authorization and storing user data.

However as most of our products are **AWS**-based we migrated with [**Cognito**](https://aws.amazon.com/cognito/), which also uses **JWT** together with **JWK**. Even though the flow of all of the operations and algorithms are perfectly standard, there is one difference, which is not supported by **Auth0**'s library: the library only handles **JWKS** (`https://$DOMAIN/.well-known/jwks.json`) located under the root of the domain, however **Cognito** serves it from common domain by locating user pool ID in the path (`https://cognito-idp.$REGION.amazonaws.com/$POOL/.well-known/jwks.json`).

Apart from this one case **Auth0** **JWT** library is perfectly provider-agnostic and can be used to work with virtually any identity provider that produce **JWT** tokens.

Right now this is the only purpose of `commons-jwt` - to provide alternative implementation of **JWKS** loader, that will be more flexible.

With plain `auth0-jwt` library this wouldn't work (for simplicity this code targets **Spring Security**, but it's all just about **JWT** handling):

```java
@EnableWebSecurity
public class CoreSecurityConfiguration extends WebSecurityConfigurerAdapter
{
    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure(HttpSecurity http)
        throws
            Exception
    {
        JwtWebSecurityConfigurer
            .forRS256(this.audience, this.issuer)
            // this will perform all necessary JWT-related configuration
            .configure(http)
            /* your config */;
    }
}
```

But you can easily replace default **JWK** provider with our implementation to have it working with **Cognito** (and possibly many more providers):

```java
@EnableWebSecurity
public class CoreSecurityConfiguration extends WebSecurityConfigurerAdapter
{
    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    @Override
    protected void configure(HttpSecurity http)
        throws
            Exception
    {
        JwtWebSecurityConfigurer
            .forRS256(this.audience, this.issuer, this.authenticationProvider())
            // this will perform all necessary JWT-related configuration
            .configure(http)
            /* your config */;
    }

    @Bean
    public JwkProvider jwkProvider()
    {
        return new GuavaCachedJwkProvider(
            new FullUrlJwkProvider(this.issuer) // this is our implementation
        );
    }

    @Bean
    public AuthenticationProvider authenticationProvider()
    {
        return new JwtAuthenticationProvider(
            this.jwkProvider(),
            this.issuer,
            this.audience
        );
    }
}
```
