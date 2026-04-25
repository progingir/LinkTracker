--liquibase formatted sql
--changeset linktracker:4
ALTER TABLE link ADD COLUMN error_count INT DEFAULT 0 NOT NULL;
