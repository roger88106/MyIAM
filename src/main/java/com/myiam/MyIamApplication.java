package com.myiam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MyIamApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyIamApplication.class, args);
	}

}
