<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Custom JVM serializers

This module provides ready serializers for **Kotlinx serialization** API for `java.util.UUID` and
`java.time.OffsetDateTime` types:

```kotlin
@Serializable
data class ObjectMetaData(
    @Serializable(with = UuidSerializer::class)
    val objectId: UUID,
    val version: Long,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val createdAt: OffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val updatedAt: OffsetDateTime
)
```
