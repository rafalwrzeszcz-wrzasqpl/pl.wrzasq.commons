/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.text.formatter;

/**
 * (X)HTML format handler.
 */
public class HtmlFormatter implements FormatterInterface
{
    /**
     * {@inheritDoc}
     */
    @Override
    public String transform(String text)
    {
        // this is already our desired format
        return text;
    }
}
