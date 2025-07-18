package com.example.adoptions.adoptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.DirectChannelSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.modulith.events.Externalized;

import java.io.File;

@Externalized(IntegrationConfiguration.DOG_ADOPTED_EVENT_CHANNEL)
public record DogAdoptedEvent(int dogId) {
}

@Configuration
class IntegrationConfiguration {

    static final String DOG_ADOPTED_EVENT_CHANNEL = "dogAdoptedEventChannel";

    @Bean
    IntegrationFlow fileInboundIntegrationFlow(
            @Value("file://${HOME}/Desktop/in") File in,
            @Value("file://${HOME}/Desktop/out") File out
    ) {
        return IntegrationFlow
                .from(Files.inboundAdapter(in).autoCreateDirectory(true))
                .handle((GenericHandler<File>) (payload, headers) -> {
                    System.out.println("handling " + payload);
                    headers.forEach((k, v) -> System.out.println("header " + k + " = " + v));
                    return payload;
                })
                .handle(Files.outboundAdapter(out).autoCreateDirectory(true))
                .get();
    }

    @Bean
    IntegrationFlow dogAdoptedEventFlow(DirectChannelSpec dogAdoptedEventChannel) {
        return IntegrationFlow
                .from(dogAdoptedEventChannel)
                .handle((GenericHandler<DogAdoptedEvent>) (payload, headers) -> {
                    System.out.println("handling " + payload);
                    headers.forEach((k, v) -> System.out.println("header " + k + " = " + v));
                    return null;
                })
                .get();
    }

    @Bean(DOG_ADOPTED_EVENT_CHANNEL)
    DirectChannelSpec dogAdoptedEventChannel() {
        return MessageChannels.direct();
    }

}