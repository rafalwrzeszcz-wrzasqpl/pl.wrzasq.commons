/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.dynamodb.mapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.wrzasq.commons.dynamodb.mapper.OffsetDateTimeMapper;

public class OffsetDateTimeMapperTest {
    @Test
    public void convert() {
        var dateTime = OffsetDateTime.of(2011, 1, 30, 14, 58, 0, 0, ZoneOffset.ofHours(1));
        var mapper = new OffsetDateTimeMapper();

        Assertions.assertEquals(
            "2011-01-30T14:58:00+01:00",
            mapper.convert(dateTime),
            "OffsetDateTimeMapper.convert() should return ISO date format."
        );
    }

    @Test
    public void unconvert() {
        var dateTime = OffsetDateTime.of(2015, 7, 2, 3, 20, 0, 0, ZoneOffset.ofHours(2));
        var mapper = new OffsetDateTimeMapper();

        Assertions.assertEquals(
            dateTime,
            mapper.unconvert("2015-07-02T03:20:00+02:00"),
            "OffsetDateTimeMapper.unconvert() should parse ISO date format."
        );
    }
}
