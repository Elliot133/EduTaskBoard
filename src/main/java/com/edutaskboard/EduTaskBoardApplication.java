package com.edutaskboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EduTaskBoardApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduTaskBoardApplication.class, args);
    }
}
