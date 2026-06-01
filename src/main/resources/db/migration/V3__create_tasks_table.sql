-- =============================================================================
-- V3: Tạo bảng tasks
-- Phiên bản: 1.0.0
-- Mô tả: Bảng lưu task (công việc) trong các dự án - đơn vị cơ bản của Kanban
-- Các trường estimated_hours và actual_hours là input quan trọng cho AI module
-- =============================================================================

CREATE TABLE IF NOT EXISTS tasks (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    title               VARCHAR(300)    NOT NULL,
    description         TEXT,
    status              VARCHAR(20)     NOT NULL DEFAULT 'TODO',
    priority            VARCHAR(10)     NOT NULL DEFAULT 'MEDIUM',
    deadline            DATE,
    estimated_hours     DECIMAL(7,2),       -- Thời gian ước tính (giờ)
    actual_hours        DECIMAL(7,2),       -- Thời gian thực tế (giờ) - AI input
    start_date          DATE,
    completed_date      DATE,
    display_order       INT             NOT NULL DEFAULT 0,
    project_id          BIGINT          NOT NULL,
    assignee_id         BIGINT,             -- NULL = chưa phân công
    created_by          BIGINT          NOT NULL,
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6),

    CONSTRAINT pk_tasks PRIMARY KEY (id),
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_tasks_status CHECK (status IN ('TODO','IN_PROGRESS','IN_REVIEW','DONE','BLOCKED')),
    CONSTRAINT chk_tasks_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    CONSTRAINT chk_tasks_hours CHECK (estimated_hours >= 0 AND actual_hours >= 0)
);

CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_deadline ON tasks(deadline);
-- Composite index cho Kanban board query
CREATE INDEX idx_tasks_project_status_order ON tasks(project_id, status, display_order);
