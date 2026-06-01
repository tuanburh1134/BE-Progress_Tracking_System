package com.projecttracker.dto.request;

import com.projecttracker.entity.Project;
import com.projecttracker.entity.Task;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO cho request tạo mới hoặc cập nhật task.
 */
@Getter
@Setter
public class TaskRequest {

    @NotBlank(message = "Tiêu đề task không được để trống")
    @Size(min = 2, max = 300, message = "Tiêu đề phải từ 2-300 ký tự")
    private String title;

    @Size(max = 5000, message = "Mô tả không quá 5000 ký tự")
    private String description;

    private Task.TaskStatus status = Task.TaskStatus.TODO;

    private Project.Priority priority = Project.Priority.MEDIUM;

    private LocalDate deadline;

    /** Thời gian ước tính (giờ), dùng cho AI dự đoán */
    @DecimalMin(value = "0.5", message = "Thời gian ước tính tối thiểu là 0.5 giờ")
    @DecimalMax(value = "1000.0", message = "Thời gian ước tính tối đa là 1000 giờ")
    private Double estimatedHours;

    /** ID của người được phân công (có thể null) */
    private Long assigneeId;
}
