/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.web.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Front page rendering.
 */
@Component("chillDevFrontPageFilter")
public class FrontPageFilter implements Filter
{
    /**
     * Response wrapper that buffers the response.
     */
    private static class FrontHttpServletResponse extends ContentCachingResponseWrapper
    {
        /**
         * Initializes wrapper with embedded response.
         *
         * @param response Nested response object.
         */
        FrontHttpServletResponse(HttpServletResponse response)
        {
            super(response);
        }

        /**
         * Produces real response.
         *
         * @param view Destination template.
         * @param request Request for this scope.
         * @throws ServletException When rendering view fails.
         * @throws IOException When I/O operation fails on rendering response.
         */
        private void buildResponse(View view, HttpServletRequest request) throws IOException, ServletException
        {
            HttpServletResponse response = (HttpServletResponse) this.getResponse();

            // there needs to be something to wrap
            if (this.getContentSize() == 0
                // if it's an error response, always generate view representation
                || response.getStatus() < HttpStatus.BAD_REQUEST.value()
                    && (
                        // if it's downloadable file, just pass it through
                        response.containsHeader(HttpHeaders.CONTENT_DISPOSITION)
                        || !(
                            // if it's not JSON response, it's not API call
                            response.containsHeader(HttpHeaders.CONTENT_TYPE)
                            && MediaType.APPLICATION_JSON.isCompatibleWith(
                                MimeType.valueOf(response.getContentType())
                            )
                        )
                    )
            )
            {
                this.copyBodyToResponse();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("responseStatus", response.getStatus());
            data.put("responseContent", new String(this.getContentAsByteArray(), StandardCharsets.UTF_8));

            try {
                view.render(data, request, response);
                //CHECKSTYLE:OFF: IllegalCatchCheck
            } catch (Exception error) {
                //CHECKSTYLE:ON: IllegalCatchCheck
                throw new ServletException(error);
            }
        }
    }

    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(FrontPageFilter.class);

    /**
     * View resolver.
     */
    @Autowired
    @Setter
    private ViewResolver viewResolver;

    /**
     * Template view name.
     */
    @Value("${chillDev.frontPageFilter.viewName}")
    @Setter
    private String viewName;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws ServletException, IOException
    {
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            // parse acceptable media types
            Enumeration<String> accept = request.getHeaders(HttpHeaders.ACCEPT);
            List<String> accepts = Collections.list(accept);
            List<MediaType> mediaTypes = MediaType.parseMediaTypes(accepts);

            // handle web page requests
            if (request.getMethod().equals(HttpMethod.GET.name())) {
                for (MediaType mediaType : mediaTypes) {
                    if (
                        MediaType.APPLICATION_XHTML_XML.isCompatibleWith(mediaType)
                            || MediaType.TEXT_HTML.isCompatibleWith(mediaType)
                        )
                    {
                        this.logger.trace("Rendering page response for {}.", request.getRequestURI());

                        response = new FrontPageFilter.FrontHttpServletResponse(response);

                        break;
                    }
                }
            }

            // forward request, no matter if it's plain or wrapped
            chain.doFilter(request, response);

            if (response instanceof FrontPageFilter.FrontHttpServletResponse) {
                try {
                    ((FrontPageFilter.FrontHttpServletResponse) response).buildResponse(
                        this.viewResolver.resolveViewName(this.viewName, RequestContextUtils.getLocale(request)),
                        request
                    );
                    //CHECKSTYLE:OFF: IllegalCatchCheck
                } catch (Exception error) {
                    //CHECKSTYLE:ON: IllegalCatchCheck
                    this.logger.error("Error rendering {}: {}.", this.viewName, error.getMessage(), error);
                    throw new ServletException(error);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig)
    {
        // dummy method
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy()
    {
        // dummy method
    }
}
