/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.text.slugifier;

import java.text.Normalizer;

import java.util.Locale;

/**
 * Simple implementation of slugifier.
 */
public class SimpleSlugifier implements Slugifier
{
    /**
     * Default separator.
     */
    public static final String DEFAULT_DELIMITER = "-";

    /**
     * Separator for URL parts.
     */
    private String delimiter = SimpleSlugifier.DEFAULT_DELIMITER;

    /**
     * Changes words delimiter.
     *
     * @param delimiter New delimiter.
     */
    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String slugify(String text)
    {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("[^\\p{ASCII}]", "")
            .replaceAll("\\W+", this.delimiter)
            .replaceAll(this.delimiter + "+", this.delimiter)
            .replaceAll("^" + this.delimiter, "")
            .replaceAll(this.delimiter + "$", "")
            .toLowerCase(Locale.ROOT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String slugify(String... texts)
    {
        return this.slugify(String.join(this.delimiter, texts));
    }
}
