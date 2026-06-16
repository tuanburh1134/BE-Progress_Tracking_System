-- V5: Tạo bảng project_members nếu chưa tồn tại
-- Lưu quan hệ nhiều-nhiều giữa User và Project kèm vai trò

CREATE TABLE IF NOT EXISTS project_members (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'MEMBER',
    joined_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT uk_project_user UNIQUE (project_id, user_id),

    INDEX idx_pm_project_id (project_id),
    INDEX idx_pm_user_id    (user_id)
);
