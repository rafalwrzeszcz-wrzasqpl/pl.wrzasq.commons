/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.text.formatter;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 * Markdown format handler.
 */
public class MarkdownFormatter implements FormatterInterface
{
    /**
     * Markdown processor with all available extras.
     */
    private PegDownProcessor markdownProcessor = new PegDownProcessor(Extensions.ALL);

    /**
     * {@inheritDoc}
     */
    @Override
    public String transform(String text)
    {
        return this.markdownProcessor.markdownToHtml(text);
    }
}
