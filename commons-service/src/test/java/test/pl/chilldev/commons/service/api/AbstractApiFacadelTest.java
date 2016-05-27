/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.service.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import pl.chilldev.commons.service.api.AbstractApiFacade;

@RunWith(MockitoJUnitRunner.class)
public class AbstractApiFacadelTest
{
    class TestApiFacade extends AbstractApiFacade
    {
        public TestApiFacade(Validator validator)
        {
            super(validator);
        }
    }

    @Mock
    private Validator validator;

    @Test
    public void mapList()
    {
        Collection<Integer> input = new ArrayList<>();
        input.add(12);

        List<String> output = new ArrayList<>();
        output.add("14");

        Assert.assertEquals(
            "AbstractApiFacade.mapList() should map objects collection to another type.",
            output,
            AbstractApiFacade.mapList(input, Integer::toOctalString)
        );
    }

    @Test
    public void validateEntity() throws JSONRPC2Error
    {
        Object entity = new Object();

        Mockito.when(this.validator.validate(entity)).thenReturn(Collections.emptySet());

        AbstractApiFacade facade = new AbstractApiFacadelTest.TestApiFacade(this.validator);
        facade.validateEntity(entity);
    }

    @Test(expected = JSONRPC2Error.class)
    public void validateEntityFailed() throws JSONRPC2Error
    {
        Set<ConstraintViolation<Object>> errors = new HashSet<>();
        errors.add(new ConstraintViolation<Object>()
        {
            @Override
            public String getMessage()
            {
                return null;
            }

            @Override
            public String getMessageTemplate()
            {
                return null;
            }

            @Override
            public Object getRootBean()
            {
                return null;
            }

            @Override
            public Class<Object> getRootBeanClass()
            {
                return null;
            }

            @Override
            public Object getLeafBean()
            {
                return null;
            }

            @Override
            public Object[] getExecutableParameters()
            {
                return new Object[0];
            }

            @Override
            public Object getExecutableReturnValue()
            {
                return null;
            }

            @Override
            public Path getPropertyPath()
            {
                return null;
            }

            @Override
            public Object getInvalidValue()
            {
                return null;
            }

            @Override
            public ConstraintDescriptor<?> getConstraintDescriptor()
            {
                return null;
            }

            @Override
            public <U> U unwrap(Class<U> type)
            {
                return null;
            }
        });

        Object entity = new Object();

        Mockito.when(this.validator.validate(entity)).thenReturn(errors);

        AbstractApiFacade facade = new AbstractApiFacadelTest.TestApiFacade(this.validator);
        facade.validateEntity(entity);
    }
}
