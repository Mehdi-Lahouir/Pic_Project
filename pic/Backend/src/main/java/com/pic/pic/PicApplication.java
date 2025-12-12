package com.pic.pic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PicApplication {

	public static void main(String[] args) {
		SpringApplication.run(PicApplication.class, args);
	}

}
