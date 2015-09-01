/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.json.writer;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import net.minidev.json.reader.JsonWriterI;

import org.springframework.data.domain.Sort;

/**
 * Spring-Data Sort object serializer.
 */
public class SortWriter
    implements
        JsonWriterI<Sort>
{
    /**
     * Dumps object into JSON-serializable array and writes it as a JSON string.
     *
     * @param value Value.
     * @param out Output stream.
     * @param compression JSON formatter style.
     * @throws IOException When write to output stream fails.
     */
    @Override
    public void writeJSONString(Sort value, Appendable out, JSONStyle compression)
        throws
            IOException
    {
        List<List<String>> sort = new ArrayList<>();

        // serialize each sorting property into two-element array of form [property, direction]
        if (value != null) {
            for (Sort.Order single : value) {
                List<String> order = new ArrayList<>();
                order.add(single.getProperty());
                order.add(single.getDirection().toString());
                sort.add(order);
            }
        }

        JSONValue.writeJSONString(sort, out, compression);
    }
}
