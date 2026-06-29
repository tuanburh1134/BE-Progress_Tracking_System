-- =============================================================================
-- V7: Thêm cột github_link vào bảng projects phục vụ quản lý link repository
-- Phiên bản: 1.0.0
-- =============================================================================

ALTER TABLE projects ADD COLUMN github_link VARCHAR(300);
