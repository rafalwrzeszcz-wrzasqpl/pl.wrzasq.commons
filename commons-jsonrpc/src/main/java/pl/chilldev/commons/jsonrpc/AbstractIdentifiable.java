/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Base class for transfer objects that can be identified by key.
 *
 * @param <Type> Key type.
 */
@Accessors(chain = true)
@Getter
@Setter
public abstract class AbstractIdentifiable<Type extends Serializable>
{
    /**
     * Object identifier.
     */
    private Type id;

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
