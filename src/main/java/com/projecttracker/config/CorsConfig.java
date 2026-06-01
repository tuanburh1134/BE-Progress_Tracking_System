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

        // Cho phép các origin từ Frontend (dev + production)
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",   // Vite dev server
                "http://localhost:3000",   // Alternative dev port
                "http://localhost:80"      // Production nginx
        ));

        // Các HTTP method được phép
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers được phép trong request
        configuration.setAllowedHeaders(List.of("*"));

        // Cho phép gửi credentials (cookies, Authorization header)
        configuration.setAllowCredentials(true);

        // Cache preflight response trong 1 giờ (giảm số lượng OPTIONS request)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
