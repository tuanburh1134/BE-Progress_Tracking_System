package com.projecttracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Cấu hình CORS (Cross-Origin Resource Sharing) cho phép Frontend
 * truy cập Backend từ các origin khác nhau.
 *
 * <p>Trong môi trường development, cho phép tất cả origin.
 * Trong production, chỉ nên cho phép domain cụ thể.</p>
 */
@Configuration
public class CorsConfig {

    /**
     * Định nghĩa nguồn cấu hình CORS áp dụng cho toàn bộ ứng dụng.
     *
     * @return CorsConfigurationSource với các rule đã cấu hình
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép frontend dev/prod chạy ở bất kỳ origin nào, bao gồm localhost các cổng khác nhau
        configuration.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000", "*"));

        // Các HTTP method được phép
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers được phép trong request
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));

        // Cho phép gửi credentials (cookies, Authorization header)
        configuration.setAllowCredentials(true);

        // Cho phép frontend đọc header Authorization nếu cần
        configuration.setExposedHeaders(List.of("Authorization"));

        // Cache preflight response trong 1 giờ (giảm số lượng OPTIONS request)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
