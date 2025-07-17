package com.example.processor;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.json.JsonToObjectTransformer;

@SpringBootApplication
public class ProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessorApplication.class, args);
    }

    @Bean
    IntegrationFlow inboundAdoptionEventIntegrationFlow(ConnectionFactory connectionFactory) {
        var amqp = Amqp.inboundAdapter(connectionFactory, "adoptions");
        return IntegrationFlow
                .from(amqp)
                .transform((GenericTransformer<byte[], String>) source -> new String(source))
                .transform(new JsonToObjectTransformer(DogAdoptionEvent.class))
                .handle((payload, headers) -> {
                    headers.forEach((k, v) -> System.out.println(k + '=' + v));
                    System.out.println("got payload: " + payload);
                    return null;
                })
                .get();
    }

}

// look mom, no Lombok!
record DogAdoptionEvent(int dogId) {
}

