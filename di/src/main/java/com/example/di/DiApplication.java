package com.example.di;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.annotation.*;

public class DiApplication {

    public static void main(String[] args) {
        var ac = new AnnotationConfigApplicationContext(MyConfiguration.class);
        var bean = ac.getBean(CustomerService.class);
        var id = bean.byId(1);
        System.out.println("id: " + id);
    }

}


@ComponentScan
@Configuration
class MyConfiguration {


    // 1. ingest (component scanning, java config, xml, .properties
    // 2. BeanDefinitions
    // 3. beans


    @Bean
    static MyBeanDefinitionRegistryPostProcessor myBeanDefinitionRegistryPostProcessor() {
        return new MyBeanDefinitionRegistryPostProcessor();
    }

    static class MyBeanDefinitionRegistryPostProcessor implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            for (var bn : beanFactory.getBeanDefinitionNames()) {
                var bd = beanFactory.getBeanDefinition(bn);
                var clazz = beanFactory.getType(bn);
                System.out.println("inspecting " + bn + " of type " + clazz);
            }
        }
    }

    @Bean
    static ProxyingBeanPostProcessor proxyingBeanPostProcessor() {
        return new ProxyingBeanPostProcessor();
    }

    static class ProxyingBeanPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (!(bean instanceof Transactional transactional))
                return bean;

            var proxy = new ProxyFactoryBean();
            proxy.setTarget(bean);
            proxy.setProxyTargetClass(true);
            proxy.addAdvice((MethodInterceptor) invocation -> {
                var result = invocation.proceed();
                System.out.println("invoked transactional method " + invocation.getMethod().getName());
                return result;
            });
            return proxy.getObject();


        }
    }


    @Bean
    DataSource dataSource() {
        return new DataSource();
    }
}

interface Transactional {
}

class DataSource {
}

interface CustomerService {

    Customer byId(int id);
}

record Customer(int id) {
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@interface UberConfService {

    /**
     * Alias for {@link Component#value}.
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

}


@UberConfService
class DefaultCustomerService implements CustomerService, Transactional {

    private final DataSource dataSource;

    DefaultCustomerService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/hi")
    String hi() {
        return "";
    }

    @Override
    public Customer byId(int id) {
        return new Customer(24);
    }
}