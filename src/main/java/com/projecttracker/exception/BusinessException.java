package com.projecttracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception ném ra khi vi phạm quy tắc nghiệp vụ (business rule).
 *
 * <p>Ví dụ: deadline mới phải sau deadline cũ, không thể xóa dự án đang
 * có task chưa hoàn thành, thêm thành viên đã tồn tại trong dự án.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    /**
     * @param message Mô tả vi phạm nghiệp vụ
     */
    public BusinessException(String message) {
        super(message);
    }
}
