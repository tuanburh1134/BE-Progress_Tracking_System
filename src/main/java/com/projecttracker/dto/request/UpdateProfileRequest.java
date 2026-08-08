package com.projecttracker.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhận yêu cầu cập nhật thông tin cá nhân.
 */
@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @Size(max = 100, message = "GitHub username không được vượt quá 100 ký tự")
    private String githubUsername;
}
