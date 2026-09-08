-- ============================================================
-- V9: Tạo bảng otps phục vụ xác thực email
-- ============================================================

CREATE TABLE IF NOT EXISTS otps (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    code        VARCHAR(10)  NOT NULL,
    type        VARCHAR(50)  NOT NULL DEFAULT 'REGISTER',
    expired_at  DATETIME(6)  NOT NULL,
    is_used     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME(6)  NOT NULL,
    INDEX idx_otp_email_code (email, code)
);
