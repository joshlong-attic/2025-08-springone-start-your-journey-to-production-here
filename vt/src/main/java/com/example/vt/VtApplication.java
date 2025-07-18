package com.example.vt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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


}

@Controller
@ResponseBody
class VtController {

    private final RestClient http;

    VtController(RestClient.Builder http) {
        this.http = http.build();
    }

    @GetMapping("/delay")
    String delay() {
        var note = Thread.currentThread() + ":";
        var r = this.http
                .get()
                .uri("http://localhost/delay/5")
                .retrieve()
                .body(String.class);
        note += Thread.currentThread();
        System.out.println(note);
        return r;
    }
}