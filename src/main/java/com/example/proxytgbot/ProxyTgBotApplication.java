package com.example.proxytgbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProxyTgBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxyTgBotApplication.class, args);
    }

}
