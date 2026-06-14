-- =============================================================================
-- V4: Thêm cột project_code vào bảng projects
-- Mô tả: Mã định danh ngắn gọn cho dự án (ví dụ: ECOMM-A4)
-- =============================================================================

ALTER TABLE projects
    ADD COLUMN project_code VARCHAR(50) NULL COMMENT 'Mã định danh dự án (tùy chọn)';

CREATE INDEX idx_projects_code ON projects(project_code);
