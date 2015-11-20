/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

import org.junit.Test;
import org.junit.Assert;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import pl.chilldev.commons.jsonrpc.json.ParamsRetriever;

public class ParamsRetrieverTest
{
    @Test
    public void getUuid()
        throws
            JSONRPC2Error
    {
        String id = "de305d54-75b4-431b-adb2-eb6b9e546014";
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);

        ParamsRetriever params = new ParamsRetriever(data);

        Assert.assertEquals(
            "ParamsRetriever.getUuid() should return UUID passed as a string parameter.",
            id,
            params.getUuid("id").toString()
        );
    }

    @Test(expected = JSONRPC2Error.class)
    public void getUuidInvalidUuid()
        throws
            JSONRPC2Error
    {
        String id = "foo";
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);

        ParamsRetriever params = new ParamsRetriever(data);

        params.getUuid("id", true);
    }

    @Test(expected = JSONRPC2Error.class)
    public void getUuidAllowNullNoParam()
        throws
            JSONRPC2Error
    {
        JSONRPC2Request request = new JSONRPC2Request("test", this);

        ParamsRetriever params = new ParamsRetriever(request);
        params.getUuid("id", true);
    }

    @Test
    public void getUuidAllowNull()
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = new HashMap<>();
        data.put("id", null);

        JSONRPC2Request request = new JSONRPC2Request("test", data, this);

        ParamsRetriever params = new ParamsRetriever(request);

        Assert.assertNull(
            "ParamsRetriever.getUuid() should return NULL if there is no argument and NULL is allowed.",
            params.getUuid("id", true)
        );
    }

    @Test
    public void getOptUuid()
        throws
            JSONRPC2Error
    {
        String id = "de305d54-75b4-431b-adb2-eb6b9e546014";
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);

        ParamsRetriever params = new ParamsRetriever(data);

        Assert.assertEquals(
            "ParamsRetriever.getOptUuid() should return UUID passed as a string parameter.",
            id,
            params.getOptUuid("id").toString()
        );
    }

    @Test(expected = JSONRPC2Error.class)
    public void getOptUuidNull()
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = new HashMap<>();
        data.put("id", null);

        ParamsRetriever params = new ParamsRetriever(data);

        params.getOptUuid("id");
    }

    @Test
    public void getOptUuidNoParam()
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = new HashMap<>();

        ParamsRetriever params = new ParamsRetriever(data);

        Assert.assertNull(
            "ParamsRetriever.getOptUuid() should return NULL when no parameter with given name exists.",
            params.getOptUuid("id")
        );
    }

    @Test
    public void getSort()
        throws
            JSONRPC2Error
    {
        List<Object> sort = new ArrayList<>();

        List<Object> order = new ArrayList<>();
        order.add("id");
        order.add("ASC");
        sort.add(order);

        order = new ArrayList<>();
        order.add("name");
        order.add("DESC");
        sort.add(order);

        Map<String, Object> data = new HashMap<>();
        data.put("sort", sort);

        ParamsRetriever params = new ParamsRetriever(data);

        Sort value = params.getSort("sort");

        Assert.assertEquals(
            "ParamsRetriever.getSort() should build sorting orders list.",
            Sort.Direction.ASC,
            value.getOrderFor("id").getDirection()
        );
        Assert.assertEquals(
            "ParamsRetriever.getSort() should build sorting orders list.",
            Sort.Direction.DESC,
            value.getOrderFor("name").getDirection()
        );
    }

    @Test(expected = JSONRPC2Error.class)
    public void getSortInvalid()
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = new HashMap<>();
        data.put("sort", "test");

        ParamsRetriever params = new ParamsRetriever(data);

        params.getSort("sort");
    }

    @Test(expected = JSONRPC2Error.class)
    public void getSortInvalidElement()
        throws
            JSONRPC2Error
    {
        List<Object> sort = new ArrayList<>();
        sort.add("test");

        Map<String, Object> data = new HashMap<>();
        data.put("sort", sort);

        ParamsRetriever params = new ParamsRetriever(data);

        params.getSort("sort");
    }

    @Test
    public void getSortEmpty()
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = new HashMap<>();
        data.put("sort", new ArrayList<Object>());

        ParamsRetriever params = new ParamsRetriever(data);

        Assert.assertNull(
            "ParamsRetriever.getSort() should return NULL if empty list is specified to prevent Spring Data error of invalid value.",
            params.getSort("sort")
        );
    }

    @Test
    public void getSortNull()
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = new HashMap<>();

        ParamsRetriever params = new ParamsRetriever(data);

        Assert.assertNull(
            "ParamsRetriever.getSort() should return NULL if no sort is specified.",
            params.getSort("sort")
        );
    }

    @Test
    public void getPageable()
        throws
            JSONRPC2Error
    {
        List<Object> sort = new ArrayList<>();

        List<Object> order = new ArrayList<>();
        order.add("id");
        order.add("ASC");
        sort.add(order);

        Map<String, Object> data = new HashMap<>();
        data.put("pageNumber", 2);
        data.put("pageSize", 100);
        data.put("sortBy", sort);

        ParamsRetriever params = new ParamsRetriever(data);

        Pageable request = params.getPageable("pageNumber", "pageSize", "sortBy", 20);

        Assert.assertEquals(
            "ParamsRetriever.getPageable() should return page request with page number set.",
            2,
            request.getPageNumber()
        );
        Assert.assertEquals(
            "ParamsRetriever.getPageable() should return page request with page size set.",
            100,
            request.getPageSize()
        );
        Assert.assertEquals(
            "ParamsRetriever.getPageable() should return page request with sorting criteria set.",
            Sort.Direction.ASC,
            request.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    public void getPageableDefaultLimit()
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = new HashMap<>();
        data.put("pageNumber", 2);

        ParamsRetriever params = new ParamsRetriever(data);

        Pageable request = params.getPageable("pageNumber", "pageSize", "sortBy", 20);

        Assert.assertEquals(
            "ParamsRetriever.getPageable() should return page request with page size set to default value if not present.",
            20,
            request.getPageSize()
        );
    }

    @Test
    public void getPageableDefultParams()
        throws
            JSONRPC2Error
    {
        List<Object> sort = new ArrayList<>();

        List<Object> order = new ArrayList<>();
        order.add("id");
        order.add("ASC");
        sort.add(order);

        Map<String, Object> data = new HashMap<>();
        data.put("page", 2);
        data.put("limit", 100);
        data.put("sort", sort);

        ParamsRetriever params = new ParamsRetriever(data);

        Pageable request = params.getPageable(20);

        Assert.assertEquals(
            "ParamsRetriever.getPageable() should return page request with page number set.",
            2,
            request.getPageNumber()
        );
        Assert.assertEquals(
            "ParamsRetriever.getPageable() should return page request with page size set.",
            100,
            request.getPageSize()
        );
        Assert.assertEquals(
            "ParamsRetriever.getPageable() should return page request with sorting criteria set.",
            Sort.Direction.ASC,
            request.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    public void getBean()
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = new HashMap<>();
        data.put("foo", "bar");

        ParamsRetriever params = new ParamsRetriever(data);

        Bean bean = params.getBean(Bean.class);

        Assert.assertEquals(
            "ParamsRetriever.getBean() should build bean from properties in current parameter scope.",
            "bar",
            bean.getFoo()
        );
    }

    @Test
    public void getBeanNull()
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = new HashMap<>();
        data.put("bean", null);

        ParamsRetriever params = new ParamsRetriever(data);

        Assert.assertNull(
            "ParamsRetriever.getBean() should return NULL if allowed and given parameter is empty.",
            params.getBean("bean", Bean.class, true)
        );
    }

    @Test
    public void getBeanSubScope()
        throws
            JSONRPC2Error
    {
        Map<String, Object> properties = new HashMap<>();
        properties.put("foo", "bar");

        Map<String, Object> data = new HashMap<>();
        data.put("bean", properties);

        ParamsRetriever params = new ParamsRetriever(data);

        Bean bean = params.getBean("bean", Bean.class);

        Assert.assertEquals(
            "ParamsRetriever.getBean() should build bean from properties in given parameter scope.",
            "bar",
            bean.getFoo()
        );
    }
}

class Bean
{
    private String foo;

    public String getFoo()
    {
        return this.foo;
    }

    public void setFoo(String value)
    {
        this.foo = value;
    }
}
