/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.text.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various HTML text processing utilities.
 */
public class Utils
{
    /**
     * First paragraph pattern.
     */
    public static final Pattern REGEX_FIRSTPARAGRAPH = Pattern.compile(
        "<p(?: [^>]*)?>(.*?)</p>",
        Pattern.DOTALL
    );

    /**
     * Fetches first paragraph of text.
     *
     * @param text HTML snippet.
     * @return First paragrapth.
     */
    public static String firstParagraph(String text)
    {
        Matcher match = Utils.REGEX_FIRSTPARAGRAPH.matcher(text);

        return match.find() ? match.group(1) : "";
    }
}
