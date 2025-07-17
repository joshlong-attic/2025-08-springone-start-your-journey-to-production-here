package com.example.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DemoAutoConfiguration {

    @Bean
    @ConditionalOnProperty("a.b")
    ApplicationRunner applicationRunner() {
        return args -> System.out.println("hello world from the auto config!");
    }
}
