package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    @Order(1)
    RouterFunction<ServerResponse> routes() {
        return route()
                .filter(TokenRelayFilterFunctions.tokenRelay())
                .before(BeforeFilterFunctions.uri("http://localhost:8080"))
                .before(BeforeFilterFunctions.rewritePath("/api", "/"))
                .GET("/api/**", http())

                .build();
    }

    @Bean
    @Order(2)
    RouterFunction<ServerResponse> ui() {
        return route()
                .filter(TokenRelayFilterFunctions.tokenRelay())
                .before(BeforeFilterFunctions.uri("http://localhost:8020"))
                .GET("/**", http())
                .build();
    }
}
