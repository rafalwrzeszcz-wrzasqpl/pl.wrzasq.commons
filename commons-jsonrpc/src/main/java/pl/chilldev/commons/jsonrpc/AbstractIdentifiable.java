/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
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
     * Returns record identifier.
     *
     * @return Record identifier.
     */
    public abstract Type getId();

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
        if (object == null || !(object instanceof AbstractIdentifiable) || this.getId() == null) {
            return false;
        }
        return this.getId().equals(((AbstractIdentifiable<?>) object).getId());
    }

    /**
     * Generates object ID.
     *
     * @return Identity code.
     */
    @Override
    public int hashCode()
    {
        return this.getId() == null ? 0 : this.getId().hashCode();
    }
}
