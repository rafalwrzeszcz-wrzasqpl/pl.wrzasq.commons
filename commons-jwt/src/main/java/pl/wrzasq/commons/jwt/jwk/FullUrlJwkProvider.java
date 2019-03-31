/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.jwt.jwk;

import java.net.MalformedURLException;
import java.net.URL;

import com.auth0.jwk.UrlJwkProvider;

/**
 * Wrapper for URL-based JWK provider that does support keys in deeper locations.
 */
public class FullUrlJwkProvider extends UrlJwkProvider {
    /**
     * Initializes JWK provider for given issue URL.
     *
     * @param issuer Issuer URL.
     */
    public FullUrlJwkProvider(String issuer) {
        super(FullUrlJwkProvider.urlForIssuer(issuer));
    }

    /**
     * Builds URL to JWKS file.
     *
     * @param issuer Issuer URL.
     * @return JWKS file URL.
     */
    private static URL urlForIssuer(String issuer) {
        try {
            return new URL(issuer + "/.well-known/jwks.json");
        } catch (MalformedURLException error) {
            throw new IllegalArgumentException("Invalid JWKS URI.", error);
        }
    }
}
