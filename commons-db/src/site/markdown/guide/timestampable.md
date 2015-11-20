<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Automatic timestamp generation

Using `@Timestamp` annotation and `import pl.chilldev.commons.db.timestampable.TimestampableListener` allows you to easily automate operation timestamp in your record, without implementing your own events/lifecycle callbacks:

```java
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;

import pl.chilldev.commons.db.timestampable.Timestamp;
import pl.chilldev.commons.db.timestampable.TimestampType;
import pl.chilldev.commons.db.timestampable.TimestampableListener;

@Entity
@EntityListeners(TimestampableListener.class)
public class Entity
{
    @Column(nullable = false)
    @Timestamp(TimestampType.CREATE)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    @Timestamp({ TimestampType.CREATE, TimestampType.UPDATE })
    private OffsetDateTime updatedAt;
}
```

**Note:** Annotating a field `updatedAt` with just `TimestampType.UPDATE` (without `CREATE`) would result in no timestamp generation on first entity save.

**Note:** Right now, as the library is just the result of internal **Chillout Development** work, timestamps are always of type `java.time.OffsetDateTime`. In case you use different temporal time post and issue, or (even better) pull request.
