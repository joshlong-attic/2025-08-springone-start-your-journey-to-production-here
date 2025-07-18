package com.example.deskcheck1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@ResponseBody
@SpringBootApplication
public class Deskcheck1Application {

	@GetMapping ("/hi")
	Map <String,String> hi (){
		return Map.of("hi","hi!!!!");
	}

	public static void main(String[] args) {
		SpringApplication.run(Deskcheck1Application.class, args);
	}

}

