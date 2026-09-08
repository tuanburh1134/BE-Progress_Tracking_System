-- ============================================================
-- V11: Đảm bảo cột inviter_id trong bảng team_members
-- ============================================================

SET @inviter_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'team_members' 
      AND COLUMN_NAME = 'inviter_id'
);

SET @stmt = IF(@inviter_exists = 0,
    'ALTER TABLE team_members ADD COLUMN inviter_id BIGINT NULL;',
    'SELECT 1;'
);

PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Gán giá trị mặc định cho inviter_id nếu là NULL (gán bằng user_id)
UPDATE team_members SET inviter_id = user_id WHERE inviter_id IS NULL;

-- Thêm khóa ngoại fk_tm_inviter nếu chưa có
SET @fk_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'team_members'
      AND CONSTRAINT_NAME = 'fk_tm_inviter'
);

SET @stmt_fk = IF(@fk_exists = 0,
    'ALTER TABLE team_members ADD CONSTRAINT fk_tm_inviter FOREIGN KEY (inviter_id) REFERENCES users(id);',
    'SELECT 1;'
);

PREPARE stmt_fk FROM @stmt_fk;
EXECUTE stmt_fk;
DEALLOCATE PREPARE stmt_fk;
