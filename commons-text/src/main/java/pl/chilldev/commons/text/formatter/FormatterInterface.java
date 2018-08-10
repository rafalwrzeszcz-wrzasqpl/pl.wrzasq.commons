/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.text.formatter;

import pl.chilldev.commons.text.TextProcessingException;

/**
 * Source format handler interface.
 */
@FunctionalInterface
public interface FormatterInterface
{
    /**
     * Transforms source text into (X)HTML.
     *
     * @param text Source text.
     * @return (X)HTML snippet.
     * @throws TextProcessingException When text processing fails.
     */
    String transform(String text)
        throws
            TextProcessingException;
}
