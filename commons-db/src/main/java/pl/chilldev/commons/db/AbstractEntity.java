/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.db;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.GenericGenerator;

/**
 * Core entity features.
 */
@MappedSuperclass
@Accessors(chain = true)
@Getter
@Setter
public abstract class AbstractEntity
{
    /**
     * UUID field length.
     */
    public static final int LENGTH_UUID = 16;

    /**
     * Regular string field length.
     */
    public static final int LENGTH_STRING_STANDARD = 255;

    /**
     * Compact string field length.
     */
    public static final int LENGTH_STRING_COMPACT = 128;

    /**
     * URL field length.
     */
    public static final int LENGTH_URL = 1024;

    /**
     * Record unique identifier.
     */
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = AbstractEntity.LENGTH_UUID)
    private UUID id;

    /**
     * Checks object equality.
     *
     * @param object Comparison subject.
     * @return Comparison result.
     */
    @Override
    public boolean equals(Object object)
    {
        if (this == object) {
            return true;
        }
        if (object == null || !(object instanceof AbstractEntity) || this.id == null) {
            return false;
        }
        return this.id.equals(((AbstractEntity) object).getId());
    }

    /**
     * Generates object ID.
     *
     * @return Identity code.
     */
    @Override
    public int hashCode()
    {
        return this.id == null ? 0 : this.id.hashCode();
    }
}
