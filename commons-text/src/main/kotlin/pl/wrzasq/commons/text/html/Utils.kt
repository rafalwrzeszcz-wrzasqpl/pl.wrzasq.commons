/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016, 2018 - 2019, 2021 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.text.html

import pl.wrzasq.commons.text.TextProcessingException
import java.net.URLEncoder
import pl.wrzasq.commons.text.Formatter
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

private const val SUFFIX_DEFAULT = "…"
private val WORDBOUND_PATTERN = Pattern.compile("\\S\\s+\\S*?$", Pattern.UNICODE_CASE)
private val REGEX_FIRSTPARAGRAPH = Pattern.compile(
    "<p(?: [^>]*)?>(.*?)</p>",
    Pattern.DOTALL
)

/**
 * Various HTML text processing utilities.
 */
object Utils {
    private var formatter = Formatter()

    /**
     * Fetches first paragraph of text.
     *
     * @param text HTML snippet.
     * @return First paragraph.
     */
    fun firstParagraph(text: String): String {
        val match = REGEX_FIRSTPARAGRAPH.matcher(text)
        return if (match.find()) match.group(1) else ""
    }

    /**
     * Truncates text.
     *
     * @param text Text to be truncated.
     * @param length Maximum text length.
     * @param suffix Suffix to be used at the end of truncated text.
     * @param bound Whether to look for word end or not.
     * @return Translated message.
     */
    fun truncate(text: String, length: Int, suffix: String, bound: Boolean): String {
        // nothing to do here
        if (text.length <= length) {
            return text
        }

        // look for last possible word
        var position = length
        if (bound) {
            // look for last word-break
            val part = text.substring(0, length + 2)
            val matcher = WORDBOUND_PATTERN.matcher(part)
            if (matcher.find()) {
                // we add 1 as second parameter is exclusive
                position = matcher.start() + 1
            }
        }
        return text.substring(0, position).trim() + suffix
    }

    /**
     * Truncates text.
     *
     * @param text Text to be truncated.
     * @param length Maximum text length.
     * @param suffix Suffix to be used at the end of truncated text.
     * @return Translated message.
     */
    fun truncate(text: String, length: Int, suffix: String): String = truncate(text, length, suffix, true)

    /**
     * Truncates text.
     *
     * @param text Text to be truncated.
     * @param length Maximum text length.
     * @param bound Whether to look for word end or not.
     * @return Truncated text.
     */
    fun truncate(text: String, length: Int, bound: Boolean): String = truncate(text, length, SUFFIX_DEFAULT, bound)

    /**
     * Truncates text.
     *
     * @param text Text to be truncated.
     * @param length Maximum text length.
     * @return Truncated text.
     */
    fun truncate(text: String, length: Int): String = truncate(text, length, SUFFIX_DEFAULT, true)

    /**
     * Registers new text formatting handler.
     *
     * @param formatter Formatter.
     */
    @JvmStatic
    fun setFormatter(formatter: Formatter) {
        Utils.formatter = formatter
    }

    /**
     * Formats the text.
     *
     * @param format Format name.
     * @param text Source text.
     * @return Formatted text.
     * @throws TextProcessingException When text processing fails.
     */
    fun format(format: String, text: String): String = formatter.transform(format, text)

    /**
     * Wrapper function that encodes URLs using UTF-8 encoding.
     *
     * @param value URL part.
     * @return URL-encoded part.
     */
    fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
