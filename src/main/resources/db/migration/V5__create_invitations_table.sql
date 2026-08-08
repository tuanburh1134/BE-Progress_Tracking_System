-- ============================================================
-- V5: Tạo bảng lưu lời mời thành viên chờ xác nhận
-- ============================================================

CREATE TABLE invitations (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id    BIGINT       NOT NULL,
    inviter_id    BIGINT       NOT NULL,
    invitee_id    BIGINT       NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    responded_at  DATETIME(6),

    CONSTRAINT fk_inv_project  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_inviter  FOREIGN KEY (inviter_id) REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_inv_invitee  FOREIGN KEY (invitee_id) REFERENCES users(id)    ON DELETE CASCADE,

    -- Mỗi người chỉ có 1 lời mời PENDING tại 1 thời điểm trong 1 dự án
    CONSTRAINT uk_inv_project_invitee UNIQUE (project_id, invitee_id)
);

CREATE INDEX idx_inv_invitee_status ON invitations (invitee_id, status);
CREATE INDEX idx_inv_project_id     ON invitations (project_id);
