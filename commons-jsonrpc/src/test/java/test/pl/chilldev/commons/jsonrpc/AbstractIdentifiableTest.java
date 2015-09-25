/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc;

import java.util.UUID;

import org.junit.Test;
import org.junit.Assert;

import pl.chilldev.commons.jsonrpc.AbstractIdentifiable;

public class AbstractIdentifiableTest
{
    public class Entity extends AbstractIdentifiable<UUID>
    {
        public UUID id;

        @Override
        public UUID getId()
        {
            return this.id;
        }
    }

    @Test
    public void equalsSame()
    {
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiableTest.Entity();
        Assert.assertTrue(
            "Identifiable.equals() should return TRUE if the object is the very same instance.",
            entity.equals(entity)
        );
    }

    @Test
    public void equalsNull()
    {
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiableTest.Entity();
        Assert.assertFalse(
            "Identifiable.equals() should return FALSE when compared to NULL.",
            entity.equals(null)
        );
    }

    @Test
    public void equalsNotEntity()
    {
        UUID id = UUID.randomUUID();
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiableTest.Entity();
        Assert.assertFalse(
            "Identifiable.equals() should return FALSE when compared to a non-entity object.",
            entity.equals(id)
        );
    }

    @Test
    public void equalsNullId()
    {
        AbstractIdentifiable<UUID> object = new AbstractIdentifiableTest.Entity();
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiableTest.Entity();
        Assert.assertFalse(
            "Identifiable.equals() should return FALSE when it is not persisted.",
            entity.equals(object)
        );
    }

    @Test
    public void equals()
    {
        UUID id = UUID.randomUUID();

        AbstractIdentifiableTest.Entity same = new AbstractIdentifiableTest.Entity();
        same.id = id;

        AbstractIdentifiableTest.Entity other = new AbstractIdentifiableTest.Entity();
        other.id = UUID.randomUUID();

        AbstractIdentifiableTest.Entity entity = new AbstractIdentifiableTest.Entity();
        entity.id = id;

        Assert.assertTrue(
            "Identifiable.equals() should return TRUE when compared entity has same ID.",
            entity.equals(same)
        );

        Assert.assertFalse(
            "Identifiable.equals() should return FALSE when compared entity has different ID.",
            entity.equals(other)
        );
    }

    @Test
    public void testHashCode()
    {
        UUID id = UUID.randomUUID();
        AbstractIdentifiableTest.Entity entity = new AbstractIdentifiableTest.Entity();
        entity.id = id;

        Assert.assertEquals(
            "Identifiable.hashCode() should return hash code that is equal to it's ID hash code.",
            id.hashCode(),
            entity.hashCode()
        );
    }

    @Test
    public void testHashCodeNull()
    {
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiableTest.Entity();

        Assert.assertEquals(
            "Identifiable.hashCode() should return 0 if entity is not persisted.",
            0,
            entity.hashCode()
        );
    }
}
