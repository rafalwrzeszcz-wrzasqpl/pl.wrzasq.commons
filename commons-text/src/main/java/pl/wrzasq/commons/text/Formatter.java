/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text;

import java.util.HashMap;
import java.util.Map;

import pl.wrzasq.commons.text.formatter.FormatterInterface;

/**
 * Text formats handler.
 */
public class Formatter
{
    /**
     * All known format handlers.
     */
    private Map<String, FormatterInterface> formatters = new HashMap<>();

    /**
     * Registers format handler.
     *
     * @param format Format name.
     * @param handler Format handler.
     */
    public void registerFormatter(String format, FormatterInterface handler)
    {
        this.formatters.put(format, handler);
    }

    /**
     * Transforms text from given form.
     *
     * @param format Format name.
     * @param text Source text.
     * @return Generated markup snippet.
     * @throws TextProcessingException When specified format can't be handled or handler throws exception.
     */
    public String transform(String format, String text)
        throws
            TextProcessingException
    {
        if (!this.formatters.containsKey(format)) {
            throw new TextProcessingException(String.format("No handler for format \"%s\" registred.", format));
        }

        return this.formatters.get(format).transform(text);
    }
}
