-- =============================================================================
-- V5: Tạo bảng teams, team_members, notifications
-- Phiên bản: 1.0.0
-- =============================================================================

-- Bảng teams
CREATE TABLE IF NOT EXISTS teams (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_id    BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL,

    CONSTRAINT pk_teams PRIMARY KEY (id),
    CONSTRAINT fk_teams_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_teams_owner_id ON teams(owner_id);

-- Bảng team_members (quan hệ nhiều-nhiều User - Team)
CREATE TABLE IF NOT EXISTS team_members (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    team_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    joined_at  DATETIME(6) NOT NULL,

    CONSTRAINT pk_team_members PRIMARY KEY (id),
    CONSTRAINT uk_team_user UNIQUE (team_id, user_id),
    CONSTRAINT fk_tm_team FOREIGN KEY (team_id) REFERENCES teams(id)  ON DELETE CASCADE,
    CONSTRAINT fk_tm_user FOREIGN KEY (user_id)  REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_tm_team_id ON team_members(team_id);
CREATE INDEX idx_tm_user_id ON team_members(user_id);

-- Bảng notifications
CREATE TABLE IF NOT EXISTS notifications (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    type       VARCHAR(50)  NOT NULL,
    message    TEXT         NOT NULL,
    is_read    TINYINT(1)   NOT NULL DEFAULT 0,
    ref_id     BIGINT,
    created_at DATETIME(6)  NOT NULL,

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT chk_notif_type CHECK (type IN ('ADDED_TO_TEAM','ADDED_TO_PROJECT')),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notif_user_id ON notifications(user_id);
CREATE INDEX idx_notif_is_read ON notifications(user_id, is_read);
