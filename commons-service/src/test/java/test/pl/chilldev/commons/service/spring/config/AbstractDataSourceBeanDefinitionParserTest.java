/**
 * This file is part of the ChillDev-Commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2016 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.chilldev.commons.service.spring.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;

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
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlReaderContext;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import org.w3c.dom.Element;

import pl.chilldev.commons.service.spring.config.AbstractDataSourceBeanDefinitionParser;

@RunWith(MockitoJUnitRunner.class)
public class AbstractDataSourceBeanDefinitionParserTest
{
    private class DataSourceBeanDefinitionParser extends AbstractDataSourceBeanDefinitionParser
    {
        @Override
        protected List<String> getPackagesToScan()
        {
            return Collections.singletonList("pl.chilldev.commons");
        }
    
        @Override
        public BeanDefinition createRepositoryBeanDefinition(String name, Class<?> type)
        {
            return super.createRepositoryBeanDefinition(name, type);
        }
    }
    
    @Mock
    private Element element;

    @Mock
    private BeanDefinitionRegistry registry;

    @Captor
    private ArgumentCaptor<BeanDefinition> dataSourceCaptor;

    @Captor
    private ArgumentCaptor<BeanDefinition> entityManagerFactoryCaptor;

    @Captor
    private ArgumentCaptor<BeanDefinition> entityManagerCaptor;

    @Captor
    private ArgumentCaptor<BeanDefinition> repositoryFactoryCaptor;

    @Captor
    private ArgumentCaptor<BeanDefinition> transactionManagerCaptor;

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
        AbstractDataSourceBeanDefinitionParser parser
            = new AbstractDataSourceBeanDefinitionParserTest.DataSourceBeanDefinitionParser();

        // prepare attributes
        Mockito.when(this.element.getAttribute("name")).thenReturn("foo");
        Mockito.when(this.element.getAttribute("driverClass")).thenReturn("com.mysql.jdbc.Driver");
        Mockito.when(this.element.getAttribute("jdbcUrl")).thenReturn("jdbc:mysql://localhost/test");
        Mockito.when(this.element.hasAttribute("user")).thenReturn(false);
        Mockito.when(this.element.hasAttribute("password")).thenReturn(false);
        Mockito.when(this.element.getAttribute("acquireIncrement")).thenReturn("2");
        Mockito.when(this.element.getAttribute("minPoolSize")).thenReturn("5");
        Mockito.when(this.element.getAttribute("maxPoolSize")).thenReturn("100");
        Mockito.when(this.element.getAttribute("maxIdleTime")).thenReturn("30");
        Mockito.when(this.element.getAttribute("initialPoolSize")).thenReturn("10");
        Mockito.when(this.element.getAttribute("hibernateDialect")).thenReturn("org.hibernate.dialect.MySQL5InnoDBDialect");
        Mockito.when(this.element.getAttribute("hibernateJdbcBatchSize")).thenReturn("500");

        parser.parse(this.element, new ParserContext(this.readerContext, null));
        Mockito.verify(this.registry).registerBeanDefinition(
            Matchers.eq("dataSource:foo"),
            this.dataSourceCaptor.capture()
        );
        Mockito.verify(this.registry).registerBeanDefinition(
            Matchers.eq("entityManagerFactory:foo"),
            this.entityManagerFactoryCaptor.capture()
        );
        Mockito.verify(this.registry).registerBeanDefinition(
            Matchers.eq("entityManager:foo"),
            this.entityManagerCaptor.capture()
        );
        Mockito.verify(this.registry).registerBeanDefinition(
            Matchers.eq("repositoryFactory:foo"),
            this.repositoryFactoryCaptor.capture()
        );
        Mockito.verify(this.registry).registerBeanDefinition(
            Matchers.eq("transactionManager:foo"),
            this.transactionManagerCaptor.capture()
        );

        BeanDefinition dataSourceBean = this.dataSourceCaptor.getValue();
        BeanDefinition entityManagerFactoryBean = this.entityManagerFactoryCaptor.getValue();
        BeanDefinition entityManagerBean = this.entityManagerCaptor.getValue();
        BeanDefinition repositoryFactoryBean = this.repositoryFactoryCaptor.getValue();
        BeanDefinition transactionManagerBean = this.transactionManagerCaptor.getValue();

        Object value;
        MutablePropertyValues properties;

        properties = dataSourceBean.getPropertyValues();
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should create data source bean.",
            ComboPooledDataSource.class.getName(),
            dataSourceBean.getBeanClassName()
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set data source driver class.",
            "com.mysql.jdbc.Driver",
            properties.get("driverClass")
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set data source JDBC URL.",
            "jdbc:mysql://localhost/test",
            properties.get("jdbcUrl")
        );
        Assert.assertNull(
            "AbstractDataSourceBeanDefinitionParser.parse() should not set data source access login if not specified.",
            properties.get("user")
        );
        Assert.assertNull(
            "AbstractDataSourceBeanDefinitionParser.parse() should not set data source access password if not specified.",
            properties.get("password")
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set data source pool acquireIncrement property.",
            "2",
            properties.get("acquireIncrement")
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set data source pool minPoolSize property.",
            "5",
            properties.get("minPoolSize")
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set data source pool maxPoolSize property.",
            "100",
            properties.get("maxPoolSize")
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set data source pool maxIdleTime property.",
            "30",
            properties.get("maxIdleTime")
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set data source pool initialPoolSize property.",
            "10",
            properties.get("initialPoolSize")
        );

        properties = entityManagerFactoryBean.getPropertyValues();
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should create entity manager factory bean.",
            LocalContainerEntityManagerFactoryBean.class.getName(),
            entityManagerFactoryBean.getBeanClassName()
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set persistenceUnitName property based on configuration.",
            "foo",
            properties.get("persistenceUnitName")
        );
        Assert.assertTrue(
            "AbstractDataSourceBeanDefinitionParser.parse() should set entity manager factory based on data source.",
            properties.get("dataSource") instanceof RuntimeBeanReference
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set entity manager factory based on data source.",
            "dataSource:foo",
            ((RuntimeBeanReference) properties.get("dataSource")).getBeanName()
        );

        List<String> packagesToScan = new ArrayList<>();
        packagesToScan.add("pl.chilldev.commons");
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set project core package for annotations scanning.",
            packagesToScan,
            properties.get("packagesToScan")
        );

        Assert.assertTrue(
            "AbstractDataSourceBeanDefinitionParser.parse() should set JPA vendor adapter.",
            properties.get("jpaVendorAdapter") instanceof BeanDefinition
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set JPA vendor adapter.",
            HibernateJpaVendorAdapter.class.getName(),
            ((BeanDefinition) properties.get("jpaVendorAdapter")).getBeanClassName()
        );

        Assert.assertTrue(
            "AbstractDataSourceBeanDefinitionParser.parse() should set JPA properties.",
            properties.get("jpaProperties") instanceof Properties
        );
        Properties jpaProperties = (Properties) properties.get("jpaProperties");
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set JPA hibernate.hbm2ddl.auto property.",
            "update",
            jpaProperties.getProperty("hibernate.hbm2ddl.auto")
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set JPA hibernate.dialect property based on configuration.",
            "org.hibernate.dialect.MySQL5InnoDBDialect",
            jpaProperties.getProperty("hibernate.dialect")
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set JPA hibernate.jdbc.batch_size property based on configuration.",
            "500",
            jpaProperties.getProperty("hibernate.jdbc.batch_size")
        );

        value = entityManagerBean.getConstructorArgumentValues().getGenericArgumentValues().get(0).getValue();
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should create entity manager bean.",
            SharedEntityManagerCreator.class.getName(),
            entityManagerBean.getBeanClassName()
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should create entity manager bean.",
            "createSharedEntityManager",
            entityManagerBean.getFactoryMethodName()
        );
        Assert.assertTrue(
            "AbstractDataSourceBeanDefinitionParser.parse() should create entity manager bean based on entity manager factory.",
            value instanceof RuntimeBeanReference
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should create entity manager bean based on entity manager factory.",
            "entityManagerFactory:foo",
            ((RuntimeBeanReference) value).getBeanName()
        );

        value = repositoryFactoryBean.getConstructorArgumentValues().getGenericArgumentValues().get(0).getValue();
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should create repository factor bean.",
            JpaRepositoryFactory.class.getName(),
            repositoryFactoryBean.getBeanClassName()
        );
        Assert.assertTrue(
            "AbstractDataSourceBeanDefinitionParser.parse() should create repository factor bean based on entity manager.",
            value instanceof RuntimeBeanReference
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should create repository factor bean based on entity manager.",
            "entityManager:foo",
            ((RuntimeBeanReference) value).getBeanName()
        );

        value = transactionManagerBean.getConstructorArgumentValues().getGenericArgumentValues().get(0).getValue();
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should create transaction manager bean.",
            JpaTransactionManager.class.getName(),
            transactionManagerBean.getBeanClassName()
        );
        Assert.assertTrue(
            "AbstractDataSourceBeanDefinitionParser.parse() should create transaction manager bean based on entity manager factory.",
            value instanceof RuntimeBeanReference
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should create transaction manager bean based on entity manager factory.",
            "entityManagerFactory:foo",
            ((RuntimeBeanReference) value).getBeanName()
        );
    }

    @Test
    public void parse_withUser()
    {
        AbstractDataSourceBeanDefinitionParser parser
            = new AbstractDataSourceBeanDefinitionParserTest.DataSourceBeanDefinitionParser();

        // prepare attributes
        Mockito.when(this.element.getAttribute("name")).thenReturn("foo");
        Mockito.when(this.element.getAttribute("driverClass")).thenReturn("com.mysql.jdbc.Driver");
        Mockito.when(this.element.getAttribute("jdbcUrl")).thenReturn("jdbc:mysql://localhost/test");
        Mockito.when(this.element.hasAttribute("user")).thenReturn(true);
        Mockito.when(this.element.getAttribute("user")).thenReturn("admin");
        Mockito.when(this.element.hasAttribute("password")).thenReturn(false);
        Mockito.when(this.element.getAttribute("acquireIncrement")).thenReturn("2");
        Mockito.when(this.element.getAttribute("minPoolSize")).thenReturn("5");
        Mockito.when(this.element.getAttribute("maxPoolSize")).thenReturn("100");
        Mockito.when(this.element.getAttribute("maxIdleTime")).thenReturn("30");
        Mockito.when(this.element.getAttribute("initialPoolSize")).thenReturn("10");
        Mockito.when(this.element.getAttribute("hibernateDialect")).thenReturn("org.hibernate.dialect.MySQL5InnoDBDialect");
        Mockito.when(this.element.getAttribute("hibernateJdbcBatchSize")).thenReturn("500");

        parser.parse(this.element, new ParserContext(this.readerContext, null));
        Mockito.verify(this.registry).registerBeanDefinition(
            Matchers.eq("dataSource:foo"),
            this.dataSourceCaptor.capture()
        );

        MutablePropertyValues properties = this.dataSourceCaptor.getValue().getPropertyValues();

        // bean parameters assertions
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set user property for dataSource if it was configured.",
            "admin",
            properties.get("user")
        );
    }

    @Test
    public void parse_withPassword()
    {
        AbstractDataSourceBeanDefinitionParser parser
            = new AbstractDataSourceBeanDefinitionParserTest.DataSourceBeanDefinitionParser();

        // prepare attributes
        Mockito.when(this.element.getAttribute("name")).thenReturn("foo");
        Mockito.when(this.element.getAttribute("driverClass")).thenReturn("com.mysql.jdbc.Driver");
        Mockito.when(this.element.getAttribute("jdbcUrl")).thenReturn("jdbc:mysql://localhost/test");
        Mockito.when(this.element.hasAttribute("user")).thenReturn(false);
        Mockito.when(this.element.hasAttribute("password")).thenReturn(true);
        Mockito.when(this.element.getAttribute("password")).thenReturn("secret");
        Mockito.when(this.element.getAttribute("acquireIncrement")).thenReturn("2");
        Mockito.when(this.element.getAttribute("minPoolSize")).thenReturn("5");
        Mockito.when(this.element.getAttribute("maxPoolSize")).thenReturn("100");
        Mockito.when(this.element.getAttribute("maxIdleTime")).thenReturn("30");
        Mockito.when(this.element.getAttribute("initialPoolSize")).thenReturn("10");
        Mockito.when(this.element.getAttribute("hibernateDialect")).thenReturn("org.hibernate.dialect.MySQL5InnoDBDialect");
        Mockito.when(this.element.getAttribute("hibernateJdbcBatchSize")).thenReturn("500");

        parser.parse(this.element, new ParserContext(this.readerContext, null));
        Mockito.verify(this.registry).registerBeanDefinition(
            Matchers.eq("dataSource:foo"),
            this.dataSourceCaptor.capture()
        );

        MutablePropertyValues properties = this.dataSourceCaptor.getValue().getPropertyValues();

        // bean parameters assertions
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.parse() should set password property for dataSource if it was configured.",
            "secret",
            properties.get("password")
        );
    }
    
    @Test
    public void createRepositoryBeanDefinition()
    {
        AbstractDataSourceBeanDefinitionParserTest.DataSourceBeanDefinitionParser parser
            = new AbstractDataSourceBeanDefinitionParserTest.DataSourceBeanDefinitionParser();

        String name = "foo";

        BeanDefinition repositryBean = parser.createRepositoryBeanDefinition(
            name,
            AbstractDataSourceBeanDefinitionParserTest.class
        );

        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.createRepositoryBeanDefinition() should set factory bean name in current scope.",
            "repositoryFactory:foo",
            repositryBean.getFactoryBeanName()
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.createRepositoryBeanDefinition() should set factory method name.",
            "getRepository",
            repositryBean.getFactoryMethodName()
        );

        List<ConstructorArgumentValues.ValueHolder> arguments
            = repositryBean.getConstructorArgumentValues().getGenericArgumentValues();

        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.createRepositoryBeanDefinition() should set destination type of the repository.",
            1,
            arguments.size()
        );
        Assert.assertEquals(
            "AbstractDataSourceBeanDefinitionParser.createRepositoryBeanDefinition() should set destination type of the repository to given class.",
            AbstractDataSourceBeanDefinitionParserTest.class,
            arguments.get(0).getValue()
        );
    }
}
