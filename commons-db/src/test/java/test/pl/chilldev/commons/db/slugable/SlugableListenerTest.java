/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.db.slugable;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals(
            "SlugableListener.createSlugs() should create slug for annotated field.",
            "chillout-development",
            entity.updatable
        );
        Assert.assertEquals(
            "SlugableListener.createSlugs() should create slug for non-updatable field.",
            "chillout-development",
            entity.initial
        );
        Assert.assertEquals(
            "SlugableListener.createSlugs() should handle complex slug configuration.",
            "chilldev-chillout-development-88.xhtml",
            entity.combined
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

        Assert.assertEquals(
            "SlugableListener.updateSlugs() should create slug for annotated field.",
            "chillout-development",
            entity.updatable
        );
        Assert.assertNull(
            "SlugableListener.updateSlugs() should not touch non-updatable field.",
            entity.initial
        );
        Assert.assertEquals(
            "SlugableListener.updateSlugs() should handle complex slug configuration.",
            "chilldev-chillout-development-88.xhtml",
            entity.combined
        );
    }
}
