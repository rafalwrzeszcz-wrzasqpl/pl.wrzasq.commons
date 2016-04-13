/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.service.config;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import java.util.UUID;

import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.minidev.json.JSONValue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.domain.Sort;

import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import pl.chilldev.commons.jsonrpc.json.ParamsRetriever;
import pl.chilldev.commons.jsonrpc.json.writer.DateTimeWriter;
import pl.chilldev.commons.jsonrpc.json.writer.SortWriter;
import pl.chilldev.commons.jsonrpc.json.writer.StringDumpingWriter;
import pl.chilldev.commons.jsonrpc.rpc.introspector.Introspector;

/**
 * General services definition that are present in every service.
 */
@Configuration
public class BaseApplicationConfiguration
{
    static {
        // register JSON handlers
        ParamsRetriever.OBJECT_MAPPER.registerModule(new JavaTimeModule());
        JSONValue.defaultWriter.registerWriter(new StringDumpingWriter(), UUID.class);
        JSONValue.defaultWriter.registerWriter(new SortWriter(), Sort.class);
        JSONValue.defaultWriter.registerWriter(
            new DateTimeWriter(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            OffsetDateTime.class
        );
    }

    /**
     * Introspector instance.
     *
     * @return Introspector.
     */
    @Bean
    public Introspector introspector()
    {
        return Introspector.createDefault();
    }

    /**
     * Validator instance.
     *
     * @return Validator.
     */
    @Bean
    public Validator validator()
    {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Creates transaction metadata extractor.
     *
     * @return Annotation-bases transaction metadata extractor.
     */
    @Bean
    public TransactionAttributeSource transactionAttributeSource()
    {
        return new AnnotationTransactionAttributeSource();
    }
}
