/*
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.client.codec;

import java.lang.reflect.Type;

import feign.RequestTemplate;
import feign.codec.Encoder;
import org.springframework.data.domain.Pageable;
import pl.chilldev.commons.data.ConvertUtils;

/**
 * Spring Data page specification handling in requests.
 */
public class SpringDataEncoder implements Encoder
{
    /**
     * Default parameter name for page number.
     */
    private static final String DEFAULT_PARAM_PAGE = "page";

    /**
     * Default parameter name for page size.
     */
    private static final String DEFAULT_PARAM_SIZE = "size";

    /**
     * Default parameter name for sorting criteria.
     */
    private static final String DEFAULT_PARAM_SORT = "sort";

    /**
     * Delegate encoder.
     */
    private Encoder fallback;

    /**
     * Page number parameter name.
     */
    private String paramPage;

    /**
     * Page size parameter name.
     */
    private String paramSize;

    /**
     * Sorting criteria parameter name.
     */
    private String paramSort;

    /**
     * Initializes encoder to put Spring Data's `Pageable` as query string parameters in request.
     *
     * @param fallback Fallback encoder.
     * @param paramPage URL parameter name for page number.
     * @param paramSize URL parameter name for page size.
     * @param paramSort URL parameter name for sorting criteria.
     */
    public SpringDataEncoder(Encoder fallback, String paramPage, String paramSize, String paramSort)
    {
        this.fallback = fallback;
        this.paramPage = paramPage;
        this.paramSize = paramSize;
        this.paramSort = paramSort;
    }

    /**
     * Initializes object with fallback encoder.
     *
     * <p>
     *     This variant uses default parameters names ("page", "size" and "sort").
     * </p>
     *
     * @param fallback Fallback encoder.
     */
    public SpringDataEncoder(Encoder fallback)
    {
        this(
            fallback,
            SpringDataEncoder.DEFAULT_PARAM_PAGE,
            SpringDataEncoder.DEFAULT_PARAM_SIZE,
            SpringDataEncoder.DEFAULT_PARAM_SORT
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template)
    {
        if (object instanceof Pageable) {
            Pageable request = (Pageable) object;

            template
                .query(this.paramPage, String.valueOf(request.getPageNumber()))
                .query(this.paramSize, String.valueOf(request.getPageSize()))
                .query(this.paramSort, ConvertUtils.extractSort(request.getSort()));
        } else {
            this.fallback.encode(object, bodyType, template);
        }
    }
}
