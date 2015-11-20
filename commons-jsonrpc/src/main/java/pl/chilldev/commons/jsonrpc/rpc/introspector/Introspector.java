/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.rpc.introspector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pl.chilldev.commons.jsonrpc.daemon.ContextInterface;
import pl.chilldev.commons.jsonrpc.json.ParamsRetriever;
import pl.chilldev.commons.jsonrpc.rpc.Dispatcher;
import pl.chilldev.commons.jsonrpc.rpc.handler.VersionHandler;

/**
 * Introspector for facade classes to automatically map method to JSON calls.
 */
public class Introspector
{
    /**
     * Wrapper for parameter provider that reduces dependency to just request parameters retriever.
     *
     * @param <Type> Result type.
     */
    @FunctionalInterface
    interface ParameterProviderWrapper<Type>
    {
        /**
         * Fetches parameter from request.
         *
         * @param params Request parameters.
         * @return Resolved parameter.
         * @throws JSONRPC2Error When resolving parameter fails.
         */
        Type getParam(ParamsRetriever params)
            throws
                JSONRPC2Error;
    }

    /**
     * JSON-RPC call handler.
     *
     * @param <ContextType> Service request context type.
     */
    static class RequestHandler<ContextType extends ContextInterface>
        implements
            Dispatcher.RequestHandler<ContextType>
    {
        /**
         * Target method.
         */
        private String method;

        /**
         * Parameters types.
         */
        private Class<?>[] types;

        /**
         * Parameters providers.
         */
        private Introspector.ParameterProviderWrapper<?>[] params;

        /**
         * Response mapper.
         */
        private Function<Object, Object> mapper;

        /**
         * Initializes handler for given method.
         *
         * @param method Method name.
         * @param types Parameters types.
         * @param params Parameters providers.
         * @param mapper Results mapper.
         */
        RequestHandler(
            String method,
            Class<?>[] types,
            Introspector.ParameterProviderWrapper<?>[] params,
            Function<Object, Object> mapper
        )
        {
            this.method = method;
            this.types = types;
            this.params = params;
            this.mapper = mapper;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JSONRPC2Response process(JSONRPC2Request request, ContextType context)
            throws
                JSONRPC2Error
        {
            ParamsRetriever retriever = new ParamsRetriever(request.getNamedParams());

            try {
                Method method = context.getClass().getMethod(
                    this.method,
                    this.types
                );
                Object[] arguments = new Object[this.types.length];

                // resolve call arguments
                for (int i = 0; i < this.params.length; ++i) {
                    arguments[i] = this.params[i].getParam(retriever);
                }

                // invoke handler method
                Object result;
                try {
                    result = method.invoke(context, arguments);
                } catch (InvocationTargetException error) {
                    // unfold real exception
                    throw error.getCause();
                }

                // `void` method should just return response with ID
                return method.getReturnType().equals(Void.TYPE)
                    ? new JSONRPC2Response(request.getID())
                    : new JSONRPC2Response(this.mapper.apply(result), request.getID());
                //CHECKSTYLE:OFF: IllegalCatchCheck
            } catch (Throwable error) {
                //CHECKSTYLE:ON: IllegalCatchCheck
                if (error instanceof JSONRPC2Error) {
                    // just re-throw to the client
                    throw (JSONRPC2Error) error;
                } else {
                    throw JSONRPC2Error.INTERNAL_ERROR
                        .appendMessage(": " + error.getMessage())
                        .setData(error);
                }
            }
        }
    }

    /**
     * Transparent response mapper.
     */
    private static final Function<Object, Object> IDENTITY_MAPPER = (Object value) -> value;

    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(Introspector.class);

    /**
     * Parameters resolvers.
     */
    private Map<Class<?>, ParameterProvider<?>> resolvers = new HashMap<>();

    /**
     * Results mappers.
     */
    private Map<Class<?>, Function<Object, Object>> mappers = new HashMap<>();

    /**
     * Registers parameter resolver for given class.
     *
     * @param type Parameter type.
     * @param resolver Parameter resolver.
     * @param <Type> Parameter type.
     */
    public <Type> void registerParameterProvider(
        Class<Type> type,
        ParameterProvider<? extends Type> resolver
    )
    {
        this.resolvers.put(type, resolver);
    }

    /**
     * Registers return type handler for given class.
     *
     * @param type Response type.
     * @param mapper Response mapper.
     * @param <Type> Response object type.
     */
    public <Type> void registerResultMapper(
        Class<Type> type,
        Function<? super Type, Object> mapper
    )
    {
        this.mappers.put(
            type,
            (Object response) -> mapper.apply(type.cast(response))
        );
    }

    /**
     * Registers request handlers for methods of given class in provided dispatcher.
     *
     * @param facade Request handling facade.
     * @param dispatcher Methods dispatcher.
     * @param <ContextType> Service request context type (will be used for execution context).
     * @throws IllegalArgumentException When a method of the facade can't be mapped to JSON-RPC call.
     */
    public <ContextType extends ContextInterface> void register(
        Class<? super ContextType> facade,
        Dispatcher<? extends ContextType> dispatcher
    )
    {
        for (Method method : facade.getMethods()) {
            if (method.isAnnotationPresent(JsonRpcCall.class)) {
                this.logger.debug("Found {}.{} method as JSON-RPC handler.", facade.getName(), method.getName());

                JsonRpcCall call = method.getAnnotation(JsonRpcCall.class);

                Parameter[] parameters = method.getParameters();
                Introspector.ParameterProviderWrapper<?>[] providers
                    = new Introspector.ParameterProviderWrapper[parameters.length];

                try {
                    // build parameters resolvers
                    for (int i = 0; i < parameters.length; ++i) {
                        // register synthetic RPC handler
                        providers[i] = this.createParameterProvider(parameters[i]);
                    }
                } catch (IllegalArgumentException error) {
                    throw new IllegalArgumentException(
                        String.format(
                            "%s.%s() cann't be mapped to JSON-RPC handler.",
                            facade.getName(),
                            method.getName()
                        ),
                        error
                    );
                }

                Class<?> response = method.getReturnType();
                dispatcher.register(
                    // use overridden name if set
                    "".equals(call.name()) ? method.getName() : call.name(),
                    new Introspector.RequestHandler<ContextType>(
                        method.getName(),
                        method.getParameterTypes(),
                        providers,
                        // fall back to transparent mapper if no type-specific mapper is registered
                        this.mappers.containsKey(response) ? this.mappers.get(response) : Introspector.IDENTITY_MAPPER
                    )
                );
            }
        }
    }

    /**
     * Creates provider for given parameter.
     *
     * @param parameter Method parameter.
     * @return Parameter provider.
     * @throws IllegalArgumentException When a parameter cann't be resolved from JSON-RPC request.
     */
    private Introspector.ParameterProviderWrapper<?> createParameterProvider(Parameter parameter)
    {
        // try to fetch provider by parameter type
        ParameterProvider<?> provider = this.resolvers.get(parameter.getType());

        // not supported parameter type
        if (provider == null) {
            throw new IllegalArgumentException(
                String.format(
                    "\"%s\" parameter type (%s) is not supported",
                    parameter.getName(),
                    parameter.getType().getName()
                )
            );
        }

        String name = parameter.getName();
        String defaultValue = null;
        boolean optional = false;

        // override defaults if annotation is defined
        JsonRpcParam metadata = parameter.getAnnotation(JsonRpcParam.class);
        if (metadata != null) {
            name = "".equals(metadata.name()) ? name : metadata.name();
            optional = metadata.optional();
            defaultValue = metadata.defaultNull() ? null : metadata.defaultValue();
        }

        return this.createParameterProviderWrapper(provider, name, optional, defaultValue);
    }

    /**
     * Creates parameter provider wrapper for given parameter scope.
     *
     * @param provider Value provider.
     * @param name Parameter name.
     * @param optional Optional flag.
     * @param defaultValue Default value.
     * @param <Type> Parameter type.
     * @return Wrapped parameter provider.
     */
    private <Type> Introspector.ParameterProviderWrapper<Type> createParameterProviderWrapper(
        ParameterProvider<? extends Type> provider,
        String name,
        boolean optional,
        String defaultValue
    )
    {
        return (ParamsRetriever params) -> provider.getParam(name, params, optional, defaultValue);
    }

    /**
     * Creates dispatcher for given facade type.
     *
     * <p>
     * Created dispatcher contains additional <tt>version</tt> method handler.
     * </p>
     *
     * @param type Class to be used as a facade.
     * @param <ContextType> Service request context type (will be used for execution context).
     * @return Dispatcher.
     */
    public <ContextType extends ContextInterface> Dispatcher<ContextType> createDispatcher(Class<ContextType> type)
    {
        Dispatcher<ContextType> dispatcher = new Dispatcher<>();
        dispatcher.register("version", new VersionHandler());

        this.register(type, dispatcher);

        return dispatcher;
    }

    /**
     * Creates introspector with handlers for common types.
     *
     * @return Introspector.
     */
    public static Introspector createDefault()
    {
        Introspector introspector = new Introspector();

        // parameters retrievers

        // boolean retriever
        introspector.registerParameterProvider(
            boolean.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
                return optional
                    ? params.getOptBoolean(name, defaultValue.toLowerCase(Locale.ROOT).equals("true"))
                    : params.getBoolean(name);
            }
        );

        // integer retriever
        introspector.registerParameterProvider(
            int.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
                return optional
                    ? params.getOptInt(name, Integer.parseInt(defaultValue))
                    : params.getInt(name);
            }
        );

        // long retriever
        introspector.registerParameterProvider(
            long.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
                return optional
                    ? params.getOptLong(name, Long.parseLong(defaultValue))
                    : params.getLong(name);
            }
        );

        // string retriever
        introspector.registerParameterProvider(
            String.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
                return optional
                    ? params.getOptString(name, defaultValue)
                    : params.getString(name);
            }
        );

        // UUID retriever
        introspector.registerParameterProvider(
            UUID.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
                return optional
                    ? params.getOptUuid(name)
                    : params.getUuid(name);
            }
        );

        // List retriever
        introspector.registerParameterProvider(
            List.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
                return optional
                    ? params.getOptList(name, Collections.singletonList(defaultValue))
                    : params.getList(name);
            }
        );

        // Spring Data paged request retriever
        introspector.registerParameterProvider(
            Pageable.class,
            (String name, ParamsRetriever params, boolean optional, String defaultValue) -> {
                return params.getPageable(Integer.parseInt(defaultValue));
            }
        );

        // return types handlers

        // Spring Data paged response handler
        introspector.registerResultMapper(
            Page.class,
            (Page page) -> {
                Map<String, Object> result = new HashMap<>();
                result.put("count", page.getTotalElements());
                result.put("records", page.getContent());
                return result;
            }
        );

        return introspector;
    }
}
