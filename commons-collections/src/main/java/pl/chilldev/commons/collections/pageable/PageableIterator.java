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
public class PageableIterator<Type> implements Iterator<Type>
{
    /**
     * Current result page.
     */
    protected Slice<? extends Type> page;

    /**
     * Current page data.
     */
    protected Iterator<? extends Type> data;

    /**
     * Data source for paged results.
     */
    protected Function<Pageable, Slice<? extends Type>> source;

    /**
     * Initializes iterator with given starting point.
     *
     * @param page Initial page.
     * @param source Paged results data source.
     */
    public PageableIterator(Slice<? extends Type> page, Function<Pageable, Slice<? extends Type>> source)
    {
        this.source = source;

        this.handlePage(page);
    }

    /**
     * Sets current page data.
     *
     * @param page Current page.
     */
    protected void handlePage(Slice<? extends Type> page)
    {
        this.page = page;
        this.data = page.getContent().iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext()
    {
        return this.data.hasNext() || this.page.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type next()
    {
        // if there is no more data in current page move to next one
        if (!this.data.hasNext()) {
            this.handlePage(this.source.apply(this.page.nextPageable()));
        }

        return this.data.next();
    }
}
