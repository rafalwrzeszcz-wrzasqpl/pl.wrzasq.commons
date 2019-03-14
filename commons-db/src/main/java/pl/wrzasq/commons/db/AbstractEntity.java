/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.db;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(of = "id")
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
}
