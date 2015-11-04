<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# `PageableIterator`

`pl.chilldev.commons.collections.pageable.PageableIterator` allows to iterate continuously through paged **Spring Data** results. It needs initial page (doesn't have to be first one, just the one you want to start with) and supplier method that will provide next page results when the current one is finished.

You can also use `pl.chilldev.commons.collections.pageable.PageableCollection` to initialize new iterator - it's suitable for enhanced `for ( in )` loops:

```java
import org.springframework.data.domain.Pageable;

import pl.chilldev.commons.collections.pageable.PageableCollection;

// small note - unlike PageableIterator, you initialize PageableColleciton with the request
// it's to allow re-using in multiple iterations
Pageable request = new PageRequest(0, 100);
PageableCollection<YourEntity> pagedCollection = new PageableCollection<>(
    request,
    (Pageable pageRequest) -> fetchYourPagedResult(pageRequest)
);
for (YourEntity entity : pagedCollection) {
    // the iterator will automatically move to next page if there is any
}
```
