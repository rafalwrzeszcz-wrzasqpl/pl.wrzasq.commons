/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import pl.chilldev.commons.db.AbstractEntity;

public class AbstractEntityTest
{
    @Test
    public void getId()
    {
        AbstractEntity entity = new Entity();

        Assert.assertNull(
            "AbstractEntity.getId() should return NULL if entity is not persisted.",
            entity.getId()
        );
    }

    @Test
    public void setId()
    {
        UUID id = UUID.randomUUID();
        AbstractEntity entity = new Entity();
        entity.setId(id);

        Assert.assertEquals(
            "AbstractEntity.getId() should return previously set ID if assigned.",
            id,
            entity.getId()
        );
    }

    @Test
    public void equalsSame()
    {
        AbstractEntity entity = new Entity();
        Assert.assertTrue(
            "AbstractEntity.equals() should return TRUE if the object is the very same instance.",
            entity.equals(entity)
        );
    }

    @Test
    public void equalsNull()
    {
        AbstractEntity entity = new Entity();
        Assert.assertFalse(
            "AbstractEntity.equals() should return FALSE when compared to NULL.",
            entity.equals(null)
        );
    }

    @Test
    public void equalsNotEntity()
    {
        UUID id = UUID.randomUUID();
        AbstractEntity entity = new Entity();
        Assert.assertFalse(
            "AbstractEntity.equals() should return FALSE when compared to a non-entity object.",
            entity.equals(id)
        );
    }

    @Test
    public void equalsWithoutIds()
    {
        AbstractEntity entity = new Entity();
        AbstractEntity other = new Entity();

        Assert.assertTrue(
            "AbstractEntity.equals() should return TRUE when objects have no IDs specified.",
            entity.equals(other)
        );
    }

    @Test
    public void equalsWithoutOwnId()
    {
        UUID id = UUID.randomUUID();

        AbstractEntity entity = new Entity();
        entity.setId(id);

        AbstractEntity other = new Entity();

        Assert.assertFalse(
            "AbstractEntity.equals() should return FALSE when object has no ID specified.",
            entity.equals(other)
        );
    }

    @Test
    public void equalsWithoutOther()
    {
        UUID id = UUID.randomUUID();

        AbstractEntity entity = new Entity();

        AbstractEntity other = new Entity();
        other.setId(id);

        Assert.assertFalse(
            "AbstractEntity.equals() should return FALSE when subject has no ID specified.",
            entity.equals(other)
        );
    }

    @Test
    public void equals()
    {
        UUID id = UUID.randomUUID();

        AbstractEntity same = new Entity();
        same.setId(id);

        AbstractEntity other = new Entity();
        other.setId(UUID.randomUUID());

        AbstractEntity entity = new Entity();
        entity.setId(id);

        Assert.assertTrue(
            "AbstractEntity.equals() should return TRUE when compared entity has same ID.",
            entity.equals(same)
        );

        Assert.assertFalse(
            "AbstractEntity.equals() should return FALSE when compared entity has different ID.",
            entity.equals(other)
        );
    }

    @Test
    public void hashCodeWithoutId()
    {
        AbstractEntity entity = new Entity();
        AbstractEntity other = new Entity();

        Assert.assertEquals(
            "AbstractEntity.hashCode() should calculate same hash code if there is no ID specified.",
            entity.hashCode(),
            other.hashCode()
        );
    }

    @Test
    public void hashCodeSame()
    {
        UUID id = UUID.randomUUID();

        AbstractEntity entity = new Entity();
        entity.setId(id);

        AbstractEntity other = new Entity();
        other.setId(id);

        Assert.assertEquals(
            "AbstractEntity.hashCode() should calculate same hash code if there is same ID specified.",
            entity.hashCode(),
            other.hashCode()
        );
    }

    @Test
    public void hashCodeDifferent()
    {
        AbstractEntity entity = new Entity();
        entity.setId(UUID.randomUUID());

        AbstractEntity other = new Entity();
        other.setId(UUID.randomUUID());

        Assert.assertNotEquals(
            "AbstractEntity.hashCode() should calculate different hash code if there are different IDs specified.",
            entity.hashCode(),
            other.hashCode()
        );
    }
}

class Entity extends AbstractEntity
{
}
