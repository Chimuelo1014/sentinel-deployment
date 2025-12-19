package com.sentinel.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// @EnableFeignClients(basePackages = "com.sentinel.auth.client") // ❌
// Temporarily disabled for testing
@EnableScheduling
@EnableAsync
@org.springframework.context.annotation.ComponentScan(basePackages = "com.sentinel.auth")
@org.springframework.web.servlet.config.annotation.EnableWebMvc // ✅ Force Spring MVC
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}