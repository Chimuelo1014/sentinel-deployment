package com.sentinel.backend_for_frontend_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BackendForFrontendServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendForFrontendServiceApplication.class, args);
	}

}
