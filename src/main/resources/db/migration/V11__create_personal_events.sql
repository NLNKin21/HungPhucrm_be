-- V11__create_personal_events.sql

CREATE TABLE personal_events (
    id          BINARY(16)    NOT NULL,
    user_id     BINARY(16)    NOT NULL,
    title       VARCHAR(255)  NOT NULL,
    description TEXT          NULL,
    event_date  DATE          NOT NULL,
    start_time  TIME          NULL,
    end_time    TIME          NULL,
    color       VARCHAR(20)   NOT NULL DEFAULT '#2563EB',
    reminder    BIT(1)        NOT NULL DEFAULT b'0',
    repeat_type ENUM('NONE', 'DAILY', 'WEEKLY', 'MONTHLY')
                              NOT NULL DEFAULT 'NONE',
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                              ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_personal_events_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_personal_events_user_date (user_id, event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;