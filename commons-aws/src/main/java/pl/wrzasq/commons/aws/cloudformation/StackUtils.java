/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2019 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.wrzasq.commons.aws.cloudformation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Custom utility routines for dealing with CloudFormation data operations.
 */
public class StackUtils {
    /**
     * Converts key-value mapping into SDK data structure.
     *
     * @param input Key-value plain values.
     * @param converter Converter for single key-value tuple into SDK structure.
     * @param <Type> SDK element type.
     * @return List of SDK elements.
     */
    public static <Type> Collection<Type> buildSdkList(
        Map<String, String> input,
        BiFunction<String, String, Type> converter
    ) {
        return Optional.ofNullable(input)
            .orElse(Collections.emptyMap())
            .entrySet()
            .stream()
            .map(entry -> converter.apply(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
}
