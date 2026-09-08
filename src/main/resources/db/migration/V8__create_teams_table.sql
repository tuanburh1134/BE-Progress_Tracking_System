-- ============================================================
-- V8: Tạo bảng teams và team_members
-- ============================================================

CREATE TABLE IF NOT EXISTS teams (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    owner_id    BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    CONSTRAINT fk_team_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS team_members (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id      BIGINT      NOT NULL,
    user_id      BIGINT      NOT NULL,
    inviter_id   BIGINT      NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invited_at   DATETIME(6) NOT NULL,
    responded_at DATETIME(6),
    CONSTRAINT fk_tm_team    FOREIGN KEY (team_id)    REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_tm_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_tm_inviter FOREIGN KEY (inviter_id) REFERENCES users(id),
    CONSTRAINT uk_tm_team_user UNIQUE (team_id, user_id)
);
