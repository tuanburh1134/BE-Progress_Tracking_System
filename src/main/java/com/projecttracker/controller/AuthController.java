package com.projecttracker.controller;

import com.projecttracker.dto.request.LoginRequest;
import com.projecttracker.dto.request.RegisterRequest;
import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.AuthResponse;
import com.projecttracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller xử lý các request liên quan đến xác thực.
 *
 * <p>Các endpoint trong controller này là PUBLIC - không cần JWT token.</p>
 *
 * <p>Base URL: /api/auth</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Đăng nhập, đăng ký tài khoản")
public class AuthController {

    private final AuthService authService;
    private final com.projecttracker.service.OtpService otpService;

    /**
     * Gửi mã OTP xác thực qua Email.
     *
     * <p>POST /api/auth/send-otp</p>
     */
    @PostMapping("/send-otp")
    @Operation(summary = "Gửi mã OTP qua Email", description = "Tạo và gửi mã xác thực OTP 6 số đến email")
    public ResponseEntity<ApiResponse<String>> sendOtp(
            @Valid @RequestBody com.projecttracker.dto.request.SendOtpRequest request) {

        otpService.generateAndSendOtp(request.getEmail(), request.getType());
        return ResponseEntity.ok(ApiResponse.success(
                "Mã OTP đã được gửi đến email " + request.getEmail() + ". Vui lòng kiểm tra hộp thư!",
                "Gửi OTP thành công"
        ));
    }

    /**
     * Đăng nhập và nhận JWT token.
     *
     * <p>POST /api/auth/login</p>
     *
     * @param request Body chứa email và password
     * @return 200 OK với JWT token và thông tin user
     */
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Xác thực và nhận JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công"));
    }

    /**
     * Đăng ký tài khoản mới kèm xác thực OTP.
     *
     * <p>POST /api/auth/register</p>
     *
     * @param request Body chứa thông tin đăng ký và mã OTP
     * @return 201 Created với JWT token (đăng nhập ngay sau khi đăng ký)
     */
    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Xác thực OTP và tạo tài khoản mới")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đăng ký thành công"));
    }

    /**
     * Đăng nhập hoặc tự động đăng ký bằng tài khoản Google.
     *
     * <p>POST /api/auth/google</p>
     *
     * @param request Body chứa Google ID Token (credential)
     * @return 200 OK với JWT token và thông tin user
     */
    @PostMapping("/google")
    @Operation(summary = "Đăng nhập bằng Google", description = "Xác thực Google ID Token và nhận JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(
            @Valid @RequestBody com.projecttracker.dto.request.GoogleLoginRequest request) {

        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập bằng Google thành công"));
    }
}
