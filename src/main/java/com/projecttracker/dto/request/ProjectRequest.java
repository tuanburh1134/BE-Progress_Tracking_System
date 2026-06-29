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

    @Size(max = 50, message = "Mã dự án không quá 50 ký tự")
    private String projectCode;

    @Size(max = 5000, message = "Mô tả không quá 5000 ký tự")
    private String description;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate deadline;

    private Project.Priority priority = Project.Priority.MEDIUM;

    private Project.Sdlc sdlc = Project.Sdlc.AGILE;

    @Size(max = 300, message = "Đường dẫn GitHub không vượt quá 300 ký tự")
    private String githubLink;
}
