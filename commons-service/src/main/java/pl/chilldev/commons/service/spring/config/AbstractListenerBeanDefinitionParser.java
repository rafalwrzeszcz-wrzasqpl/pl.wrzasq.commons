/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.service.spring.config;

import java.net.InetSocketAddress;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;

import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import org.w3c.dom.Element;

/**
 * Handler for listener beans.
 */
public abstract class AbstractListenerBeanDefinitionParser
    implements BeanDefinitionParser
{
    /**
     * `name=""` attribute.
     */
    private static final String ATTRIBUTE_NAME = "name";

    /**
     * `host=""` attribute.
     */
    private static final String ATTRIBUTE_HOST = "host";

    /**
     * `port=""` attribute.
     */
    private static final String ATTRIBUTE_PORT = "port";

    /**
     * `maxPacketSize=""` attribute.
     */
    private static final String ATTRIBUTE_MAXPACKETSIZE = "maxPacketSize";

    /**
     * `dataSource=""` attribute.
     */
    private static final String ATTRIBUTE_DATASOURCE = "dataSource";

    /**
     * `address` property name.
     */
    private static final String PROPERTY_ADDRESS = "address";

    /**
     * `target` property name.
     */
    private static final String PROPERTY_TARGET = "target";

    /**
     * `transactionManager` property name.
     */
    private static final String PROPERTY_TRANSACTIONMANAGER = "transactionManager";

    /**
     * `transactionAttributeSource` property name.
     */
    private static final String PROPERTY_TRANSACTIONATTRIBUTESOURCE = "transactionAttributeSource";

    /**
     * `createListener()` factory method name.
     */
    private static final String METHOD_CREATELISTENER = "createListener";

    /**
     * {@inheritDoc}
     */
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        Class<?> beanClass = this.getFactoryClass();
        String name = element.getAttribute(AbstractListenerBeanDefinitionParser.ATTRIBUTE_NAME);

        // main bean setup
        GenericBeanDefinition listenerBean = new GenericBeanDefinition();
        listenerBean.setBeanClass(beanClass);
        listenerBean.setFactoryMethodName(AbstractListenerBeanDefinitionParser.METHOD_CREATELISTENER);
        listenerBean.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);

        ConstructorArgumentValues arguments = listenerBean.getConstructorArgumentValues();
        MutablePropertyValues properties = listenerBean.getPropertyValues();

        // constructor arguments
        arguments.addGenericArgumentValue(name);
        arguments.addGenericArgumentValue(
            this.createApiFacadeProxyBeanDefinition(
                element.getAttribute(AbstractListenerBeanDefinitionParser.ATTRIBUTE_DATASOURCE)
            )
        );

        // required properties
        properties.addPropertyValue(
            AbstractListenerBeanDefinitionParser.PROPERTY_ADDRESS,
            this.createAddressBeanDefinition(element)
        );

        // optional properties
        if (element.hasAttribute(AbstractListenerBeanDefinitionParser.ATTRIBUTE_MAXPACKETSIZE)) {
            properties.addPropertyValue(
                AbstractListenerBeanDefinitionParser.ATTRIBUTE_MAXPACKETSIZE,
                element.getAttribute(AbstractListenerBeanDefinitionParser.ATTRIBUTE_MAXPACKETSIZE)
            );
        }

        // register bean
        parserContext.getRegistry().registerBeanDefinition(
            beanClass.getName() + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME + name,
            listenerBean
        );

        return null;
    }

    /**
     * Creates socket address bean definition.
     *
     * @param element Element with address configuration.
     * @return Socket address definition.
     */
    private BeanDefinition createAddressBeanDefinition(Element element)
    {
        GenericBeanDefinition addressBean = new GenericBeanDefinition();
        addressBean.setBeanClass(InetSocketAddress.class);

        // set up address initialization
        ConstructorArgumentValues arguments = addressBean.getConstructorArgumentValues();
        arguments.addGenericArgumentValue(
            element.getAttribute(AbstractListenerBeanDefinitionParser.ATTRIBUTE_HOST),
            "java.lang.String"
        );
        arguments.addGenericArgumentValue(
            element.getAttribute(AbstractListenerBeanDefinitionParser.ATTRIBUTE_PORT),
            "int"
        );

        return addressBean;
    }

    /**
     * Creates transactional proxy bean definition for API facade.
     *
     * @param name Data source scope identifier.
     * @return API facade proxy bean.
     */
    private BeanDefinition createApiFacadeProxyBeanDefinition(String name)
    {
        GenericBeanDefinition proxyBean = new GenericBeanDefinition();
        proxyBean.setBeanClass(TransactionProxyFactoryBean.class);

        MutablePropertyValues properties = proxyBean.getPropertyValues();

        // original API facade
        properties.addPropertyValue(
            AbstractListenerBeanDefinitionParser.PROPERTY_TARGET,
            this.createApiFacadeBeanDefinition(name)
        );

        properties.addPropertyValue(
            AbstractListenerBeanDefinitionParser.PROPERTY_TRANSACTIONMANAGER,
            new RuntimeBeanReference(
                AbstractDataSourceBeanDefinitionParser.BEANNAME_TRANSACTIONMANAGER
                    + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                    + name
            )
        );
        properties.addPropertyValue(
            AbstractListenerBeanDefinitionParser.PROPERTY_TRANSACTIONATTRIBUTESOURCE,
            new RuntimeBeanReference(AbstractListenerBeanDefinitionParser.PROPERTY_TRANSACTIONATTRIBUTESOURCE)
        );

        return proxyBean;
    }

    /**
     * Creates API facade bean definition.
     *
     * @param name Data source scope identifier.
     * @return API facade bean.
     */
    protected abstract BeanDefinition createApiFacadeBeanDefinition(String name);

    /**
     * Provides factory class for bean definition.
     *
     * @return Factory class.
     */
    protected abstract Class<?> getFactoryClass();
}
