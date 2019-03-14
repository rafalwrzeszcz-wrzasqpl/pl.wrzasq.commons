<!---
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015, 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# AbstractEntity

`pl.wrzasq.commons.db.AbstractEntity` is base class entity that we use in our projects. It uses `java.util.UUID` as a primary key identifier. It's stored in database as a binary field so all of the operations should be possibly as quick as on integer keys.

Using **UUID** ensures us that particular object, no matter on which system and of what type, is unique. Thanks to that we can make `.equals()` (and `.hashCode()`) implementations much easier by just relying on the ID. It's also easier with UUID to perform migrations between shards.
