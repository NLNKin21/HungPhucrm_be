CREATE TABLE consultation_notes (
    id              BINARY(16)   NOT NULL,
    consultation_id BINARY(16)   NOT NULL,
    author_id       BINARY(16)   NOT NULL,
    content         TEXT         NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_note_consultation FOREIGN KEY (consultation_id)
        REFERENCES consultations(id) ON DELETE CASCADE,
    CONSTRAINT fk_note_author FOREIGN KEY (author_id)
        REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_note_consultation (consultation_id),
    INDEX idx_note_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;