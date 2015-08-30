/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2015 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.jsonrpc.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Enhanced version of parameters retriever.
 */
public class ParamsRetriever extends NamedParamsRetriever
{
    /**
     * Default page number parameter name.
     */
    public static final String DEFAULTPARAM_PAGE = "page";

    /**
     * Default records limit parameter name.
     */
    public static final String DEFAULTPARAM_LIMIT = "limit";

    /**
     * Default sort specification parameter name.
     */
    public static final String DEFAULTPARAM_SORT = "sort";

    /**
     * Jackson data binder.
     */
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Initializes retriever with given parameters set.
     *
     * @param params Parameters map.
     */
    public ParamsRetriever(Map<String, Object> params)
    {
        super(params);
    }

    /**
     * Initializes retriever for given request.
     *
     * @param request JSON-RPC request.
     */
    public ParamsRetriever(JSONRPC2Request request)
    {
        super(
            request.getParamsType() == JSONRPC2ParamsType.NO_PARAMS
            ? new HashMap<String, Object>()
            : request.getNamedParams()
        );
    }

    /**
     * Returns UUID identifier from given parameter (not allowing null).
     *
     * @param param Parameter name.
     * @return Parsed UUID identifier.
     * @throws JSONRPC2Error On a missing parameter, bad type or null value (JSONRPC2Error.INVALID_PARAMS).
     */
    public UUID getUuid(String param)
        throws
            JSONRPC2Error
    {
        return this.getUuid(param, false);
    }

    /**
     * Returns UUID identifier from given parameter.
     *
     * @param param Parameter name.
     * @param allowNull Whether to allow NULL values or throw exception in such case.
     * @return Parsed UUID identifier.
     * @throws JSONRPC2Error On a missing parameter or bad type or null value (JSONRPC2Error.INVALID_PARAMS).
     */
    public UUID getUuid(String param, boolean allowNull)
        throws
            JSONRPC2Error
    {
        String uuid = this.getString(param, allowNull);

        return ParamsRetriever.parseUuid(param, uuid, allowNull);
    }

    /**
     * Returns UUID identifier from given optional parameter (not allowing null).
     *
     * @param param Parameter name.
     * @return Parsed UUID identifier. Note, that it may be NULL, if parameter is not set, but not when set to NULL.
     * @throws JSONRPC2Error On a missing parameter, bad type or null value (JSONRPC2Error.INVALID_PARAMS).
     */
    public UUID getOptUuid(String param)
        throws
            JSONRPC2Error
    {
        return this.getOptUuid(param, false);
    }

    /**
     * Returns UUID identifier from given optional parameter.
     *
     * @param param Parameter name.
     * @param allowNull Whether to allow NULL values or throw exception in such case.
     * @return Parsed UUID identifier.
     * @throws JSONRPC2Error On a missing parameter, bad type or null value (JSONRPC2Error.INVALID_PARAMS).
     */
    public UUID getOptUuid(String param, boolean allowNull)
        throws
            JSONRPC2Error
    {
        if (!this.hasParam(param)) {
            return null;
        }

        String uuid = this.getOptString(param, allowNull, null);

        return ParamsRetriever.parseUuid(param, uuid, allowNull);
    }

    /**
     * Parses UUID string.
     *
     * @param param Parameter name.
     * @param value Parameter value.
     * @param allowNull Whether to allow NULL values or throw exception in such case.
     * @return Parsed UUID identifier.
     * @throws JSONRPC2Error On bad format or null value (JSONRPC2Error.INVALID_PARAMS).
     */
    protected static UUID parseUuid(String param, String value, boolean allowNull)
        throws
            JSONRPC2Error
    {
        try {
            // return null only if it's allowed
            if (allowNull && value == null) {
                return null;
            }

            // parse UUID from parameter
            return UUID.fromString(value);
        } catch (IllegalArgumentException error) {
            throw JSONRPC2Error.INVALID_PARAMS.appendMessage(
                String.format(": Parameter \"%s\" is not valid UUID.", param)
            );
        }
    }

    /**
     * Builds sort ordering criteria list.
     *
     * @param param Parameter name.
     * @return Parsed sort ordering criteria (NULL if empty).
     * @throws JSONRPC2Error On a missing parameter, bad type or null value (JSONRPC2Error.INVALID_PARAMS).
     */
    public Sort getSort(String param)
        throws
            JSONRPC2Error
    {
        // grab all sort orders
        List<Sort.Order> sort = new ArrayList<>();
        List<?> property;
        for (Object order : this.getOptList(param, Collections.emptyList())) {
            if (order instanceof List) {
                property = (List<?>) order;
                sort.add(
                    new Sort.Order(
                        Sort.Direction.valueOf(property.get(1).toString()),
                        property.get(0).toString()
                    )
                );
            } else {
                throw JSONRPC2Error.INVALID_PARAMS.appendMessage(
                    String.format(": Parameter \"%s\" is not valid Sort definition.", param)
                );
            }
        }

        return sort.size() > 0 ? new Sort(sort) : null;
    }

    /**
     * Builds sort ordering criteria list.
     *
     * @param pageParam Page number parameter name.
     * @param limitParam Results limit parameter name.
     * @param sortParam Sort specification parameter name.
     * @param defaultLimit Default page limit.
     * @return Paged request specification.
     * @throws JSONRPC2Error On a missing parameter, bad type or null value (JSONRPC2Error.INVALID_PARAMS).
     */
    public Pageable getPageable(String pageParam, String limitParam, String sortParam, int defaultLimit)
        throws
            JSONRPC2Error
    {
        return new PageRequest(
            this.getOptInt(pageParam, 0),
            this.getOptInt(limitParam, defaultLimit),
            this.getSort(sortParam)
        );
    }

    /**
     * Builds paged request data from default parameters names.
     *
     * @param defaultLimit Default page limit.
     * @return Paged request specification.
     * @throws JSONRPC2Error On a missing parameter, bad type or null value (JSONRPC2Error.INVALID_PARAMS).
     */
    public Pageable getPageable(int defaultLimit)
        throws
            JSONRPC2Error
    {
        return this.getPageable(
            ParamsRetriever.DEFAULTPARAM_PAGE,
            ParamsRetriever.DEFAULTPARAM_LIMIT,
            ParamsRetriever.DEFAULTPARAM_SORT,
            defaultLimit
        );
    }

    /**
     * Retrieves typed bean from current parameters.
     *
     * @param type Destination bean class.
     * @param <Type> Destination bean type.
     * @return Populated bean.
     */
    public <Type> Type getBean(Class<Type> type)
    {
        return ParamsRetriever.OBJECT_MAPPER.convertValue(this.getParams(), type);
    }

    /**
     * Retrieves typed bean from sub-map located under specified entry.
     *
     * @param param Property map parameter name.
     * @param type Destination bean class.
     * @param allowNull Whether NULL should be accepted as bean value.
     * @param <Type> Destination bean type.
     * @return Populated bean.
     * @throws JSONRPC2Error When parameters reading fails.
     */
    public <Type> Type getBean(String param, Class<Type> type, boolean allowNull)
        throws
            JSONRPC2Error
    {
        Map<String, Object> data = this.getMap(param, allowNull);

        if (data == null) {
            return null;
        }

        return new ParamsRetriever(data).getBean(type);
    }

    /**
     * Retrieves typed bean from sub-map located under specified entry.
     *
     * @param param Property map parameter name.
     * @param type Destination bean class.
     * @param <Type> Destination bean type.
     * @return Populated bean.
     * @throws JSONRPC2Error When parameters reading fails.
     */
    public <Type> Type getBean(String param, Class<Type> type)
        throws
            JSONRPC2Error
    {
        return this.getBean(param, type, false);
    }
}
