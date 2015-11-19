/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.client.introspector;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Pageable;

import pl.chilldev.commons.jsonrpc.client.Connector;
import pl.chilldev.commons.jsonrpc.client.RpcCallException;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcCall;
import pl.chilldev.commons.jsonrpc.rpc.introspector.JsonRpcParam;

/**
 * Introspector for clients classes to automatically map method to JSON calls.
 */
public class Introspector
{
    /**
     * Wrapper for parameter mapper that reduces dependency to just call parameters.
     *
     * @param <Type> Mapping parameter type.
     */
    @FunctionalInterface
    interface ParameterMapperWrapper<Type>
    {
        /**
         * Populates call parameters with given value.
         *
         * @param value Parameter value passed to the call.
         * @param params Current state of RPC call parameters.
         */
        void putParam(Type value, Map<String, Object> params);
    }

    /**
     * RPC method call.
     */
    static class Call
    {
        /**
         * RPC method name.
         */
        protected String name;

        /**
         * Parameters mappers.
         */
        protected List<Introspector.ParameterMapperWrapper<Object>> params;

        /**
         * Response handler.
         */
        protected Function<Object, ?> handler;

        /**
         * Initializes RPC call handler.
         *
         * @param name Request method name.
         * @param params Parameters mappers.
         * @param handler Response handler.
         */
        Call(
            String name,
            List<Introspector.ParameterMapperWrapper<Object>> params,
            Function<Object, ?> handler
        )
        {
            this.name = name;
            this.params = params;
            this.handler = handler;
        }

        /**
         * Executes request on given connector.
         *
         * @param connector TCP connector.
         * @param arguments Request parameters.
         * @return Response result.
         * @throws RpcCallException When execution of remote call fails.
         */
        public Object execute(Connector connector, Object[] arguments)
            throws
                RpcCallException
        {
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < arguments.length; ++i) {
                this.params.get(i).putParam(arguments[i], params);
            }

            return this.handler.apply(
                params.isEmpty()
                    ? connector.execute(this.name)
                    : connector.execute(this.name, params)
            );
        }
    }

    /**
     * RPC calls wrapper.
     */
    public static class Client
    {
        /**
         * TCP connector.
         */
        protected Connector connector;

        /**
         * RPC calls.
         */
        protected Map<Method, Introspector.Call> calls = new HashMap<>();

        /**
         * Initializes service over given client.
         *
         * @param connector TCP connector.
         */
        Client(Connector connector)
        {
            this.connector = connector;
        }

        /**
         * Registers method call handler.
         *
         * @param method Client class method.
         * @param call RPC call handler.
         */
        public void register(Method method, Introspector.Call call)
        {
            this.calls.put(method, call);
        }

        /**
         * Executes the RPC call.
         *
         * @param method Invoked method.
         * @param arguments Call-time arguments.
         * @return Execution result.
         * @throws RpcCallException When execution of remote call fails.
         */
        @RuntimeType
        public Object execute(@Origin Method method, @AllArguments Object[] arguments)
            throws
                RpcCallException
        {
            return this.calls.get(method).execute(this.connector, arguments);
        }
    }

    /**
     * Default parameter mapper.
     */
    protected static final ParameterMapper<Object> DEFAULT_MAPPER
        = (String name, Object value, Map<String, Object> params) -> params.put(name, value);

    /**
     * Transparent response handler.
     */
    protected static final Function<Object, Object> IDENTITY_HANDLER = (Object value) -> value;

    /**
     * Logger.
     */
    protected Logger logger = LoggerFactory.getLogger(Introspector.class);

    /**
     * Parameters mappers.
     */
    protected Map<Class<?>, ParameterMapper<Object>> mappers = new HashMap<>();

    /**
     * Results handlers.
     */
    protected Map<Class<?>, Function<Object, ?>> handlers = new HashMap<>();

    /**
     * Registers parameter mapper for given class.
     *
     * @param type Parameter type.
     * @param mapper Parameter mapper.
     * @param <Type> Parameter type.
     */
    public <Type> void registerParameterMapper(
        Class<Type> type,
        ParameterMapper<? super Type> mapper
    )
    {
        this.mappers.put(
            type,
            (String name, Object value, Map<String, Object> params)
                -> mapper.putParam(name, type.cast(value), params)
        );
    }

    /**
     * Registers response type handler for given class.
     *
     * @param type Response type.
     * @param handler Response handler.
     * @param <Type> Response object type.
     */
    public <Type> void registerResultHandler(
        Class<Type> type,
        Function<Object, ? extends Type> handler
    )
    {
        this.handlers.put(
            type,
            handler
        );
    }

    /**
     * Builds client wrapper that handles all of the JSON-RPC calls.
     *
     * @param type Base client type.
     * @param connector TPC connector to use for the calls.
     * @param <Type> Client type.
     * @return Client instance.
     */
    public <Type> Class<? extends Type> createClient(Class<Type> type, Connector connector)
    {
        return new ByteBuddy()
            .subclass(type)
            .method(ElementMatchers.isAnnotatedWith(JsonRpcCall.class))
            .intercept(MethodDelegation.to(this.buildClient(type, connector)))
            .make()
            .load(this.getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
            .getLoaded();
    }

    /**
     * Creates RPC client.
     *
     * @param type Base client type.
     * @param connector TPC connector to use for the calls.
     * @return RPC client.
     */
    protected Introspector.Client buildClient(Class<?> type, Connector connector)
    {
        Introspector.Client client = new Introspector.Client(connector);

        for (Method method : type.getMethods()) {
            if (method.isAnnotationPresent(JsonRpcCall.class)) {
                this.logger.debug("Found {}.{} method as JSON-RPC request.", type.getName(), method.getName());

                JsonRpcCall call = method.getAnnotation(JsonRpcCall.class);

                Parameter[] parameters = method.getParameters();
                List<Introspector.ParameterMapperWrapper<Object>> mappers
                    = new ArrayList<>(parameters.length);

                // build parameters resolvers
                for (Parameter parameter : parameters) {
                    // register synthetic RPC handler
                    mappers.add(this.createParameterMapper(parameter));
                }

                // response handler
                Class<?> response = method.getReturnType();
                Function<Object, ?> handler = this.handlers.containsKey(response)
                    ? this.handlers.get(response)
                    : Introspector.IDENTITY_HANDLER;

                client.register(
                    // use overridden name if set
                    method,
                    new Introspector.Call(
                        "".equals(call.name()) ? method.getName() : call.name(),
                        mappers,
                        handler
                    )
                );
            }
        }

        return client;
    }

    /**
     * Creates mapper for given parameter.
     *
     * @param parameter Method parameter.
     * @return Parameter provider.
     */
    protected Introspector.ParameterMapperWrapper<Object> createParameterMapper(Parameter parameter)
    {
        // try to fetch provider by parameter type
        Class<?> type = parameter.getType();
        ParameterMapper<Object> mapper = this.mappers.containsKey(type)
            ? this.mappers.get(type)
            : Introspector.DEFAULT_MAPPER;

        String name = parameter.getName();

        // override defaults if annotation is defined
        JsonRpcParam metadata = parameter.getAnnotation(JsonRpcParam.class);
        if (metadata != null) {
            name = "".equals(metadata.name()) ? name : metadata.name();
        }

        return this.createParameterMapperWrapper(mapper, name);
    }

    /**
     * Creates parameter mapper wrapper for given parameter scope.
     *
     * @param mapper Value mapper.
     * @param name Parameter name.
     * @param <Type> Parameter type.
     * @return Wrapped parameter mapper.
     */
    protected <Type> Introspector.ParameterMapperWrapper<Type> createParameterMapperWrapper(
        ParameterMapper<? super Type> mapper,
        String name
    )
    {
        return (Type value, Map<String, Object> params) -> mapper.putParam(name, value, params);
    }

    /**
     * Creates introspector with handlers for common types.
     *
     * @return Introspector.
     */
    public static Introspector createDefault()
    {
        Introspector introspector = new Introspector();

        // parameters mappers

        // Spring Data page request mapper
        introspector.registerParameterMapper(
            Pageable.class,
            (String name, Pageable value, Map<String, Object> params) -> {
                params.put("page", value.getPageNumber());
                params.put("limit", value.getPageSize());
                params.put("sort", value.getSort());
            }
        );

        // response types handlers

        // UUID handling
        introspector.registerResultHandler(
            UUID.class,
            (Object response) -> UUID.fromString(response.toString())
        );

        return introspector;
    }
}
