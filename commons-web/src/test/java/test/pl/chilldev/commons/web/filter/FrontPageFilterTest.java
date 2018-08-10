/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.web.filter;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import pl.chilldev.commons.web.filter.FrontPageFilter;

public class FrontPageFilterTest
{
    private static final String ACCEPT_LANGUAGE_VALUE = "pl";

    private static final String VIEW_NAME = "front";

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private FilterChain chain;

    @Mock
    private ServletRequest servletRequest;

    @Mock
    private ServletResponse servletResponse;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private ViewResolver viewResolver;

    @Mock
    private View view;

    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

    @Test
    public void doFilter() throws Exception
    {
        String content = "{\"foo\":123}";

        FrontPageFilter filter = this.createFrontPageFilter();
        MockHttpServletRequest request = this.createMockRequest();
        request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // just for code coverage
        filter.init(null);
        filter.destroy();

        Mockito
            .when(this.viewResolver.resolveViewName(FrontPageFilterTest.VIEW_NAME, Locale.ROOT))
            .thenReturn(this.view);
        Mockito
            .doAnswer((InvocationOnMock invocation) -> {
                    ((HttpServletResponse) (invocation.getArguments()[1])).getWriter().print(content);
                    return null;
            })
            .when(this.chain)
            .doFilter(Mockito.isA(HttpServletRequest.class), Mockito.isA(HttpServletResponse.class));

        filter.doFilter(request, response, this.chain);

        Mockito
            .verify(this.view)
            .render(this.dataCaptor.capture(), Mockito.isA(HttpServletRequest.class), Mockito.same(response));

        // response view verification
        Assert.assertTrue(
            "FrontPageFilter.FrontHttpServletResponse.buildResponse() should expose response status.",
            this.dataCaptor.getValue().containsKey("responseStatus")
        );
        Assert.assertEquals(
            "FrontPageFilter.FrontHttpServletResponse.buildResponse() should expose response status.",
            HttpStatus.OK.value(),
            this.dataCaptor.getValue().get("responseStatus")
        );
        Assert.assertTrue(
            "FrontPageFilter.FrontHttpServletResponse.buildResponse() should expose response content.",
            this.dataCaptor.getValue().containsKey("responseContent")
        );
        Assert.assertEquals(
            "FrontPageFilter.FrontHttpServletResponse.buildResponse() should expose response content.",
            content,
            this.dataCaptor.getValue().get("responseContent")
        );
    }

    @Test
    public void doFilterEmptyResponse() throws Exception
    {
        FrontPageFilter filter = this.createFrontPageFilter();
        MockHttpServletRequest request = this.createMockRequest();
        request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.OK.value());

        Mockito
            .when(this.viewResolver.resolveViewName(FrontPageFilterTest.VIEW_NAME, Locale.ROOT))
            .thenReturn(this.view);

        filter.doFilter(request, this.httpServletResponse, this.chain);

        this.verifyBypassResponse();
    }

    @Test
    public void doFilterErrorResponse() throws Exception
    {
        FrontPageFilter filter = this.createFrontPageFilter();
        MockHttpServletRequest request = this.createMockRequest();
        request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Mockito
            .when(this.viewResolver.resolveViewName(FrontPageFilterTest.VIEW_NAME, Locale.ROOT))
            .thenReturn(this.view);
        Mockito
            .doAnswer((InvocationOnMock invocation) -> {
                ((HttpServletResponse) (invocation.getArguments()[1])).getWriter().print("{\"foo\":123}");
                return null;
            })
            .when(this.chain)
            .doFilter(Mockito.isA(HttpServletRequest.class), Mockito.isA(HttpServletResponse.class));

        filter.doFilter(request, response, this.chain);

        Mockito
            .verify(this.chain)
            .doFilter(Mockito.isA(HttpServletRequest.class), Mockito.isA(HttpServletResponse.class));
    }

    @Test
    public void doFilterContentDisposition() throws Exception
    {
        FrontPageFilter filter = this.createFrontPageFilter();
        MockHttpServletRequest request = this.createMockRequest();
        request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.OK.value());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "filename=\"foo.json\"");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Mockito
            .when(this.viewResolver.resolveViewName(FrontPageFilterTest.VIEW_NAME, Locale.ROOT))
            .thenReturn(this.view);
        Mockito
            .doAnswer((InvocationOnMock invocation) -> {
                ((HttpServletResponse) (invocation.getArguments()[1])).getWriter().print("{\"foo\":123}");
                return null;
            })
            .when(this.chain)
            .doFilter(Mockito.isA(HttpServletRequest.class), Mockito.isA(HttpServletResponse.class));

        filter.doFilter(request, response, this.chain);

        this.verifyBypassResponse();
    }

    @Test
    public void doFilterNoContentType() throws Exception
    {
        FrontPageFilter filter = this.createFrontPageFilter();
        MockHttpServletRequest request = this.createMockRequest();
        request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.OK.value());

        Mockito
            .when(this.viewResolver.resolveViewName(FrontPageFilterTest.VIEW_NAME, Locale.ROOT))
            .thenReturn(this.view);
        Mockito
            .doAnswer((InvocationOnMock invocation) -> {
                ((HttpServletResponse) (invocation.getArguments()[1])).getWriter().print("{\"foo\":123}");
                return null;
            })
            .when(this.chain)
            .doFilter(Mockito.isA(HttpServletRequest.class), Mockito.isA(HttpServletResponse.class));

        filter.doFilter(request, response, this.chain);

        this.verifyBypassResponse();
    }

    @Test
    public void doFilterNotJsonResponse() throws Exception
    {
        FrontPageFilter filter = this.createFrontPageFilter();
        MockHttpServletRequest request = this.createMockRequest();
        request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_XML_VALUE);

        Mockito
            .when(this.viewResolver.resolveViewName(FrontPageFilterTest.VIEW_NAME, Locale.ROOT))
            .thenReturn(this.view);
        Mockito
            .doAnswer((InvocationOnMock invocation) -> {
                ((HttpServletResponse) (invocation.getArguments()[1])).getWriter().print("{\"foo\":123}");
                return null;
            })
            .when(this.chain)
            .doFilter(Mockito.isA(HttpServletRequest.class), Mockito.isA(HttpServletResponse.class));

        filter.doFilter(request, response, this.chain);

        this.verifyBypassResponse();
    }

    @Test(expected = ServletException.class)
    public void doFilterErrorOnViewRender() throws Exception
    {
        FrontPageFilter filter = this.createFrontPageFilter();
        MockHttpServletRequest request = this.createMockRequest();
        request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Mockito
            .when(this.viewResolver.resolveViewName(FrontPageFilterTest.VIEW_NAME, Locale.ROOT))
            .thenReturn(this.view);
        Mockito
            .doAnswer((InvocationOnMock invocation) -> {
                ((HttpServletResponse) (invocation.getArguments()[1])).getWriter().print("{\"foo\":123}");
                return null;
            })
            .when(this.chain)
            .doFilter(Mockito.isA(HttpServletRequest.class), Mockito.isA(HttpServletResponse.class));
        Mockito
            .doThrow(Exception.class)
            .when(this.view)
            .render(
                Mockito.anyMap(),
                Mockito.isA(HttpServletRequest.class),
                Mockito.isA(HttpServletResponse.class)
            );

        filter.doFilter(request, response, this.chain);
    }

    @Test(expected = ServletException.class)
    public void doFilterThrowsExceptionOnViewResolving() throws Exception
    {
        MockHttpServletRequest request = this.createMockRequest();
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XHTML_XML_VALUE);

        Mockito
            .when(this.viewResolver.resolveViewName(FrontPageFilterTest.VIEW_NAME, Locale.ROOT))
            .thenThrow(Exception.class);

        FrontPageFilter filter = this.createFrontPageFilter();
        filter.doFilter(request, this.httpServletResponse, this.chain);
    }

    @Test
    public void doFilterNotGetMethod() throws IOException, ServletException
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());

        Filter filter = new FrontPageFilter();
        filter.doFilter(request, this.httpServletResponse, this.chain);
        Mockito.verify(this.chain).doFilter(request, this.httpServletResponse);
    }

    @Test
    public void doFilterNotMarkupAccept() throws IOException, ServletException
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, MediaType.IMAGE_GIF_VALUE);
        request.setMethod(HttpMethod.GET.name());

        Filter filter = new FrontPageFilter();
        filter.doFilter(request, this.httpServletResponse, this.chain);
        Mockito.verify(this.chain).doFilter(request, this.httpServletResponse);
    }

    @Test
    public void doFilterNotHttpServletRequest() throws IOException, ServletException
    {
        Filter filter = new FrontPageFilter();
        filter.doFilter(this.servletRequest, this.httpServletResponse, this.chain);
        Mockito.verifyZeroInteractions(this.chain);
    }

    @Test
    public void doFilterNotHttpServletResponse() throws IOException, ServletException
    {
        Filter filter = new FrontPageFilter();
        filter.doFilter(this.httpServletRequest, this.servletResponse, this.chain);
        Mockito.verifyZeroInteractions(this.chain);
    }

    private MockHttpServletRequest createMockRequest()
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.GET.name());
        request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, FrontPageFilterTest.ACCEPT_LANGUAGE_VALUE);
        request.addPreferredLocale(Locale.ROOT);
        return request;
    }

    private FrontPageFilter createFrontPageFilter()
    {
        FrontPageFilter filter = new FrontPageFilter();
        filter.setViewResolver(this.viewResolver);
        filter.setViewName(FrontPageFilterTest.VIEW_NAME);
        return filter;
    }

    private void verifyBypassResponse() throws IOException, ServletException
    {
        Mockito
            .verify(this.chain)
            .doFilter(Mockito.isA(HttpServletRequest.class), Mockito.isA(HttpServletResponse.class));
        Mockito
            .verifyZeroInteractions(this.view);
    }
}
