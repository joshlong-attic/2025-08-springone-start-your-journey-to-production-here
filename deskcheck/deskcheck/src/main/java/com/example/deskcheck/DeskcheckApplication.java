package com.example.deskcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeskcheckApplication {

	public static void main(String[] args) {

		System.out.println(System.getenv().get("S3_KEY"));
		SpringApplication.run(DeskcheckApplication.class, args);
	}

}

class Foo {

}

class Bar {

	void blah() {
	}

}