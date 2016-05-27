/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.service.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;

/**
 * Generic utilities for service facades.
 */
public abstract class AbstractApiFacade
{
    /**
     * Data validator.
     */
    private Validator validator;

    /**
     * Initialize facade with data validator.
     *
     * @param validator Data validator.
     */
    public AbstractApiFacade(Validator validator)
    {
        this.validator = validator;
    }

    /**
     * Maps list of one type into another.
     *
     * @param source Source collection.
     * @param mapper Mapping function.
     * @param <Source> Input type.
     * @param <Destination> Result type.
     * @return List of items constructed by mapping.
     */
    public static <Source, Destination> List<Destination> mapList(
        Collection<Source> source,
        Function<? super Source, ? extends Destination> mapper)
    {
        return source.stream()
            .map(mapper)
            .collect(Collectors.toList());
    }

    /**
     * Validates entity before saving.
     *
     * @param entity Entity to be validated.
     * @param <Type> Validated object type.
     * @throws JSONRPC2Error In case of validation errors.
     */
    public <Type> void validateEntity(Type entity)
        throws
            JSONRPC2Error
    {
        Set<ConstraintViolation<Type>> errors = this.validator.validate(entity);
        if (!errors.isEmpty()) {
            List<String> messages = new ArrayList<>();
            for (ConstraintViolation<Type> error : errors) {
                messages.add(error.getMessage());
            }

            throw JSONRPC2Error.INVALID_PARAMS.appendMessage(
                String.format(": %s", String.join(", ", messages))
            );
        }
    }
}
