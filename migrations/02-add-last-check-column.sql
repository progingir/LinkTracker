--liquibase formatted sql
--changeset linktracker:2
ALTER TABLE link ADD COLUMN last_check_at TIMESTAMP WITH TIME ZONE DEFAULT TO_TIMESTAMP(0);
CREATE INDEX idx_link_last_check_at ON link(last_check_at);
