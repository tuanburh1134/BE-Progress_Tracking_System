-- =============================================================================
-- V8: Thêm cột project_id vào bảng build_reports để liên kết lịch sử CI/CD với từng dự án
-- Phiên bản: 1.0.0
-- =============================================================================

ALTER TABLE build_reports ADD COLUMN project_id BIGINT;
ALTER TABLE build_reports ADD CONSTRAINT fk_build_reports_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL;
CREATE INDEX idx_build_reports_project ON build_reports(project_id);
