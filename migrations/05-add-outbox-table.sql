--liquibase formatted sql
--changeset linktracker:5
CREATE TABLE IF NOT EXISTS outbox_messages (
    id          UUID PRIMARY KEY,
    payload     TEXT NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(20) NOT NULL,
    attempts    INTEGER DEFAULT 0
);

CREATE INDEX idx_outbox_status_id ON outbox_messages (status, id);
