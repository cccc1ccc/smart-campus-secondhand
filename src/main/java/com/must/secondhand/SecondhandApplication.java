package com.must.secondhand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Smart Campus Second-hand Trading Platform (MUST, Group 11 SeekDeep).
 * Entry point of the Spring Boot application.
 */
@EnableScheduling
@SpringBootApplication
public class SecondhandApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandApplication.class, args);
    }
}
