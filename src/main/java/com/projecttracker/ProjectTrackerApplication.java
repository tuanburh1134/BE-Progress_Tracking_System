package com.projecttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point của ứng dụng Project Tracker Backend.
 *
 * <p>Ứng dụng cung cấp REST API cho hệ thống theo dõi tiến độ dự án thông minh,
 * bao gồm quản lý dự án, task, người dùng và tích hợp AI dự đoán tiến độ.</p>
 */
@SpringBootApplication
@EnableJpaAuditing // Bật tự động cập nhật createdAt, updatedAt
public class ProjectTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectTrackerApplication.class, args);
    }
}
