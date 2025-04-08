package com.kjq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SseBroadcastApplication {
    public static void main(String[] args) {
        SpringApplication.run(SseBroadcastApplication.class, args);
    }
}
