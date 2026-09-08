package com.projecttracker.service;

import com.projecttracker.dto.request.GoogleLoginRequest;
import com.projecttracker.dto.request.LoginRequest;
import com.projecttracker.dto.request.RegisterRequest;
import com.projecttracker.dto.response.AuthResponse;

/**
 * Service interface định nghĩa các thao tác xác thực người dùng.
 *
 * <p>Tuân thủ nguyên tắc DIP (Dependency Inversion): các class phụ thuộc
 * vào interface này, không phụ thuộc vào implementation cụ thể.</p>
 * Service xác thực người dùng.
 */
public interface AuthService {

    /**
     * Xác thực thông tin đăng nhập và trả về JWT token.
     *
     * @param request Thông tin đăng nhập (email + password)
     * @return AuthResponse chứa JWT token và thông tin user
     */
    AuthResponse login(LoginRequest request);

    /**
     * Đăng ký tài khoản mới.
     *
     * @param request Thông tin đăng ký
     * @return AuthResponse với token để đăng nhập ngay sau khi đăng ký
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Đăng nhập hoặc tự động đăng ký bằng tài khoản Google (Google Sign-In).
     *
     * @param request Chứa Google ID token
     * @return AuthResponse chứa JWT token và thông tin user
     */
    AuthResponse googleLogin(GoogleLoginRequest request);
}
