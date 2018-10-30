/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015, 2018 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.db.slugable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.chilldev.commons.db.slugable.Slug;
import pl.chilldev.commons.db.slugable.SlugableListener;

public class SlugableListenerTest
{
    public class TestEntity
    {
        private String name;

        private Integer value;

        @Slug("name")
        private String updatable;

        @Slug(value = "name", updatable = false)
        private String initial;

        @Slug(value = {"name", "value"}, prefix = "chilldev-", suffix = ".xhtml")
        private String combined;
    }

    @Test
    public void createSlugs()
        throws
            IllegalAccessException
    {
        // build entity
        SlugableListenerTest.TestEntity entity = new SlugableListenerTest.TestEntity();
        entity.name = "Chillout Development";
        entity.value = 88;

        SlugableListener listener = new SlugableListener();
        listener.createSlugs(entity);

        Assertions.assertEquals(
            "chillout-development",
            entity.updatable,
            "SlugableListener.createSlugs() should create slug for annotated field."
        );
        Assertions.assertEquals(
            "chillout-development",
            entity.initial,
            "SlugableListener.createSlugs() should create slug for non-updatable field."
        );
        Assertions.assertEquals(
            "chilldev-chillout-development-88.xhtml",
            entity.combined,
            "SlugableListener.createSlugs() should handle complex slug configuration."
        );

        // this is for code coverage of getSlugifier() method
        listener.createSlugs(entity);
    }

    @Test
    public void updateSlugs()
        throws
            IllegalAccessException
    {
        // build entity
        SlugableListenerTest.TestEntity entity = new SlugableListenerTest.TestEntity();
        entity.name = "Chillout Development";
        entity.value = 88;

        SlugableListener listener = new SlugableListener();
        listener.updateSlugs(entity);

        Assertions.assertEquals(
            "chillout-development",
            entity.updatable,
            "SlugableListener.updateSlugs() should create slug for annotated field."
        );
        Assertions.assertNull(
            entity.initial,
            "SlugableListener.updateSlugs() should not touch non-updatable field."
        );
        Assertions.assertEquals(
            "chilldev-chillout-development-88.xhtml",
            entity.combined,
            "SlugableListener.updateSlugs() should handle complex slug configuration."
        );
    }
}
