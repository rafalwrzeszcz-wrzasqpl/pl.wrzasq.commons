/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.collections.pageable;

import java.util.Iterator;
import java.util.function.Function;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

/**
 * Collection class representing paged resource.
 *
 * @param <Type> Collection element type.
 */
public class PageableCollection<Type> implements Iterable<Type>
{
    /**
     * Initial page request.
     */
    protected Pageable request;

    /**
     * Data source for paged results.
     */
    protected Function<Pageable, Slice<? extends Type>> source;

    /**
     * Initializes collection for given starting point.
     *
     * @param request Initial page request.
     * @param source Paged results data source.
     */
    public PageableCollection(Pageable request, Function<Pageable, Slice<? extends Type>> source)
    {
        this.request = request;
        this.source = source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Type> iterator()
    {
        return new PageableIterator<Type>(this.source.apply(this.request), this.source);
    }
}
