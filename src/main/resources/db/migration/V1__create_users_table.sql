-- =============================================================================
-- V1: Create users table (ENUM version)
-- =============================================================================

CREATE TABLE users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)     NOT NULL,
    email           VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(100),
    avatar_url      VARCHAR(500),

    role            ENUM('ADMIN','MANAGER','MEMBER','VIEWER') NOT NULL,

    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_username (username)
);

-- Index
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Admin mặc định
INSERT INTO users (
    username, email, password_hash, full_name, role, is_active, created_at
) VALUES (
    'admin',
    'admin@projecttracker.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKPriSMUP5jRLHEJoJ6NLiyjRvlm',
    'System Administrator',
    'ADMIN',
    TRUE,
    NOW()
);