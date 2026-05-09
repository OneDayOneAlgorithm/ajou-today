package com.hj.ajouToday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ajouTodayApplication {
	public static void main(String[] args) {
		SpringApplication.run(ajouTodayApplication.class, args);
	}
}