/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.db.timestampable;

import java.time.OffsetDateTime;

import org.junit.Assert;
import org.junit.Test;

import pl.chilldev.commons.db.timestampable.Timestamp;
import pl.chilldev.commons.db.timestampable.TimestampType;
import pl.chilldev.commons.db.timestampable.TimestampableListener;

public class TimestampableListenerTest
{
    public class TestEntity
    {
        @Timestamp(TimestampType.CREATE)
        protected OffsetDateTime createdAt;

        @Timestamp(TimestampType.UPDATE)
        protected OffsetDateTime updatedAt;

        @Timestamp({ TimestampType.CREATE, TimestampType.UPDATE })
        protected OffsetDateTime lastChangedAt;
    }

    @Test
    public void createTimestamps()
        throws
            IllegalAccessException
    {
        // build entity
        TimestampableListenerTest.TestEntity entity = new TimestampableListenerTest.TestEntity();

        OffsetDateTime before = OffsetDateTime.now();

        TimestampableListener listener = new TimestampableListener();
        listener.createTimestamps(entity);

        OffsetDateTime after = OffsetDateTime.now();

        Assert.assertFalse(
            "TimestampableListener.createTimestamps() should set create timestamp for field annotated with CREATE timestamp.",
            entity.createdAt.isBefore(before)
        );
        Assert.assertFalse(
            "TimestampableListener.createTimestamps() should set create timestamp for field annotated with CREATE timestamp.",
            entity.createdAt.isAfter(after)
        );
        Assert.assertNull(
            "TimestampableListener.createTimestamps() should not set create timestamp for field not annotated with CREATE timestamp.",
            entity.updatedAt
        );
        Assert.assertFalse(
            "TimestampableListener.createTimestamps() should set create timestamp for field annotated with multiple timestamps, including CREATE.",
            entity.lastChangedAt.isBefore(before)
        );
        Assert.assertFalse(
            "TimestampableListener.createTimestamps() should set create timestamp for field annotated with multiple timestamps, including CREATE.",
            entity.lastChangedAt.isAfter(after)
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

        Assert.assertNull(
            "TimestampableListener.updateTimestamps() should not set update timestamp for field not annotated with UPDATE timestamp.",
            entity.createdAt
        );
        Assert.assertFalse(
            "TimestampableListener.updateTimestamps() should set update timestamp for field annotated with UPDATE timestamp.",
            entity.updatedAt.isBefore(before)
        );
        Assert.assertFalse(
            "TimestampableListener.updateTimestamps() should set update timestamp for field annotated with UPDATE timestamp.",
            entity.updatedAt.isAfter(after)
        );
        Assert.assertFalse(
            "TimestampableListener.updateTimestamps() should set update timestamp for field annotated with multiple timestamps, including UPDATE.",
            entity.lastChangedAt.isBefore(before)
        );
        Assert.assertFalse(
            "TimestampableListener.updateTimestamps() should set update timestamp for field annotated with multiple timestamps, including UPDATE.",
            entity.lastChangedAt.isAfter(after)
        );
    }
}
