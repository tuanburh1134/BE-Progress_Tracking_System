package com.projecttracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO yêu cầu gửi mã OTP về email.
 */
@Getter
@Setter
public class SendOtpRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    /** Loại OTP (Mặc định REGISTER) */
    private String type = "REGISTER";
}
