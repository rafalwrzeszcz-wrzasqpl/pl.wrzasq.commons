/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text.slugifier;

import java.text.Normalizer;

import java.util.Locale;

import lombok.Setter;

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
    @Setter
    private String delimiter = SimpleSlugifier.DEFAULT_DELIMITER;

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
