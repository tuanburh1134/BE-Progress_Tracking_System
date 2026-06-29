package com.projecttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class ProjectTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectTrackerApplication.class, args);
    }
}
//test webhook 11111
