CREATE TABLE progress_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    task_id BIGINT NOT NULL,

    old_progress INT,
    new_progress INT,

    updated_by BIGINT NOT NULL,

    created_at DATETIME NOT NULL,

    CONSTRAINT fk_progress_task
        FOREIGN KEY(task_id)
        REFERENCES tasks(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_progress_user
        FOREIGN KEY(updated_by)
        REFERENCES users(id)
);