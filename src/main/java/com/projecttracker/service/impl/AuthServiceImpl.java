package com.projecttracker.service.impl;

import com.projecttracker.dto.request.LoginRequest;
import com.projecttracker.dto.request.RegisterRequest;
import com.projecttracker.dto.response.AuthResponse;
import com.projecttracker.entity.User;
import com.projecttracker.exception.BusinessException;
import com.projecttracker.repository.UserRepository;
import com.projecttracker.security.JwtTokenProvider;
import com.projecttracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai AuthService - xử lý logic xác thực người dùng.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * {@inheritDoc}
     *
     * <p>Quy trình:
     * <ol>
     *   <li>Dùng AuthenticationManager xác thực email/password</li>
     *   <li>Load user từ DB</li>
     *   <li>Generate JWT token</li>
     *   <li>Trả về AuthResponse với token và thông tin user</li>
     * </ol>
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Đăng nhập với email: {}", request.getEmail());

        // Spring Security sẽ ném BadCredentialsException nếu sai email/pass
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Không tìm thấy user"));

        String token = jwtTokenProvider.generateToken(user.getId());
        log.info("Đăng nhập thành công cho user: {}", user.getEmail());

        return buildAuthResponse(user, token);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Quy trình:
     * <ol>
     *   <li>Kiểm tra email và username chưa tồn tại</li>
     *   <li>Mã hóa password bằng BCrypt</li>
     *   <li>Lưu user vào DB</li>
     *   <li>Generate JWT token và trả về</li>
     * </ol>
     * </p>
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Đăng ký tài khoản mới với email: {}", request.getEmail());

        // Kiểm tra email và username unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email '" + request.getEmail() + "' đã được sử dụng");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.UserRole.MEMBER)
                .build();

        User savedUser = userRepository.save(newUser);
        String token = jwtTokenProvider.generateToken(savedUser.getId());

        log.info("Đăng ký thành công cho user: {}", savedUser.getEmail());
        return buildAuthResponse(savedUser, token);
    }

    /**
     * Xây dựng AuthResponse từ user và token.
     *
     * @param user  User entity
     * @param token JWT access token
     * @return AuthResponse đã được populate
     */
    private AuthResponse buildAuthResponse(User user, String token) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .expiresIn(86400L) // 24 giờ
                .user(userInfo)
                .build();
    }
}
