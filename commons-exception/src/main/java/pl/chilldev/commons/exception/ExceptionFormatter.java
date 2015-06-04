/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2014 - 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.exception;

import java.text.MessageFormat;

/**
 * Exception dumper signature.
 */
@FunctionalInterface
public interface ExceptionFormatter
{
    /**
     * Simple formatter that prints one-line exception info (it's type and message).
     */
    ExceptionFormatter SIMPLE_FORMAT = new ExceptionFormatter() {
        /**
         * Messages formatter.
         */
        private MessageFormat formatter = new MessageFormat("{0}: {1}");

        /**
         * {@inheritDoc}
         */
        @Override
        public String format(Throwable exception)
        {
            return this.formatter.format(new Object[] {
                    exception.getClass().getName(),
                    exception.getMessage(),
            });
        }
    };

    /**
     * Formats exception to printable representation.
     *
     * @param exception Exception to be displayed.
     * @return String representation.
     */
    String format(Throwable exception);
}
