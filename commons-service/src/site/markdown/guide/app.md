<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2015 - 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# Service application

This package is mainly oriented in exposing common architecture of network service application used by **Chillout Development**. It's a **Spring** configuration handling library that can be used together with `commons-jsonrpc` to build full network RPC service with less effort.

## `BaseApplicationConfiguration`

`pl.chilldev.commons.service.config.BaseApplicationConfiguration` is a Spring Java-config class that provides all services neccessary for building transactional JSON-RPC service:

-   `introspector` for JSON-RPC service building;
-   `validator` for validating data;
-   `transactionAttributeSource` for handling `@Transactional` annotations.

## Spring configuration handling

Usually application needs to customize mainly network and database connectivity. For that there are two abstract bean definition handlers which allow to handle generic configuration in a format:

```xml
<?xml version="1.0" encoding="utf-8"?>
<beans
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:sites="http://chilldev.pl/java/spring/sites"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
        http://chilldev.pl/java/spring/sites http://chilldev.pl/schema/spring/chilldev-sites-0.0.5.xsd
">
    <sites:data-source
        name="jpa0"
        jdbcUrl="jdbc:mysql://localhost/database?autoReconnect=true&amp;useUnicode=true&amp;characterEncoding=utf-8"
        user="root"
        password="secret"
        />
    <sites:listener
        name="chilldev.pl"
        port="6000"
        dataSource="jpa0"
        />
</beans>
```

**Note:** `sites` is a sample namespace for custom configuration, but all the configuration structure is the same, handled by generic classes from `commons-service` library.

### Usage

To use this configuration stub in your application, you need to subclass these abstract handlers probviding missing pieces about how to bind your implementation to these beans (note that you are free to define any element names, class names are not related to element names).

For the data source connection, all that you have to do is to specify source packages for your models to be bound into data source entity manager:

```java
public class DataSourceBeanDefinitionParser
    extends AbstractDataSourceBeanDefinitionParser
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getPackagesToScan()
    {
        return Collections.singletonList("pl.chilldev.sites.service");
    }
}
```

For the listener you need to do two things - provide factory class that will produce listeners based on given API facades and provide it's backing implementation bean:

```java
public class ListenerBeanDefinitionParser
    extends AbstractListenerBeanDefinitionParser
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> getFactoryClass()
    {
        // this class should return listener for your API facades
        return ApiFacadeFactory.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BeanDefinition createApiFacadeBeanDefinition(String name)
    {
        // define your API facade bean and all it's dependencies

        GenericBeanDefinition apiFacadeBean = new GenericBeanDefinition();
        apiFacadeBean.setBeanClass(ApiFacadeImpl.class);
        apiFacadeBean.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);

        // required repositories
        ConstructorArgumentValues arguments = apiFacadeBean.getConstructorArgumentValues();
        arguments.addGenericArgumentValue(
            new RuntimeBeanReference(
                SiteRepository.class.getName() + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME + name
            )
        );
        arguments.addGenericArgumentValue(
            new RuntimeBeanReference(
                TagRepository.class.getName() + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME + name
            )
        );

        return apiFacadeBean;
    }
}
```

Usually, the factory class can be very simple and is mostly used to delegate facade-dispatcher binding:

```java
public class ApiFacadeFactory
{
    /**
     * Builds listener for API.
     *
     * @param name Listener name.
     * @param facade Data models facade.
     * @param dispatcher RPC dispatcher.
     * @return API listener.
     */
    public static Listener<ApiFacade> createListener(
        String name,
        ApiFacade facade,
        Dispatcher<ApiFacade> dispatcher
    )
    {
        return new Listener<>(
            name,
            facade,
            dispatcher
        );
    }
}
```
