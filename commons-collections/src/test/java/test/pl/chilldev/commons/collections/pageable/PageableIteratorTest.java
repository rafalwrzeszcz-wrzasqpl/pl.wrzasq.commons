/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.collections.pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import pl.chilldev.commons.collections.pageable.PageableIterator;

@RunWith(MockitoJUnitRunner.class)
public class PageableIteratorTest
{
    @Mock
    private Function<Pageable, Slice<? extends Object>> supplier;

    @Mock
    private Slice<? extends Object> page;

    @Mock
    private Slice<? extends Object> secondPage;

    @Test
    public void hasNextData()
    {
        List<Object> content = new ArrayList<>();
        content.add(new Object());

        Mockito.doReturn(content).when(this.page).getContent();

        PageableIterator<Object> iterator = new PageableIterator<>(this.page, this.supplier);

        Assert.assertTrue(
            "PageableIterator.hasNext() should return TRUE if there is any data left in current chunk.",
            iterator.hasNext()
        );
    }

    @Test
    public void hasNextPage()
    {
        List<Object> content = new ArrayList<>();

        Mockito.doReturn(content).when(this.page).getContent();
        Mockito.doReturn(true).when(this.page).hasNext();

        PageableIterator<Object> iterator = new PageableIterator<>(this.page, this.supplier);

        Assert.assertTrue(
            "PageableIterator.hasNext() should return TRUE if there is next page of results.",
            iterator.hasNext()
        );
    }

    @Test
    public void hasNextFalse()
    {
        List<Object> content = new ArrayList<>();

        Mockito.doReturn(content).when(this.page).getContent();
        Mockito.doReturn(false).when(this.page).hasNext();

        PageableIterator<Object> iterator = new PageableIterator<>(this.page, this.supplier);

        Assert.assertFalse(
            "PageableIterator.hasNext() should return FALSE if there is nothing more in buffer and no next page.",
            iterator.hasNext()
        );
    }

    @Test
    public void nextData()
    {
        Object element = new Object();
        List<Object> content = new ArrayList<>();
        content.add(element);

        Mockito.doReturn(content).when(this.page).getContent();

        PageableIterator<Object> iterator = new PageableIterator<>(this.page, this.supplier);

        Assert.assertSame(
            "PageableIterator.next() should return element from buffer.",
            element,
            iterator.next()
        );
    }

    @Test
    public void nextPage()
    {
        Object element = new Object();
        List<Object> content = new ArrayList<>();
        content.add(element);

        Pageable request = new PageRequest(1, 10);

        Mockito.doReturn(new ArrayList<>()).when(this.page).getContent();
        Mockito.doReturn(request).when(this.page).nextPageable();
        Mockito.doReturn(this.secondPage).when(this.supplier).apply(request);
        Mockito.doReturn(content).when(this.secondPage).getContent();

        PageableIterator<Object> iterator = new PageableIterator<>(this.page, this.supplier);

        Assert.assertSame(
            "PageableIterator.next() should load next page if current buffer is empty.",
            element,
            iterator.next()
        );
    }
}
