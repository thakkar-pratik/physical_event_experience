package com.example.stadiumflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StadiumflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(StadiumflowApplication.class, args);
    }

}
