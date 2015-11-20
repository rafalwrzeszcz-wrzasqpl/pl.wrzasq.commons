/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.json.writer;

import java.io.IOException;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import net.minidev.json.JSONStyle;
import net.minidev.json.reader.JsonWriterI;

/**
 * Generic JSON dumper for types with plain string representation.
 */
public class DateTimeWriter
    implements
        JsonWriterI<TemporalAccessor>
{
    /**
     * Assigned formatter.
     */
    private DateTimeFormatter formatter;

    /**
     * Initializes writer with given date-time formatter.
     *
     * @param formatter Formatter.
     */
    public DateTimeWriter(DateTimeFormatter formatter)
    {
        this.formatter = formatter;
    }

    /**
     * Dumps object as a string representation.
     *
     * @param value Value.
     * @param out Output stream.
     * @param compression JSON formatter style.
     * @throws IOException When write to output stream fails.
     */
    @Override
    public void writeJSONString(TemporalAccessor value, Appendable out, JSONStyle compression)
        throws
            IOException
    {
        compression.writeString(out, this.formatter.format(value));
    }
}
