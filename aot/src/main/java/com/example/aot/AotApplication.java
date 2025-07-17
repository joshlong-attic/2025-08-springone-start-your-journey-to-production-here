package com.example.aot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.javapoet.MethodSpec;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.HashSet;
import java.util.function.Consumer;

//@RegisterReflectionForBinding({Cat.class })
//@ImportRuntimeHints(AotApplication.Hints.class)
@SpringBootApplication
public class AotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AotApplication.class, args);
    }

    @Bean
    static BFIAP bfiap() {
        return new BFIAP();
    }

    static class BFIAP implements BeanFactoryInitializationAotProcessor {

        @Override
        public BeanFactoryInitializationAotContribution processAheadOfTime(
                ConfigurableListableBeanFactory beanFactory) {

            var serializable = new HashSet<Class<?>>();

            for (var beanName : beanFactory.getBeanDefinitionNames()) {
                var type = beanFactory.getType(beanName);
                if (Serializable.class.isAssignableFrom(type)) {
                    serializable.add(type);
                    System.out.println("going to register " + beanName + " for serialization");
                }
            }
            return (generationContext, code ) -> {

                // generate code
                code.getMethods().add("hello", builder -> builder
                        .addCode("System.out.println(\"hello\");")) ;

                // register hints
                var hints = generationContext.getRuntimeHints();
                for (var s : serializable) {
                    hints.serialization().registerType(TypeReference.of(s.getName()));
                }
            };
        }
    }

    // 1. reflection
    // 2. jni
    // 3. resource loading
    // 4. serialization
    // 5. proxies

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

            hints.reflection().registerType(Cat.class, MemberCategory.values());
            hints.serialization().registerType(Cat.class);

        }
    }

    @Bean
    ApplicationRunner runner(ObjectMapper objectMapper) {
        return args -> {
            var felix = new Cat("Felix");
            System.out.println(objectMapper.writeValueAsString(felix));

            System.out.println("hello, world!");
        };
    }
}

record Cat(String name) implements Serializable {
}

@Component
class ShoppingCart implements Serializable {

}


@Controller
@ResponseBody
class CatController {

    @GetMapping("/felix")
    Cat cat() {
        return new Cat("Felix");
    }
}