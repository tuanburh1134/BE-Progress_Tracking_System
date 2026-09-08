-- ============================================================
-- V10: Đảm bảo cột invited_at và responded_at trong team_members
-- ============================================================

SET @dropdown_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'team_members' 
      AND COLUMN_NAME = 'invited_at'
);

SET @stmt = IF(@dropdown_exists = 0,
    'ALTER TABLE team_members ADD COLUMN invited_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);',
    'SELECT 1;'
);

PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------

SET @resp_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'team_members' 
      AND COLUMN_NAME = 'responded_at'
);

SET @stmt2 = IF(@resp_exists = 0,
    'ALTER TABLE team_members ADD COLUMN responded_at DATETIME(6) NULL;',
    'SELECT 1;'
);

PREPARE stmt2 FROM @stmt2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
