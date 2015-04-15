/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc.json.writer;

import java.io.IOException;

import net.minidev.json.JSONStyle;
import net.minidev.json.reader.JsonWriterI;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

import pl.chilldev.commons.jsonrpc.json.writer.StringDumpingWriter;

@RunWith(MockitoJUnitRunner.class)
public class StringDumpingWriterTest
{
    @Mock
    private Object value;

    @Mock
    private JSONStyle compression;

    @Mock
    private Appendable out;

    @Test
    public void writeJSONString()
        throws
            IOException
    {
        String dump = "test";

        doReturn(dump).when(this.value).toString();

        JsonWriterI writer = new StringDumpingWriter();
        writer.writeJSONString(this.value, this.out, this.compression);

        verify(this.compression).writeString(this.out, dump);
    }
}
