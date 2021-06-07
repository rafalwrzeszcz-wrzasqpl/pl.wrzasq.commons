/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016, 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text.slugifier

import java.text.Normalizer

/**
 * Default separator.
 */
const val DEFAULT_DELIMITER = "-"

/**
 * Simple implementation of slugifier.
 *
 * @param delimiter Separator for URL parts.
 */
class SimpleSlugifier(
    private val delimiter: String = DEFAULT_DELIMITER
) : Slugifier {
    override fun slugify(vararg texts: String): String = Normalizer.normalize(
        texts.joinToString(delimiter),
        Normalizer.Form.NFD
    )
        .replace("[^\\p{ASCII}]".toRegex(), "")
        .replace("\\W+".toRegex(), delimiter)
        .replace("$delimiter+".toRegex(), delimiter)
        .replace("^$delimiter".toRegex(), "")
        .replace("$delimiter$".toRegex(), "")
        .lowercase()
}
