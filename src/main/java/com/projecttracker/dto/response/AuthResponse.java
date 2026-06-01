package com.projecttracker.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO chứa thông tin xác thực trả về sau khi đăng nhập / refresh token.
 *
 * @param accessToken  JWT access token (hạn dùng ngắn)
 * @param refreshToken JWT refresh token (hạn dùng dài)
 * @param tokenType    Loại token, mặc định "Bearer"
 * @param expiresIn    Thời gian hết hạn access token (giây)
 * @param user         Thông tin user đã đăng nhập
 */
@Getter
@Builder
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;

    private UserInfo user;

    /**
     * Thông tin cơ bản của user trả kèm token,
     * tránh phải gọi thêm request /me sau login.
     */
    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String avatarUrl;
        private String role;
    }
}
