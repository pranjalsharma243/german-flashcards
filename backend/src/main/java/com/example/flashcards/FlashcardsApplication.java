package com.example.flashcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class FlashcardsApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlashcardsApplication.class, args);
    }
}
