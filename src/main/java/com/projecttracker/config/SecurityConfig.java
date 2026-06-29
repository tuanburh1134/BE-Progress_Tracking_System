package com.projecttracker.config;

import com.projecttracker.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Cấu hình Spring Security cho ứng dụng.
 *
 * <p>Sử dụng JWT stateless authentication, BCrypt password hashing.
 * Các endpoint công khai được khai báo rõ ràng, còn lại yêu cầu xác thực.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Cho phép dùng @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    /** Các endpoint không cần xác thực */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/api/webhooks/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };

    /**
     * Cấu hình Security Filter Chain.
     * Sử dụng JWT stateless, tắt CSRF, cấu hình CORS.
     *
     * @param http HttpSecurity builder
     * @return SecurityFilterChain đã cấu hình
     * @throws Exception nếu cấu hình thất bại
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Tắt CSRF vì dùng JWT stateless
                .csrf(AbstractHttpConfigurer::disable)
                // Cấu hình CORS từ CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // Không tạo session (stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Phân quyền các endpoint
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Cung cấp AuthenticationProvider sử dụng UserDetailsService và BCrypt.
     *
     * @return DaoAuthenticationProvider đã cấu hình
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCrypt password encoder với strength mặc định (10 rounds).
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager để xử lý login request.
     *
     * @param config AuthenticationConfiguration từ Spring
     * @return AuthenticationManager
     * @throws Exception nếu không lấy được manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
