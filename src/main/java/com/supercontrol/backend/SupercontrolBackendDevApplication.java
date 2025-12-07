package com.supercontrol.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SupercontrolBackendDevApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupercontrolBackendDevApplication.class, args);
    }
}
