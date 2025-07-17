package com.example.vt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SpringBootApplication
public class VtApplication {

    public static void main(String[] args) {
        SpringApplication.run(VtApplication.class, args);
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

}

// stolen from the amazing Cora Iberkleid
@Controller
@ResponseBody
class VtController {

//    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();
    
    private final RestClient restClient;

    VtController(RestClient restClient) {
        this.restClient = restClient;
    }

    @GetMapping("/delay")
    String delay() {
        return this.restClient
                .get()
                .uri("http://localhost/delay/5")
                .retrieve()
                .body(String.class);
    }
}
