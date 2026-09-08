-- ============================================================
-- V12: Đảm bảo đầy đủ tất cả các cột trong bảng team_members
-- ============================================================

-- 1. Cột status
SET @status_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'team_members' 
      AND COLUMN_NAME = 'status'
);

SET @stmt_status = IF(@status_exists = 0,
    'ALTER TABLE team_members ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT \'PENDING\';',
    'SELECT 1;'
);

PREPARE stmt_status FROM @stmt_status;
EXECUTE stmt_status;
DEALLOCATE PREPARE stmt_status;

-- 2. Cột invited_at
SET @invited_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'team_members' 
      AND COLUMN_NAME = 'invited_at'
);

SET @stmt_invited = IF(@invited_exists = 0,
    'ALTER TABLE team_members ADD COLUMN invited_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);',
    'SELECT 1;'
);

PREPARE stmt_invited FROM @stmt_invited;
EXECUTE stmt_invited;
DEALLOCATE PREPARE stmt_invited;

-- 3. Cột responded_at
SET @responded_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'team_members' 
      AND COLUMN_NAME = 'responded_at'
);

SET @stmt_responded = IF(@responded_exists = 0,
    'ALTER TABLE team_members ADD COLUMN responded_at DATETIME(6) NULL;',
    'SELECT 1;'
);

PREPARE stmt_responded FROM @stmt_responded;
EXECUTE stmt_responded;
DEALLOCATE PREPARE stmt_responded;

-- 4. Cột inviter_id
SET @inviter_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'team_members' 
      AND COLUMN_NAME = 'inviter_id'
);

SET @stmt_inviter = IF(@inviter_exists = 0,
    'ALTER TABLE team_members ADD COLUMN inviter_id BIGINT NULL;',
    'SELECT 1;'
);

PREPARE stmt_inviter FROM @stmt_inviter;
EXECUTE stmt_inviter;
DEALLOCATE PREPARE stmt_inviter;

-- Cập nhật inviter_id nếu NULL
UPDATE team_members SET inviter_id = user_id WHERE inviter_id IS NULL;
