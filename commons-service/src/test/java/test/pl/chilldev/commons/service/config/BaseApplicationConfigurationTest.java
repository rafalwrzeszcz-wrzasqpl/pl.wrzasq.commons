/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.service.config;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minidev.json.JSONValue;

import org.json.JSONException;

import org.junit.Test;

import org.skyscreamer.jsonassert.JSONAssert;

import pl.chilldev.commons.service.config.BaseApplicationConfiguration;

public class BaseApplicationConfigurationTest
{
    @Test
    public void staticInitialization()
        throws
            ClassNotFoundException, JSONException
    {
        Class.forName(BaseApplicationConfiguration.class.getName());

        UUID uuid = UUID.fromString("afa1b21b-e918-42cd-8293-7033735ae3be");
        OffsetDateTime dateTime = OffsetDateTime.of(2011, 01, 30, 14, 58, 0, 0, ZoneOffset.ofHours(1));

        Map<String, Object> map = new HashMap<>();
        map.put("uuid", uuid);
        map.put("dateTime", dateTime);

        JSONAssert.assertEquals(
            "{\"uuid\":\"afa1b21b-e918-42cd-8293-7033735ae3be\",\"dateTime\":\"2011-01-30T14:58:00+01:00\"}",
            JSONValue.toJSONString(map),
            true
        );
    }

    @Test
    public void validator()
    {
        BaseApplicationConfiguration config = new BaseApplicationConfiguration();
        config.validator();
        // it's just to check if service is constructed
        // to check methods handling we should develop functional/integration tests
    }

    @Test
    public void introspector()
    {
        BaseApplicationConfiguration config = new BaseApplicationConfiguration();
        config.introspector();
        // it's just to check if service is constructed
        // to check methods handling we should develop functional/integration tests
    }

    @Test
    public void transactionAttributeSource()
    {
        BaseApplicationConfiguration config = new BaseApplicationConfiguration();
        config.transactionAttributeSource();
        // it's just to check if service is constructed
        // to check methods handling we should develop functional/integration tests
    }
}
