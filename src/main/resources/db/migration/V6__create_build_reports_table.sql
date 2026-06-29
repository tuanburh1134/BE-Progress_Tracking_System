-- =============================================================================
-- V6: Tạo bảng build_reports phục vụ hệ thống AI CI/CD tự động
-- Phiên bản: 1.0.0
-- =============================================================================

CREATE TABLE IF NOT EXISTS build_reports (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    commit_hash    VARCHAR(100)  NOT NULL,
    commit_message VARCHAR(500),
    author         VARCHAR(100),
    branch         VARCHAR(50),
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    log            LONGTEXT,
    ai_suggestions LONGTEXT,
    created_at     DATETIME(6)   NOT NULL,
    updated_at     DATETIME(6),

    CONSTRAINT pk_build_reports PRIMARY KEY (id)
);

CREATE INDEX idx_build_reports_commit ON build_reports(commit_hash);
CREATE INDEX idx_build_reports_status ON build_reports(status);
