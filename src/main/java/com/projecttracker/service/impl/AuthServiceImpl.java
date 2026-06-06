package com.projecttracker.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        log.info("Đăng nhập với email: {}", request.getEmail());

        SecurityContextHolder.getContext().setAuthentication(
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException("Không tìm thấy user"));

        String token = jwtTokenProvider.generateToken(user.getId());

        return buildAuthResponse(user, token);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "Email '" + request.getEmail() + "' đã được sử dụng");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                    "Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.UserRole.MEMBER)
                .build();

        var savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(savedUser.getId());

        return buildAuthResponse(savedUser, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {

        AuthResponse.UserInfo userInfo =
                AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRole().name())
                        .build();

        return AuthResponse.builder()
                .accessToken(token)
                .expiresIn(86400L)
                .user(userInfo)
                .build();
    }
}