-- =============================================================================
-- V2: Tạo bảng projects và project_members
-- =============================================================================

-- Bảng projects
CREATE TABLE IF NOT EXISTS projects (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200)    NOT NULL,
    description     TEXT,
    start_date      DATE,
    deadline        DATE,

    status          ENUM(
                        'PLANNING',
                        'IN_PROGRESS',
                        'ON_HOLD',
                        'COMPLETED',
                        'CANCELLED'
                    ) NOT NULL DEFAULT 'PLANNING',

    priority        ENUM(
                        'LOW',
                        'MEDIUM',
                        'HIGH',
                        'CRITICAL'
                    ) NOT NULL DEFAULT 'MEDIUM',

    progress        INT             NOT NULL DEFAULT 0,
    owner_id        BIGINT          NOT NULL,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6),

    CONSTRAINT pk_projects PRIMARY KEY (id),
    CONSTRAINT fk_projects_owner
        FOREIGN KEY (owner_id) REFERENCES users(id),

    CONSTRAINT chk_projects_progress
        CHECK (progress >= 0 AND progress <= 100)
);

CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_deadline ON projects(deadline);

-- Bảng project_members
CREATE TABLE IF NOT EXISTS project_members (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    project_id  BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,

    role        ENUM(
                    'ADMIN',
                    'MANAGER',
                    'MEMBER',
                    'VIEWER'
                ) NOT NULL,

    joined_at   DATETIME(6) NOT NULL,

    CONSTRAINT pk_project_members PRIMARY KEY (id),
    CONSTRAINT uk_project_user UNIQUE (project_id, user_id),

    CONSTRAINT fk_pm_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pm_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_pm_project_id ON project_members(project_id);
CREATE INDEX idx_pm_user_id ON project_members(user_id);