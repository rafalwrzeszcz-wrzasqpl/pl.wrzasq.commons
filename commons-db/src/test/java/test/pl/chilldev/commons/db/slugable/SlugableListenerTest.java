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
        protected String name;

        protected Integer value;

        @Slug("name")
        protected String updatable;

        @Slug(value = "name", updatable = false)
        protected String initial;

        @Slug(value = {"name", "value"}, prefix = "chilldev-", suffix = ".xhtml")
        protected String combined;

        public String getName()
        {
            return this.name;
        }

        public void setName(String value)
        {
            this.name = value;
        }

        public Integer getValue()
        {
            return this.value;
        }

        public void setValue(Integer value)
        {
            this.value = value;
        }

        public String getUpdatable()
        {
            return this.updatable;
        }

        public void setUpdatable(String value)
        {
            this.updatable = value;
        }

        public String getInitial()
        {
            return this.initial;
        }

        public void setInitial(String value)
        {
            this.initial = value;
        }

        public String getCombined()
        {
            return this.combined;
        }

        public void setCombined(String value)
        {
            this.combined = value;
        }
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
