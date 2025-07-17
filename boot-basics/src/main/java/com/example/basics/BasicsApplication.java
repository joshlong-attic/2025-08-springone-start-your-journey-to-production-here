package com.example.basics;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

// SPRING SQUARE
// aop - aspect oriented programming
// di - dependency injection
// portable service abstractions (PlatformTransactionManager)
// autoconfiguration (new in Boot)

@SpringBootApplication
public class BasicsApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BasicsApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(CustomerService customerService) {
        return args -> {
            Customer customerById = customerService.getCustomerById(1);
            System.out.println("got the customer by id [" + customerById + "]");
        };
    }
}


@Service
class CustomerService {

    private final JdbcClient db;

    CustomerService(JdbcClient db) {
        this.db = db;
    }

    @Transactional
    Customer getCustomerById(int id) {
        return this.db
                .sql("select * from customer where id = ?")
                .param(id)
                .query((rs, rowNum) -> new Customer(rs.getInt("id"), rs.getString("name")))
                .single();
    }
}

record Customer(int id, String name) {
}

@Controller
@ResponseBody
class HelloController {

    @GetMapping ("/hello")
    Map<String, String> hello (){
        return Map.of("message",  "Hello World!");
    }
}