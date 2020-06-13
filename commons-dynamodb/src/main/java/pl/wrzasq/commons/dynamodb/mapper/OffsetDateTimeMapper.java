/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2020 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.dynamodb.mapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

/**
 * Java 8 Time API date time object mapper.
 */
public class OffsetDateTimeMapper implements DynamoDBTypeConverter<String, OffsetDateTime> {
    /**
     * {@inheritDoc}
     */
    @Override
    public String convert(OffsetDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OffsetDateTime unconvert(String value) {
        return OffsetDateTime.parse(value);
    }
}
