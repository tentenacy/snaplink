package com.tenacy.snaplink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SnaplinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnaplinkApplication.class, args);
	}

}
