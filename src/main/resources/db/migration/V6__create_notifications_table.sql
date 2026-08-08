-- ============================================================
-- V6: Tạo bảng thông báo hệ thống
-- ============================================================

CREATE TABLE notifications (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id  BIGINT       NOT NULL,
    type          VARCHAR(50)  NOT NULL,
    message       TEXT         NOT NULL,
    reference_id  BIGINT,
    is_read       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_notif_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notif_recipient_read ON notifications (recipient_id, is_read);
CREATE INDEX idx_notif_created_at     ON notifications (created_at DESC);
