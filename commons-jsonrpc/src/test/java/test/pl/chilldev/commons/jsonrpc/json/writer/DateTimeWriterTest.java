/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.json.writer;

import java.io.IOException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import net.minidev.json.JSONStyle;
import net.minidev.json.reader.JsonWriterI;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.chilldev.commons.jsonrpc.json.writer.DateTimeWriter;

@RunWith(MockitoJUnitRunner.class)
public class DateTimeWriterTest
{
    @Mock
    private JSONStyle compression;

    @Mock
    private Appendable out;

    @Test
    public void writeJSONString()
        throws
            IOException
    {
        OffsetDateTime dateTime = OffsetDateTime.of(2015, 7, 2, 3, 20, 0, 0, ZoneOffset.ofHours(1));

        JsonWriterI<TemporalAccessor> writer = new DateTimeWriter(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        writer.writeJSONString(dateTime, this.out, this.compression);

        Mockito.verify(this.compression).writeString(this.out, "2015-07-02T03:20:00+01:00");
    }
}
