CREATE TABLE work_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    hours_spent DECIMAL(5,2) NOT NULL,
    note TEXT,

    created_at DATETIME NOT NULL,

    CONSTRAINT fk_worklog_task
        FOREIGN KEY(task_id)
        REFERENCES tasks(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_worklog_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
);