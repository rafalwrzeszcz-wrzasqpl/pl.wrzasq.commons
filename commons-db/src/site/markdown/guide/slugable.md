<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# URL-friendly entities

`commons-db` package provides easy way for making URLs for your entities. This task can be handled by `pl.chilldev.commons.db.slugable.SlugableListener`. To use it you have to define it as your **JPA** `@EntityListener` and annotate fields that should store URL fragments with `pl.chilldev.commons.db.slugable.Slug` annotation.

Here is a minimum example:

```java
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;

import pl.chilldev.commons.db.slugable.Slug;
import pl.chilldev.commons.db.slugable.SlugableListener;

@Entity
@EntityListeners(SlugableListener.class)
public class Entity
{
    // your entity title
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Slug("name")
    private String slug;
}
```

This will result in field `slug` being automatically generated every time you modify (also on the initial creation of the entity) `name` field. For example doing:

```java
Entity entity = new Entity();
entity.name = "Test";
em.save(entity);
```

Will save value `test` in `slug` field.

**Note:** For more information about how slugifying works see `commons-text` documentation.

## More complex example

`@Slug` annotation has a little more features. First of all you can define more source fields to build slug from (they don't need to be of type `String`, they will be casted to it):

```java
public class Entity
{
    // your entity title
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer page;

    @Column(nullable = false)
    @Slug({"name", "page"})
    private String slug;
}
```

Will result in slugs like `page-title-1`, `page-title-2`, etc. - where `1` and `2` and `page` property values.

You can also specify prefix ans suffix for generated slugs:

```java
public class Entity
{
    // your entity title
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Slug(value = "name", prefix = "pages/", suffix = ".xhtml")
    private String slug;
}
```

Now it will generate `pages/page-title.xhtml`.

You can also mark slug field to not be updated after source property change:

```java
public class Entity
{
    // your entity title
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Slug(value = "name", updateable = false)
    private String slug;
}
```

This way only initial slug value will be saved, any further changes done to `name` field won't result in `slug` field changes.
