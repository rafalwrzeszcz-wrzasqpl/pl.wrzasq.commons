/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package pl.chilldev.commons.service.spring.config;

import java.util.List;
import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import org.w3c.dom.Element;

/**
 * Data source (entire set of beans related to entities and transactions as well) definition handler.
 */
public abstract class AbstractDataSourceBeanDefinitionParser
    implements BeanDefinitionParser
{
    /**
     * Transaction manager bean name prefix.
     */
    public static final String BEANNAME_TRANSACTIONMANAGER = "transactionManager";

    /**
     * Bean name separator.
     */
    public static final String SEPARATOR_BEANNAME = ":";

    /**
     * `name=""` attribute.
     */
    protected static final String ATTRIBUTE_NAME = "name";

    /**
     * `driverClass=""` attribute.
     */
    private static final String ATTRIBUTE_DRIVERCLASS = "driverClass";

    /**
     * `jdbcUrl=""` attribute.
     */
    private static final String ATTRIBUTE_JDBCURL = "jdbcUrl";

    /**
     * `user=""` attribute.
     */
    private static final String ATTRIBUTE_USER = "user";

    /**
     * `password=""` attribute.
     */
    private static final String ATTRIBUTE_PASSWORD = "password";

    /**
     * `acquireIncrement=""` attribute.
     */
    private static final String ATTRIBUTE_ACQUIREINCREMENT = "acquireIncrement";

    /**
     * `minPoolSize=""` attribute.
     */
    private static final String ATTRIBUTE_MINPOOLSIZE = "minPoolSize";

    /**
     * `maxPoolSize=""` attribute.
     */
    private static final String ATTRIBUTE_MAXPOOLSIZE = "maxPoolSize";

    /**
     * `maxIdleTime=""` attribute.
     */
    private static final String ATTRIBUTE_MAXIDLETIME = "maxIdleTime";

    /**
     * `initialPoolSize=""` attribute.
     */
    private static final String ATTRIBUTE_INITIALPOOLSIZE = "initialPoolSize";

    /**
     * `hibernateDialect=""` attribute.
     */
    private static final String ATTRIBUTE_HIBERNATEDIALECT = "hibernateDialect";

    /**
     * `hibernateJdbcBatchSize=""` attribute.
     */
    private static final String ATTRIBUTE_HIBERNATEJDBCBATCHSIZE = "hibernateJdbcBatchSize";

    //CHECKSTYLE:OFF: MultipleStringLiteralsCheck
    /**
     * Data source bean name prefix.
     */
    private static final String BEANNAME_DATASOURCE = "dataSource";
    //CHECKSTYLE:ON: MultipleStringLiteralsCheck

    /**
     * Entity manager factory bean name prefix.
     */
    private static final String BEANNAME_ENTITYMANAGERFACTORY = "entityManagerFactory";

    /**
     * Entity manager bean name prefix.
     */
    private static final String BEANNAME_ENTITYMANAGER = "entityManager";

    /**
     * Repository factory bean name prefix.
     */
    private static final String BEANNAME_REPOSITORYFACTORY = "repositoryFactory";

    /**
     * `createSharedEntityManager()` factory method name.
     */
    private static final String METHOD_CREATESHAREDENTITYMANAGER = "createSharedEntityManager";

    /**
     * `getRepository()` factory method name.
     */
    private static final String METHOD_GETREPOSITORY = "getRepository";

    /**
     * `persistenceUnitName` property name.
     */
    private static final String PROPERTY_PERSISTENCEUNITNAME = "persistenceUnitName";

    /**
     * `dataSource` property name.
     */
    //CHECKSTYLE:OFF: MultipleStringLiteralsCheck
    private static final String PROPERTY_DATASOURCE = "dataSource";
    //CHECKSTYLE:ON: MultipleStringLiteralsCheck

    /**
     * `packagesToScan` property name.
     */
    private static final String PROPERTY_PACKAGESTOSCAN = "packagesToScan";

    /**
     * `jpaVendorAdapter` property name.
     */
    private static final String PROPERTY_JPAVENDORADAPTER = "jpaVendorAdapter";

    /**
     * `hibernate.hbm2ddl.auto` property name.
     */
    private static final String PROPERTY_HIBERNATEHBM2DDLAUTO = "hibernate.hbm2ddl.auto";

    /**
     * `hibernate.dialect` property name.
     */
    private static final String PROPERTY_HIBERNATEDIALECT = "hibernate.dialect";

    /**
     * `hibernate.jdbc.batch_size` property name.
     */
    private static final String PROPERTY_HIBERNATEJDBCBATCHSIZE = "hibernate.jdbc.batch_size";

    /**
     * `jpaProperties` property name.
     */
    private static final String PROPERTY_JPAPROPERTIES = "jpaProperties";

    /**
     * {@inheritDoc}
     */
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        String name = element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_NAME);
        BeanDefinitionRegistry registry = parserContext.getRegistry();

        // build all beans
        registry.registerBeanDefinition(
            AbstractDataSourceBeanDefinitionParser.BEANNAME_DATASOURCE
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name,
            this.createDataSourceBeanDefinition(element)
        );
        registry.registerBeanDefinition(
            AbstractDataSourceBeanDefinitionParser.BEANNAME_ENTITYMANAGERFACTORY
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name,
            this.createEntityManagerFactoryBeanDefinition(name, element)
        );
        registry.registerBeanDefinition(
            AbstractDataSourceBeanDefinitionParser.BEANNAME_ENTITYMANAGER
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name,
            this.createEntityManagerBeanDefinition(name)
        );
        registry.registerBeanDefinition(
            AbstractDataSourceBeanDefinitionParser.BEANNAME_REPOSITORYFACTORY
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name,
            this.createRepositoryFactoryBeanDefinition(name)
        );
        registry.registerBeanDefinition(
            AbstractDataSourceBeanDefinitionParser.BEANNAME_TRANSACTIONMANAGER
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name,
            this.createTransactionManagerBeanDefinition(name)
        );

        return null;
    }

    /**
     * Builds data source bean.
     *
     * @param element Configuration options parsed from XML.
     * @return Bean definition.
     */
    private BeanDefinition createDataSourceBeanDefinition(Element element)
    {
        // JDBC connection pool with C3P0
        GenericBeanDefinition bean = new GenericBeanDefinition();
        bean.setBeanClass(ComboPooledDataSource.class);

        MutablePropertyValues properties = bean.getPropertyValues();
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_DRIVERCLASS,
            element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_DRIVERCLASS)
        );
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_JDBCURL,
            element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_JDBCURL)
        );
        if (element.hasAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_USER)) {
            properties.addPropertyValue(
                AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_USER,
                element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_USER)
            );
        }
        if (element.hasAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_PASSWORD)) {
            properties.addPropertyValue(
                AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_PASSWORD,
                element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_PASSWORD)
            );
        }
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_ACQUIREINCREMENT,
            element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_ACQUIREINCREMENT)
        );
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_MINPOOLSIZE,
            element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_MINPOOLSIZE)
        );
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_MAXPOOLSIZE,
            element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_MAXPOOLSIZE)
        );
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_MAXIDLETIME,
            element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_MAXIDLETIME)
        );
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_INITIALPOOLSIZE,
            element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_INITIALPOOLSIZE)
        );

        return bean;
    }

    /**
     * Builds entity manager factory bean.
     *
     * @param name Current scope name.
     * @param element Configuration options parsed from XML.
     * @return Bean definition.
     */
    private BeanDefinition createEntityManagerFactoryBeanDefinition(String name, Element element)
    {
        // Hibernate ORM
        GenericBeanDefinition bean = new GenericBeanDefinition();
        bean.setBeanClass(LocalContainerEntityManagerFactoryBean.class);

        MutablePropertyValues properties = bean.getPropertyValues();
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.PROPERTY_PERSISTENCEUNITNAME,
            name
        );
        properties.addPropertyValue(
            //CHECKSTYLE:OFF: MultipleStringLiteralsCheck
            AbstractDataSourceBeanDefinitionParser.PROPERTY_DATASOURCE,
            //CHECKSTYLE:ON: MultipleStringLiteralsCheck
            new RuntimeBeanReference(AbstractDataSourceBeanDefinitionParser.BEANNAME_DATASOURCE
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name)
        );

        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.PROPERTY_PACKAGESTOSCAN,
            this.getPackagesToScan()
        );

        // build vendor adapter bean
        GenericBeanDefinition jpaVendorAdapterBean = new GenericBeanDefinition();
        jpaVendorAdapterBean.setBeanClass(HibernateJpaVendorAdapter.class);
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.PROPERTY_JPAVENDORADAPTER,
            jpaVendorAdapterBean
        );

        // JPA properties
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty(
            AbstractDataSourceBeanDefinitionParser.PROPERTY_HIBERNATEHBM2DDLAUTO,
            "update" //TODO: will probably change
        );
        jpaProperties.setProperty(
            AbstractDataSourceBeanDefinitionParser.PROPERTY_HIBERNATEDIALECT,
            element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_HIBERNATEDIALECT)
        );
        jpaProperties.setProperty(
            AbstractDataSourceBeanDefinitionParser.PROPERTY_HIBERNATEJDBCBATCHSIZE,
            element.getAttribute(AbstractDataSourceBeanDefinitionParser.ATTRIBUTE_HIBERNATEJDBCBATCHSIZE)
        );
        properties.addPropertyValue(
            AbstractDataSourceBeanDefinitionParser.PROPERTY_JPAPROPERTIES,
            jpaProperties
        );

        return bean;
    }

    /**
     * Builds entity manager bean.
     *
     * @param name Current scope name.
     * @return Bean definition.
     */
    private BeanDefinition createEntityManagerBeanDefinition(String name)
    {
        // entity manager used directly in the code
        GenericBeanDefinition bean = new GenericBeanDefinition();
        bean.setBeanClass(SharedEntityManagerCreator.class);
        bean.setFactoryMethodName(AbstractDataSourceBeanDefinitionParser.METHOD_CREATESHAREDENTITYMANAGER);
        bean.getConstructorArgumentValues().addGenericArgumentValue(
            new RuntimeBeanReference(AbstractDataSourceBeanDefinitionParser.BEANNAME_ENTITYMANAGERFACTORY
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name)
        );

        return bean;
    }

    /**
     * Builds repository factory bean.
     *
     * @param name Current scope name.
     * @return Bean definition.
     */
    private BeanDefinition createRepositoryFactoryBeanDefinition(String name)
    {
        // for by-hand JPA repositories creation
        GenericBeanDefinition bean = new GenericBeanDefinition();
        bean.setBeanClass(JpaRepositoryFactory.class);
        bean.getConstructorArgumentValues().addGenericArgumentValue(
            new RuntimeBeanReference(AbstractDataSourceBeanDefinitionParser.BEANNAME_ENTITYMANAGER
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name)
        );

        return bean;
    }

    /**
     * Builds transaction manager bean.
     *
     * @param name Current scope name.
     * @return Bean definition.
     */
    private BeanDefinition createTransactionManagerBeanDefinition(String name)
    {
        // JPA transactions manager
        GenericBeanDefinition bean = new GenericBeanDefinition();
        bean.setBeanClass(JpaTransactionManager.class);
        bean.getConstructorArgumentValues().addGenericArgumentValue(
            new RuntimeBeanReference(AbstractDataSourceBeanDefinitionParser.BEANNAME_ENTITYMANAGERFACTORY
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name)
        );

        return bean;
    }

    /**
     * Builds typed repository bean.
     *
     * @param name Current scope name.
     * @param type Repository type.
     * @return Bean definition.
     */
    protected BeanDefinition createRepositoryBeanDefinition(String name, Class<?> type)
    {
        // entity repository type
        GenericBeanDefinition bean = new GenericBeanDefinition();
        bean.setFactoryBeanName(
            AbstractDataSourceBeanDefinitionParser.BEANNAME_REPOSITORYFACTORY
                + AbstractDataSourceBeanDefinitionParser.SEPARATOR_BEANNAME
                + name
        );
        bean.setFactoryMethodName(AbstractDataSourceBeanDefinitionParser.METHOD_GETREPOSITORY);
        bean.getConstructorArgumentValues().addGenericArgumentValue(type);

        return bean;
    }

    /**
     * Returns list of Java packages to scan for Hibernate/JPA annotated entities.
     *
     * @return List of package names.
     */
    protected abstract List<String> getPackagesToScan();
}
