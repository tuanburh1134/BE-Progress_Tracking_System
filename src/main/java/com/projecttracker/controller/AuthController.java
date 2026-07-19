package com.projecttracker.controller;

import com.projecttracker.dto.request.LoginRequest;
import com.projecttracker.dto.request.RegisterRequest;
import com.projecttracker.dto.response.ApiResponse;
import com.projecttracker.dto.response.AuthResponse;
import com.projecttracker.entity.User;
import com.projecttracker.repository.UserRepository;
import com.projecttracker.security.UserPrincipal;
import com.projecttracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UserRepository userRepository;

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

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thông tin user hiện tại", description = "Dùng để frontend quick-check token validity")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> me(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy người dùng"));
        }

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userInfo, "Thông tin người dùng"));
    }
}
