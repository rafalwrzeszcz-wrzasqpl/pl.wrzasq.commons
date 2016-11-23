/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.collections.pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import pl.chilldev.commons.collections.pageable.PageableCollection;

@RunWith(MockitoJUnitRunner.class)
public class PageableCollectionTest
{
    @Mock
    private Function<Pageable, Slice<?>> supplier;

    @Mock
    private Slice<?> page;

    @Test
    public void iterator()
    {
        List<Object> content = new ArrayList<>();

        Pageable request = new PageRequest(0, 10);
        Mockito.doReturn(content).when(this.page).getContent();
        Mockito.doReturn(this.page).when(this.supplier).apply(request);

        Iterable<Object> collection = new PageableCollection<>(request, this.supplier);
        collection.iterator();
    }
}
