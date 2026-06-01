package com.projecttracker.exception;

import com.projecttracker.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý tập trung tất cả các exception trong ứng dụng.
 *
 * <p>Thay vì mỗi controller tự xử lý exception, class này đảm nhận việc
 * bắt exception và trả về response lỗi thống nhất theo chuẩn ApiResponse.</p>
 *
 * <p>Thứ tự ưu tiên xử lý: exception cụ thể trước, generic sau.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi validation từ @Valid annotation.
     * Trả về map với key là tên field, value là thông báo lỗi.
     *
     * @param exception MethodArgumentNotValidException từ Spring Validation
     * @return 400 Bad Request với chi tiết lỗi từng field
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Dữ liệu nhập vào không hợp lệ"));
    }

    /**
     * Xử lý khi không tìm thấy resource (404).
     *
     * @param exception ResourceNotFoundException
     * @return 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException exception) {
        log.warn("Resource not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(exception.getMessage()));
    }

    /**
     * Xử lý khi không có quyền truy cập (403).
     *
     * @param exception AccessDeniedException
     * @return 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
        log.warn("Access denied: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Bạn không có quyền thực hiện thao tác này"));
    }

    /**
     * Xử lý khi thông tin đăng nhập sai (401).
     *
     * @param exception BadCredentialsException
     * @return 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException exception) {
        log.warn("Bad credentials attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Email hoặc mật khẩu không đúng"));
    }

    /**
     * Xử lý khi vi phạm business rule (400).
     *
     * @param exception BusinessException
     * @return 400 Bad Request
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        log.warn("Business rule violation: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(exception.getMessage()));
    }

    /**
     * Fallback handler cho tất cả exception chưa được xử lý.
     * Log chi tiết để debug, trả về thông báo chung cho client.
     *
     * @param exception Exception chưa được xử lý
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception exception) {
        log.error("Unhandled exception occurred", exception);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Đã có lỗi xảy ra. Vui lòng thử lại sau."));
    }
}
