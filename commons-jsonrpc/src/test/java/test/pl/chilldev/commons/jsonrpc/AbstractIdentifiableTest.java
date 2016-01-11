/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.jsonrpc;

import java.util.UUID;

import org.junit.Test;
import org.junit.Assert;

import pl.chilldev.commons.jsonrpc.AbstractIdentifiable;

public class AbstractIdentifiableTest
{
    @Test
    public void setId()
    {
        UUID id = UUID.randomUUID();
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiable<UUID>() {};
        entity.setId(id);

        Assert.assertEquals(
            "AbstractIdentifiable.setId() should set object identifier.",
            id,
            entity.getId()
        );
    }

    @Test
    public void equalsSame()
    {
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiable<UUID>() {};
        Assert.assertTrue(
            "AbstractIdentifiable.equals() should return TRUE if the object is the very same instance.",
            entity.equals(entity)
        );
    }

    @Test
    public void equalsNull()
    {
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiable<UUID>() {};
        Assert.assertFalse(
            "Identifiable.equals() should return FALSE when compared to NULL.",
            entity.equals(null)
        );
    }

    @Test
    public void equalsNotEntity()
    {
        UUID id = UUID.randomUUID();
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiable<UUID>() {};
        Assert.assertFalse(
            "AbstractIdentifiable.equals() should return FALSE when compared to a non-entity object.",
            entity.equals(id)
        );
    }

    @Test
    public void equalsNullId()
    {
        AbstractIdentifiable<UUID> object = new AbstractIdentifiable<UUID>() {};
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiable<UUID>() {};
        Assert.assertFalse(
            "AbstractIdentifiable.equals() should return FALSE when it is not persisted.",
            entity.equals(object)
        );
    }

    @Test
    public void equals()
    {
        UUID id = UUID.randomUUID();

        AbstractIdentifiable<UUID> same = new AbstractIdentifiable<UUID>() {};
        same.setId(id);

        AbstractIdentifiable<UUID> other = new AbstractIdentifiable<UUID>() {};
        other.setId(UUID.randomUUID());

        AbstractIdentifiable<UUID> entity = new AbstractIdentifiable<UUID>() {};
        entity.setId(id);

        Assert.assertTrue(
            "AbstractIdentifiable.equals() should return TRUE when compared entity has same ID.",
            entity.equals(same)
        );

        Assert.assertFalse(
            "AbstractIdentifiable.equals() should return FALSE when compared entity has different ID.",
            entity.equals(other)
        );
    }

    @Test
    public void testHashCode()
    {
        UUID id = UUID.randomUUID();
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiable<UUID>() {};
        entity.setId(id);

        Assert.assertEquals(
            "AbstractIdentifiable.hashCode() should return hash code that is equal to it's ID hash code.",
            id.hashCode(),
            entity.hashCode()
        );
    }

    @Test
    public void testHashCodeNull()
    {
        AbstractIdentifiable<UUID> entity = new AbstractIdentifiable<UUID>() {};

        Assert.assertEquals(
            "AbstractIdentifiable.hashCode() should return 0 if entity is not persisted.",
            0,
            entity.hashCode()
        );
    }
}
