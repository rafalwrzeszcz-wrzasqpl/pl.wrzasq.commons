/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.db.timestampable;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.chilldev.commons.db.timestampable.Timestamp;
import pl.chilldev.commons.db.timestampable.TimestampType;
import pl.chilldev.commons.db.timestampable.TimestampableListener;

public class TimestampableListenerTest
{
    public class TestEntity
    {
        @Timestamp(TimestampType.CREATE)
        private OffsetDateTime createdAt;

        @Timestamp(TimestampType.UPDATE)
        private OffsetDateTime updatedAt;

        @Timestamp({ TimestampType.CREATE, TimestampType.UPDATE })
        private OffsetDateTime lastChangedAt;
    }

    @Test
    public void createTimestamps()
        throws
            IllegalAccessException
    {
        // just for code coverage
        TimestampType.valueOf("CREATE");

        // build entity
        TimestampableListenerTest.TestEntity entity = new TimestampableListenerTest.TestEntity();

        OffsetDateTime before = OffsetDateTime.now();

        TimestampableListener listener = new TimestampableListener();
        listener.createTimestamps(entity);

        OffsetDateTime after = OffsetDateTime.now();

        Assertions.assertFalse(
            entity.createdAt.isBefore(before),
            "TimestampableListener.createTimestamps() should set create timestamp for field annotated with CREATE timestamp."
        );
        Assertions.assertFalse(
            entity.createdAt.isAfter(after),
            "TimestampableListener.createTimestamps() should set create timestamp for field annotated with CREATE timestamp."
        );
        Assertions.assertNull(
            entity.updatedAt,
            "TimestampableListener.createTimestamps() should not set create timestamp for field not annotated with CREATE timestamp."
        );
        Assertions.assertFalse(
            entity.lastChangedAt.isBefore(before),
            "TimestampableListener.createTimestamps() should set create timestamp for field annotated with multiple timestamps, including CREATE."
        );
        Assertions.assertFalse(
            entity.lastChangedAt.isAfter(after),
            "TimestampableListener.createTimestamps() should set create timestamp for field annotated with multiple timestamps, including CREATE."
        );
    }

    @Test
    public void updateTimestamps()
        throws
            IllegalAccessException
    {
        // build entity
        TimestampableListenerTest.TestEntity entity = new TimestampableListenerTest.TestEntity();

        OffsetDateTime before = OffsetDateTime.now();

        TimestampableListener listener = new TimestampableListener();
        listener.updateTimestamps(entity);

        OffsetDateTime after = OffsetDateTime.now();

        Assertions.assertNull(
            entity.createdAt,
            "TimestampableListener.updateTimestamps() should not set update timestamp for field not annotated with UPDATE timestamp."
        );
        Assertions.assertFalse(
            entity.updatedAt.isBefore(before),
            "TimestampableListener.updateTimestamps() should set update timestamp for field annotated with UPDATE timestamp."
        );
        Assertions.assertFalse(
            entity.updatedAt.isAfter(after),
            "TimestampableListener.updateTimestamps() should set update timestamp for field annotated with UPDATE timestamp."
        );
        Assertions.assertFalse(
            entity.lastChangedAt.isBefore(before),
            "TimestampableListener.updateTimestamps() should set update timestamp for field annotated with multiple timestamps, including UPDATE."
        );
        Assertions.assertFalse(
            entity.lastChangedAt.isAfter(after),
            "TimestampableListener.updateTimestamps() should set update timestamp for field annotated with multiple timestamps, including UPDATE."
        );
    }
}
