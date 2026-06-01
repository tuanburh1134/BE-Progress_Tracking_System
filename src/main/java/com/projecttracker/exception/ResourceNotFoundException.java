package com.projecttracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception ném ra khi không tìm thấy resource trong database.
 *
 * <p>Ví dụ: tìm project theo ID không tồn tại, tìm user không có trong hệ thống.</p>
 *
 * @see GlobalExceptionHandler#handleResourceNotFound(ResourceNotFoundException)
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param resourceName Tên loại resource (ví dụ: "Project", "Task", "User")
     * @param fieldName    Tên field dùng để tìm (ví dụ: "id", "email")
     * @param fieldValue   Giá trị field đã tìm (ví dụ: 123, "test@email.com")
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("Không tìm thấy %s với %s = '%s'", resourceName, fieldName, fieldValue));
    }

    /**
     * Constructor với thông báo tùy chỉnh.
     *
     * @param message Thông báo lỗi tùy chỉnh
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
