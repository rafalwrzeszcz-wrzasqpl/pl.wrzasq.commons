/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.service.spring.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlReaderContext;

import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import org.w3c.dom.Element;

import pl.chilldev.commons.service.spring.config.AbstractListenerBeanDefinitionParser;

@RunWith(MockitoJUnitRunner.class)
public class AbstractListenerBeanDefinitionParserTest
{
    private class ListenerBeanDefinitionParser extends AbstractListenerBeanDefinitionParser
    {
        private BeanDefinition apiBean;

        private ListenerBeanDefinitionParser(BeanDefinition apiBean)
        {
            this.apiBean = apiBean;
        }

        @Override
        protected BeanDefinition createApiFacadeBeanDefinition(String name)
        {
            return this.apiBean;
        }

        @Override
        protected Class<?> getFactoryClass()
        {
            return AbstractListenerBeanDefinitionParserTest.class;
        }
    }

    @Mock
    private Element element;

    @Mock
    private BeanDefinitionRegistry registry;

    @Mock
    private BeanDefinition apiBean;

    @Captor
    private ArgumentCaptor<BeanDefinition> captor;

    private XmlReaderContext readerContext;

    @Before
    public void setUp()
    {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.registry);
        this.readerContext = new XmlReaderContext(null, null, null, null, reader, null);
    }

    @Test
    public void parse()
    {
        AbstractListenerBeanDefinitionParser parser
            = new AbstractListenerBeanDefinitionParserTest.ListenerBeanDefinitionParser(this.apiBean);

        // prepare attributes
        Mockito.when(this.element.getAttribute("name")).thenReturn("foo");
        Mockito.when(this.element.getAttribute("host")).thenReturn("127.0.0.1");
        Mockito.when(this.element.getAttribute("port")).thenReturn("1234");
        Mockito.when(this.element.hasAttribute("maxPacketSize")).thenReturn(false);
        Mockito.when(this.element.getAttribute("dataSource")).thenReturn("db");

        parser.parse(this.element, new ParserContext(this.readerContext, null));
        Mockito.verify(this.registry).registerBeanDefinition(
            Matchers.eq(AbstractListenerBeanDefinitionParserTest.class.getName() + ":foo"),
            this.captor.capture()
        );

        BeanDefinition listenerBean = this.captor.getValue();
        ConstructorArgumentValues arguments = listenerBean.getConstructorArgumentValues();
        MutablePropertyValues properties = listenerBean.getPropertyValues();
        BeanDefinition addressBean = (BeanDefinition) properties.getPropertyValue("address").getValue();

        // bean parameters assertions
        Assert.assertEquals(
            "ListenerBeanDefinitionParser.parse() should set bean class to ApiFacadeFactory.",
            AbstractListenerBeanDefinitionParserTest.class.getName(),
            listenerBean.getBeanClassName()
        );
        Assert.assertEquals(
            "ListenerBeanDefinitionParser.parse() should set bean factory method.",
            "createListener",
            listenerBean.getFactoryMethodName()
        );
        Assert.assertEquals(
            "ListenerBeanDefinitionParser.parse() should set constructor argument for listener name.",
            "foo",
            arguments.getGenericArgumentValue(String.class).getValue()
        );
        Assert.assertEquals(
            "ListenerBeanDefinitionParser.parse() should create API facade bean to expose data for listener calls.",
            TransactionProxyFactoryBean.class.getName(),
            ((BeanDefinition) arguments.getGenericArgumentValue(BeanDefinition.class).getValue()).getBeanClassName()
        );
        Assert.assertEquals(
            "ListenerBeanDefinitionParser.parse() should set listen host.",
            "127.0.0.1",
            addressBean.getConstructorArgumentValues().getGenericArgumentValue(String.class).getValue()
        );
        Assert.assertEquals(
            "ListenerBeanDefinitionParser.parse() should set listen port.",
            "1234",
            addressBean.getConstructorArgumentValues().getGenericArgumentValues().get(1).getValue()
        );
        Assert.assertNull(
            "ListenerBeanDefinitionParser.parse() should not set maxPacketSize property if it was not configured.",
            properties.get("maxPacketSize")
        );
    }

    @Test
    public void parse_withMaxPacketSize()
    {
        AbstractListenerBeanDefinitionParser parser
            = new AbstractListenerBeanDefinitionParserTest.ListenerBeanDefinitionParser(this.apiBean);

        // prepare attributes
        Mockito.when(this.element.getAttribute("name")).thenReturn("foo");
        Mockito.when(this.element.getAttribute("host")).thenReturn("127.0.0.1");
        Mockito.when(this.element.getAttribute("port")).thenReturn("1234");
        Mockito.when(this.element.hasAttribute("maxPacketSize")).thenReturn(true);
        Mockito.when(this.element.getAttribute("maxPacketSize")).thenReturn("1024");
        Mockito.when(this.element.getAttribute("dataSource")).thenReturn("db");

        parser.parse(this.element, new ParserContext(this.readerContext, null));
        Mockito.verify(this.registry).registerBeanDefinition(
            Matchers.eq(AbstractListenerBeanDefinitionParserTest.class.getName() + ":foo"),
            this.captor.capture()
        );

        MutablePropertyValues properties = this.captor.getValue().getPropertyValues();

        // bean parameters assertions
        Assert.assertEquals(
            "ListenerBeanDefinitionParser.parse() should set maxPacketSize property if it was configured.",
            "1024",
            properties.get("maxPacketSize")
        );
    }
}
