package com.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Wrapper chuẩn cho tất cả response trả về từ REST API.
 *
 * <p>Mọi endpoint đều trả về cấu trúc thống nhất này, giúp Frontend
 * xử lý response một cách nhất quán.</p>
 *
 * <p>Ví dụ response thành công:
 * <pre>{@code
 * {
 *   "success": true,
 *   "message": "Tạo dự án thành công",
 *   "data": { ... },
 *   "timestamp": "2024-01-01T12:00:00"
 * }
 * }</pre>
 * </p>
 *
 * <p>Ví dụ response lỗi:
 * <pre>{@code
 * {
 *   "success": false,
 *   "message": "Không tìm thấy dự án",
 *   "data": null,
 *   "timestamp": "2024-01-01T12:00:00"
 * }
 * }</pre>
 * </p>
 *
 * @param <T> Kiểu dữ liệu của phần data trong response
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Không serialize field null
public class ApiResponse<T> {

    /** Trạng thái thành công hay thất bại */
    private final boolean success;

    /** Thông báo mô tả kết quả */
    private final String message;

    /** Dữ liệu trả về (null nếu có lỗi) */
    private final T data;

    /** Thời gian tạo response */
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    // -----------------------------------------------------------------------
    // Static factory methods để tạo response nhanh
    // -----------------------------------------------------------------------

    /**
     * Tạo response thành công với dữ liệu.
     *
     * @param data    Dữ liệu trả về
     * @param message Thông báo thành công
     * @param <T>     Kiểu dữ liệu
     * @return ApiResponse thành công
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Tạo response thành công chỉ với message (không có data).
     *
     * @param message Thông báo thành công
     * @param <T>     Kiểu dữ liệu
     * @return ApiResponse thành công không có data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Tạo response lỗi với thông báo.
     *
     * @param message Thông báo lỗi
     * @param <T>     Kiểu dữ liệu
     * @return ApiResponse thất bại
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
