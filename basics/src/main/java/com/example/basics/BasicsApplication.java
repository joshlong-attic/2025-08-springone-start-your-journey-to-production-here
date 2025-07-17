package com.example.basics;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.lang.annotation.*;
import java.util.Map;

// dependency injection
// portable service abstractions
// AOP (aspect oriented programming)
// BeanPostProcessor

public class BasicsApplication {

    public static void main(String[] args) {


        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        PropertySource<?> propertySource = new MapPropertySource("mConfig",
                Map.of("username", "scott", "password", "secret", "url", "jdbc://.."));

        applicationContext.getEnvironment().getPropertySources().addFirst(propertySource);
        applicationContext.register(CustomerConfiguration.class);
        applicationContext.refresh();

        CustomerService customerService = applicationContext.getBean(CustomerService.class);
        Customer customerById = customerService.getCustomerById(42);
        System.out.println("got the customer by id [" + customerById + "]");

    }

}

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@interface MyService {

}

@Configuration
@ComponentScan
class CustomerConfiguration {

    @Bean
    DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    static MyBeanFactoryPostProcessor myBeanFactoryPostProcessor() {
        return new MyBeanFactoryPostProcessor();
    }

    @Bean
    static LoggingBeanPostProcessor loggingBeanPostProcessor(TransactionTemplate transactionTemplate) {
        return new LoggingBeanPostProcessor(transactionTemplate);
    }

    @Bean
    DataSource dataSource(@Value("${username}") String username, @Value("${password}") String pw,
                          @Value("${url}") String url) {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

}

@Service
class StatefulNetworkService implements Loggable {

    private DataSource db;

    private boolean orElse;

    public void setOrElse(boolean orElse) {
        this.orElse = orElse;
    }

    public void setDb(DataSource db) {
        this.db = db;
    }

}

interface Loggable {

}

class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (var beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            System.out.println(beanName + ":" + beanDefinition.getBeanClassName() +
                    beanFactory.getType(beanName));


        }

    }
}

class LoggingBeanPostProcessor implements BeanPostProcessor {

    private final TransactionTemplate transactionTemplate;

    LoggingBeanPostProcessor(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (!(bean instanceof Loggable))
            return bean;

        System.out.println("instrumenting bean [" + beanName + "] with Loggable-ity");
        var proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean
                .addAdvice((MethodInterceptor) invocation -> transactionTemplate.execute(new TransactionCallback<Object>() {

                    @Override
                    public Object doInTransaction(TransactionStatus status) {
                        try {
                            if (invocation.getMethod().getAnnotation(Transactional.class) != null) {
                                return invocation.proceed();
                            }
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                        return null;
                    }
                }));
        proxyFactoryBean.setTarget(bean);
        proxyFactoryBean.setProxyTargetClass(true);
        return proxyFactoryBean.getObject();

    }

}

@Service
class CustomerService implements Loggable {

    private final DataSource db;

    private final TransactionTemplate transactionTemplate;

    CustomerService(DataSource db, TransactionTemplate transactionTemplate) {
        this.db = db;
        this.transactionTemplate = transactionTemplate;
        Assert.notNull(db, "DataSource must not be null");
    }

    @Transactional
    Customer getCustomerById(int id) {
        return this.transactionTemplate.execute(status -> {
            return null; // todo sql
        });

    }

}

record Customer(int id, String name, String email) {
}
