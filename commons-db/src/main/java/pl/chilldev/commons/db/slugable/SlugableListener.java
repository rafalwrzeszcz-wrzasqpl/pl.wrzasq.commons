/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.db.slugable;

import java.lang.reflect.Field;

import javax.inject.Inject;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.commons.lang3.reflect.FieldUtils;

import pl.chilldev.commons.text.slugifier.SimpleSlugifier;
import pl.chilldev.commons.text.slugifier.Slugifier;

/**
 * Event listener for entities that should be identifiable by URL.
 */
public class SlugableListener
{
    /**
     * Slugifier to be used to generate slugs.
     */
    @Inject
    private Slugifier slugifier;

    /**
     * Returns initializer.
     *
     * <p>
     * - In CDI-capable environment it will return slugifier injected by the container.
     * - In non-CDI environment it will initialize default slugifier.
     * </p>
     *
     * @return Slugifier.
     */
    private Slugifier getSlugifier()
    {
        if (this.slugifier == null) {
            this.slugifier = new SimpleSlugifier();
        }

        return this.slugifier;
    }

    /**
     * Generate slug for give value.
     *
     * @param texts Initial value.
     * @param options Slug generation configuration.
     * @return Generated slug.
     */
    private String generateSlug(String[] texts, Slug options)
    {
        // acquire slugs generator
        Slugifier slugifier = this.getSlugifier();

        return options.prefix() + slugifier.slugify(texts) + options.suffix();
    }

    /**
     * Fills slugs declared in given object.
     *
     * @param slugable Object to be handled.
     * @param update Whether it's first save of slugs need to be only re-created.
     * @throws IllegalAccessException When accessing source field is impossible.
     */
    private void fillSlugs(Object slugable, boolean update)
        throws
            IllegalAccessException
    {
        Class<?> type = slugable.getClass();
        int i;

        for (Field field : FieldUtils.getAllFields(type)) {
            if (field.isAnnotationPresent(Slug.class)) {
                // get slug configuration
                Slug options = field.getDeclaredAnnotation(Slug.class);

                // check if it should be updated
                if (!update || options.updatable()) {
                    // find all source fields
                    String[] fields = options.value();
                    String[] texts = new String[fields.length];
                    for (i = 0; i < fields.length; ++i) {
                        texts[i] = FieldUtils.readField(slugable, fields[i], true).toString();
                    }

                    // generate slug
                    FieldUtils.writeField(slugable, field.getName(), this.generateSlug(texts, options), true);
                }
            }
        }
    }

    /**
     * Fills slugs on object creation.
     *
     * @param slugable Object to be handled.
     * @throws IllegalAccessException When accessing source field is impossible.
     */
    @PrePersist
    public void createSlugs(Object slugable)
        throws
            IllegalAccessException
    {
        this.fillSlugs(slugable, false);
    }

    /**
     * Updates slugs.
     *
     * @param slugable Object to be handled.
     * @throws IllegalAccessException When accessing source field is impossible.
     */
    @PreUpdate
    public void updateSlugs(Object slugable)
        throws
            IllegalAccessException
    {
        this.fillSlugs(slugable, true);
    }
}
