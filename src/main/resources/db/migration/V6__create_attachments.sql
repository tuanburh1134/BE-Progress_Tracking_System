CREATE TABLE attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    task_id BIGINT NOT NULL,

    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,

    uploaded_by BIGINT NOT NULL,

    created_at DATETIME NOT NULL,

    CONSTRAINT fk_attachment_task
        FOREIGN KEY(task_id)
        REFERENCES tasks(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_attachment_user
        FOREIGN KEY(uploaded_by)
        REFERENCES users(id)
);
