/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.db.timestampable;

import java.lang.reflect.Field;

import java.time.OffsetDateTime;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Handler for timestampable annotations.
 */
public class TimestampableListener
{
    /**
     * Fills all marked fields with timestamps.
     *
     * @param timestampable Object to be filled.
     * @param timestampType Type of operation to handler.
     * @throws IllegalAccessException When accessing source field is impossible.
     */
    private void generateTimestamps(Object timestampable, TimestampType timestampType)
        throws
            IllegalAccessException
    {
        Class<?> type = timestampable.getClass();

        for (Field field : FieldUtils.getAllFields(type)) {
            if (field.isAnnotationPresent(Timestamp.class)) {
                // get timestamp configuration
                Timestamp options = field.getDeclaredAnnotation(Timestamp.class);

                // verify timestamp type
                if (ArrayUtils.contains(options.value(), timestampType)) {
                    // generate timestamp
                    FieldUtils.writeField(timestampable, field.getName(), OffsetDateTime.now(), true);
                }
            }
        }
    }

    /**
     * Saves timestamps marked for entity creation creation.
     *
     * @param timestampable Subject for timestamp handling.
     * @throws IllegalAccessException When accessing source field is impossible.
     */
    @PrePersist
    public void createTimestamps(Object timestampable)
        throws
            IllegalAccessException
    {
        this.generateTimestamps(timestampable, TimestampType.CREATE);
    }

    /**
     * Saves timestamps marked for entity update creation.
     *
     * @param timestampable Subject for timestamp handling.
     * @throws IllegalAccessException When accessing source field is impossible.
     */
    @PreUpdate
    public void updateTimestamps(Object timestampable)
        throws
            IllegalAccessException
    {
        this.generateTimestamps(timestampable, TimestampType.UPDATE);
    }
}
