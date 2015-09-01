/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.json.writer;

import java.io.IOException;

import net.minidev.json.JSONValue;
import net.minidev.json.reader.JsonWriterI;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.data.domain.Sort;

import pl.chilldev.commons.jsonrpc.json.writer.SortWriter;

public class SortWriterTest
{
    @Test
    public void writeJSONString()
        throws
            IOException
    {
        Sort value = new Sort(
            new Sort.Order(Sort.Direction.ASC, "id"),
            new Sort.Order(Sort.Direction.DESC, "name")
        );

        JsonWriterI<Sort> writer = new SortWriter();
        StringBuilder out = new StringBuilder();
        writer.writeJSONString(value, out, JSONValue.COMPRESSION);

        Assert.assertEquals(
            "SortWriter.writeJSONString() should dump Sort object into list of order properties.",
            "[[\"id\",\"ASC\"],[\"name\",\"DESC\"]]",
            out.toString()
        );
    }

    @Test
    public void writeJSONStringNull()
        throws
            IOException
    {
        JsonWriterI<Sort> writer = new SortWriter();
        StringBuilder out = new StringBuilder();
        writer.writeJSONString(null, out, JSONValue.COMPRESSION);

        Assert.assertEquals(
            "SortWriter.writeJSONString() should produce empty JSON array for NULL value.",
            "[]",
            out.toString()
        );
    }
}
