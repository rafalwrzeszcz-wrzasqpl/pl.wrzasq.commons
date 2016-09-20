/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.text.formatter;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Plain text format handler.
 */
public class PlainTextFormatter implements FormatterInterface
{
    /**
     * {@inheritDoc}
     */
    @Override
    public String transform(String text)
    {
        return StringEscapeUtils.ESCAPE_XML10.translate(text).replaceAll("\n", "<br/>");
    }
}
