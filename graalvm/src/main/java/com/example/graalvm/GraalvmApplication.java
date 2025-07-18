package com.example.graalvm;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@ImportRuntimeHints(GraalvmApplication.Hints.class)
@SpringBootApplication
public class GraalvmApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraalvmApplication.class, args);
    }



    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
          hints.resources().registerPattern("message");
        }
    }

}

@Controller
@ResponseBody
class HelloController {

    @GetMapping("/hi")
    Map<String, String> hi() {
        return Map.of("hi", "hi");
    }
}