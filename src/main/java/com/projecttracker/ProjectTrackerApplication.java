package com.projecttracker;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class ProjectTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectTrackerApplication.class, args);
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Tự động repair db để sửa các lỗi checksum mismatch nếu có
            flyway.repair();
            // Tiến hành migrate như bình thường
            flyway.migrate();
        };
    }
}
