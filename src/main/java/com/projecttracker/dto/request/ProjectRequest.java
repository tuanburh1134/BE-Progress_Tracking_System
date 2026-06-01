package com.projecttracker.dto.request;

import com.projecttracker.entity.Project;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO cho request tạo mới hoặc cập nhật dự án.
 */
@Getter
@Setter
public class ProjectRequest {

    @NotBlank(message = "Tên dự án không được để trống")
    @Size(min = 3, max = 200, message = "Tên dự án phải từ 3-200 ký tự")
    private String name;

    @Size(max = 5000, message = "Mô tả không quá 5000 ký tự")
    private String description;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Deadline không được để trống")
    @Future(message = "Deadline phải là ngày trong tương lai")
    private LocalDate deadline;

    private Project.Priority priority = Project.Priority.MEDIUM;
}
