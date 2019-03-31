/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016, 2018 - 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.db;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.db.AbstractEntity;

public class AbstractEntityTest {
    @Test
    public void getId() {
        AbstractEntity entity = new Entity();

        Assertions.assertNull(
            entity.getId(),
            "AbstractEntity.getId() should return NULL if entity is not persisted."
        );
    }

    @Test
    public void setId() {
        UUID id = UUID.randomUUID();
        AbstractEntity entity = new Entity();
        entity.setId(id);

        Assertions.assertEquals(
            id,
            entity.getId(),
            "AbstractEntity.getId() should return previously set ID if assigned."
        );
    }

    @Test
    public void equalsSame() {
        AbstractEntity entity = new Entity();
        Assertions.assertEquals(
            entity,
            entity,
            "AbstractEntity.equals() should return TRUE if the object is the very same instance."
        );
    }

    @Test
    public void equalsNull() {
        AbstractEntity entity = new Entity();
        Assertions.assertNotEquals(
            entity,
            null,
            "AbstractEntity.equals() should return FALSE when compared to NULL."
        );
    }

    @Test
    public void equalsNotEntity() {
        UUID id = UUID.randomUUID();
        AbstractEntity entity = new Entity();
        Assertions.assertNotEquals(
            entity,
            id,
            "AbstractEntity.equals() should return FALSE when compared to a non-entity object."
        );
    }

    @Test
    public void equalsWithoutIds() {
        AbstractEntity entity = new Entity();
        AbstractEntity other = new Entity();

        Assertions.assertEquals(
            entity,
            other,
            "AbstractEntity.equals() should return TRUE when objects have no IDs specified."
        );
    }

    @Test
    public void equalsWithoutOwnId() {
        UUID id = UUID.randomUUID();

        AbstractEntity entity = new Entity();
        entity.setId(id);

        AbstractEntity other = new Entity();

        Assertions.assertNotEquals(
            entity,
            other,
            "AbstractEntity.equals() should return FALSE when object has no ID specified."
        );
    }

    @Test
    public void equalsWithoutOther() {
        UUID id = UUID.randomUUID();

        AbstractEntity entity = new Entity();

        AbstractEntity other = new Entity();
        other.setId(id);

        Assertions.assertNotEquals(
            entity,
            other,
            "AbstractEntity.equals() should return FALSE when subject has no ID specified."
        );
    }

    @Test
    public void equals() {
        UUID id = UUID.randomUUID();

        AbstractEntity same = new Entity();
        same.setId(id);

        AbstractEntity other = new Entity();
        other.setId(UUID.randomUUID());

        AbstractEntity entity = new Entity();
        entity.setId(id);

        Assertions.assertEquals(
            entity,
            same,
            "AbstractEntity.equals() should return TRUE when compared entity has same ID."
        );

        Assertions.assertNotEquals(
            entity,
            other,
            "AbstractEntity.equals() should return FALSE when compared entity has different ID."
        );
    }

    @Test
    public void hashCodeWithoutId() {
        AbstractEntity entity = new Entity();
        AbstractEntity other = new Entity();

        Assertions.assertEquals(
            entity.hashCode(),
            other.hashCode(),
            "AbstractEntity.hashCode() should calculate same hash code if there is no ID specified."
        );
    }

    @Test
    public void hashCodeSame() {
        UUID id = UUID.randomUUID();

        AbstractEntity entity = new Entity();
        entity.setId(id);

        AbstractEntity other = new Entity();
        other.setId(id);

        Assertions.assertEquals(
            entity.hashCode(),
            other.hashCode(),
            "AbstractEntity.hashCode() should calculate same hash code if there is same ID specified."
        );
    }

    @Test
    public void hashCodeDifferent() {
        AbstractEntity entity = new Entity();
        entity.setId(UUID.randomUUID());

        AbstractEntity other = new Entity();
        other.setId(UUID.randomUUID());

        Assertions.assertNotEquals(
            entity.hashCode(),
            other.hashCode(),
            "AbstractEntity.hashCode() should calculate different hash code if there are different IDs specified."
        );
    }
}

class Entity extends AbstractEntity {
}
