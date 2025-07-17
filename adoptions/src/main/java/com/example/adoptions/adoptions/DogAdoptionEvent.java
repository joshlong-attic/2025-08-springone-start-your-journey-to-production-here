package com.example.adoptions.adoptions;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.DirectChannelSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.messaging.MessageChannel;
import org.springframework.modulith.events.Externalized;

@Externalized(DogAdoptionEventExternalizationConfiguration.ADOPTIONS_DESTINATION)
public record DogAdoptionEvent(int dogId) {
}

@Configuration
class DogAdoptionEventExternalizationConfiguration {

    static final String ADOPTIONS_DESTINATION = "adoptions";

    @Bean(name = ADOPTIONS_DESTINATION)
    DirectChannelSpec adoptionsChannel() {
        return MessageChannels.direct();
    }

    @Bean
    Binding binding(Queue q, Exchange e) {
        return BindingBuilder
                .bind(q)
                .to(e)
                .with(ADOPTIONS_DESTINATION)
                .noargs();
    }

    @Bean
    Queue queue() {
        return QueueBuilder
                .durable(ADOPTIONS_DESTINATION)
                .build();
    }

    @Bean
    Exchange exchange() {
        return ExchangeBuilder
                .directExchange(ADOPTIONS_DESTINATION)
                .build();
    }


    @Bean
    IntegrationFlow integrationFlow(@Qualifier(ADOPTIONS_DESTINATION) MessageChannel outgoingChannel,
                                    @Value("file://${HOME}/Desktop/outbound") Resource file,
                                    AmqpTemplate amqpTemplate)
            throws Exception {


        // producer-> [ exchanges -> queues ] <-- consumer

        var amqp = Amqp.outboundAdapter(amqpTemplate)
                .routingKey(ADOPTIONS_DESTINATION)
                .exchangeName(ADOPTIONS_DESTINATION);

        return IntegrationFlow
                .from(outgoingChannel)
                .handle((GenericHandler<DogAdoptionEvent>) (payload, headers) -> {
                    System.out.println("-------");
                    System.out.println(payload);
                    headers.forEach((k, v) -> System.out.println(k + ": " + v));
                    return payload;
                })
//                .transform(new ObjectToJsonTransformer())
                .handle(amqp)
//                .handle(Files.outboundAdapter(file.getFile()).autoCreateDirectory(true))
//                .transform()
//                .filter()
//                .split()
//                .aggregate()
//                .rou
                .get();
    }
}