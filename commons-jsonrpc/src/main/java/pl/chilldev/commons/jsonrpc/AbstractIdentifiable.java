/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc;

import java.io.Serializable;

/**
 * Base class for transfer objects that can be identified by key.
 *
 * @param <Type> Key type.
 */
public abstract class AbstractIdentifiable<Type extends Serializable>
{
    /**
     * Object identifier.
     */
    private Type id;

    /**
     * Returns record identifier.
     *
     * @return Record identifier.
     */
    public Type getId()
    {
        return this.id;
    }

    /**
     * Sets record identifier.
     *
     * @param id Identifier.
     * @return Self instance.
     */
    public AbstractIdentifiable<Type> setId(Type id)
    {
        this.id = id;

        return this;
    }

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
        if (object == null || !(object instanceof AbstractIdentifiable) || this.id == null) {
            return false;
        }
        return this.id.equals(((AbstractIdentifiable<?>) object).id);
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
