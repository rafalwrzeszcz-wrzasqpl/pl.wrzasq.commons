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

import org.junit.Assert;
import org.junit.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import pl.chilldev.commons.jsonrpc.json.ConvertUtils;

public class ConvertUtilsTest
{
    @Test
    public void buildPage()
    {
        List<Object> list = new ArrayList<>();

        Map<String, Object> record = new HashMap<>();
        record.put("foo", "bar");
        list.add(record);

        record = new HashMap<>();
        record.put("foo", "baz");
        list.add(record);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("all", 5);

        Page<TestBean> page = ConvertUtils.buildPage(
            data,
            new PageRequest(1, 2),
            TestBean.class,
            "list",
            "all"
        );

        Assert.assertEquals(
            "ConvertUtils.buildPage() should load current page from raw data.",
            2,
            page.getSize()
        );
        Assert.assertEquals(
            "ConvertUtils.buildPage() should load current pare entries properties.",
            "bar",
            page.getContent().get(0).getFoo()
        );
        Assert.assertEquals(
            "ConvertUtils.buildPage() should load current pare entries properties.",
            "baz",
            page.getContent().get(1).getFoo()
        );
        Assert.assertEquals(
            "ConvertUtils.buildPage() should load size of entire result page.",
            5,
            page.getTotalElements()
        );
    }

    @Test(expected = ClassCastException.class)
    public void buildPageNotMap()
    {
        ConvertUtils.buildPage(
            new ArrayList<Object>(),
            new PageRequest(1, 1),
            TestBean.class
        );
    }

    @Test(expected = ClassCastException.class)
    public void buildPageRecordsNotList()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("records", new HashMap<String, Object>());

        ConvertUtils.buildPage(
            data,
            new PageRequest(1, 1),
            TestBean.class
        );
    }

    @Test
    public void buildPageDefaultParams()
    {
        List<Object> list = new ArrayList<>();

        Map<String, Object> record = new HashMap<>();
        record.put("foo", "bar");
        list.add(record);

        record = new HashMap<>();
        record.put("foo", "baz");
        list.add(record);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("count", 5);

        Page<TestBean> page = ConvertUtils.buildPage(
            data,
            new PageRequest(1, 2),
            TestBean.class
        );

        Assert.assertEquals(
            "ConvertUtils.buildPage() should pick default parameter names if not specified.",
            2,
            page.getSize()
        );
        Assert.assertEquals(
            "ConvertUtils.buildPage() should pick default parameter names if not specified.",
            5,
            page.getTotalElements()
        );
    }
}

class TestBean
{
    protected String foo;

    public String getFoo()
    {
        return this.foo;
    }

    public void setFoo(String value)
    {
        this.foo = value;
    }
}
