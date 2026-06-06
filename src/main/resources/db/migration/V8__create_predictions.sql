CREATE TABLE predictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    project_id BIGINT NOT NULL,

    risk_level ENUM(
        'LOW',
        'MEDIUM',
        'HIGH'
    ) NOT NULL,

    predicted_delay_days INT DEFAULT 0,
    predicted_completion_date DATE,

    confidence_score DECIMAL(5,2),

    recommendation TEXT,

    created_at DATETIME NOT NULL,

    CONSTRAINT fk_prediction_project
        FOREIGN KEY(project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_prediction_project
ON predictions(project_id);