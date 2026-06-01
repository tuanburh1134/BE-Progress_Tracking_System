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
     * Đăng ký tài khoản mới.
     *
     * <p>POST /api/auth/register</p>
     *
     * @param request Body chứa thông tin đăng ký
     * @return 201 Created với JWT token (đăng nhập ngay sau khi đăng ký)
     */
    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới và nhận JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đăng ký thành công"));
    }
}
