package com.projecttracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Swagger / OpenAPI 3 để tài liệu hóa REST API.
 *
 * <p>Truy cập Swagger UI tại: http://localhost:8080/swagger-ui.html</p>
 * <p>OpenAPI JSON tại: http://localhost:8080/api-docs</p>
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Cấu hình OpenAPI với thông tin dự án và JWT Bearer authentication.
     *
     * @return OpenAPI specification đã cấu hình
     */
    @Bean
    public OpenAPI openApiSpecification() {
        return new OpenAPI()
                .info(buildApiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildJwtSecurityScheme()));
    }

    /**
     * Xây dựng thông tin metadata của API.
     *
     * @return Info object với title, version, description
     */
    private Info buildApiInfo() {
        return new Info()
                .title("Project Tracker API")
                .version("1.0.0")
                .description("""
                        # Hệ Thống Theo Dõi Tiến Độ Dự Án Thông Minh
                        
                        REST API cho hệ thống quản lý dự án với tích hợp AI dự đoán tiến độ.
                        
                        ## Tính năng chính:
                        - Quản lý dự án và task
                        - Phân công công việc cho thành viên
                        - Dashboard theo dõi tiến độ
                        - AI dự đoán nguy cơ trễ deadline
                        
                        ## Xác thực:
                        Sử dụng JWT Bearer Token. Đăng nhập qua `/api/auth/login` để lấy token.
                        """)
                .contact(new Contact()
                        .name("Project Tracker Team")
                        .email("projecttracker@email.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Xây dựng JWT Bearer security scheme cho Swagger UI.
     *
     * @return SecurityScheme với type HTTP Bearer JWT
     */
    private SecurityScheme buildJwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Nhập JWT token (không cần tiền tố 'Bearer ')");
    }
}
