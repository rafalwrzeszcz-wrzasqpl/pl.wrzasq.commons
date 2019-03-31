/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.db.timestampable;

/**
 * Timestamp operation type.
 */
public enum TimestampType {
    /**
     * Creation timestamp.
     */
    CREATE,
    /**
     * Last update timestamp.
     */
    UPDATE;
}
