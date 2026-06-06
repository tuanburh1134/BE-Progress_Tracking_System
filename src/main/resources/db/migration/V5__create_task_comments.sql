CREATE TABLE task_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    content TEXT NOT NULL,

    created_at DATETIME NOT NULL,

    CONSTRAINT fk_comment_task
        FOREIGN KEY(task_id)
        REFERENCES tasks(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comment_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
);