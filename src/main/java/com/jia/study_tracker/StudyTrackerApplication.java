package com.jia.study_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.jia.study_tracker")
@EnableScheduling
public class StudyTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudyTrackerApplication.class, args);
	}
}
