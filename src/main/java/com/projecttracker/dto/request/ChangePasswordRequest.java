package com.projecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO yêu cầu Đổi mật khẩu có xác thực OTP.
 */
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Mã OTP không được để trống")
    @Size(min = 6, max = 6, message = "Mã OTP phải đúng 6 chữ số")
    private String otpCode;

    @NotBlank(message = "Mật khẩu cũ không được để trống")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu mới phải có tối thiểu 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).+$",
            message = "Mật khẩu mới phải chứa ít nhất 1 chữ cái in hoa, 1 chữ số và 1 ký tự đặc biệt"
    )
    private String newPassword;
}
