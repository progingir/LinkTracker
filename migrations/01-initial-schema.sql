--liquibase formatted sql

--changeset linktracker:1
CREATE TABLE chat (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE link (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url TEXT UNIQUE NOT NULL,
    last_update TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_link_url ON link(url);

CREATE TABLE subscription (
    chat_id BIGINT REFERENCES chat(id) ON DELETE CASCADE,
    link_id BIGINT REFERENCES link(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, link_id)
);

CREATE INDEX idx_subscription_link_id ON subscription(link_id);

CREATE TABLE tag (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE subscription_tag (
    chat_id BIGINT,
    link_id BIGINT,
    tag_id BIGINT REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, link_id, tag_id),
    FOREIGN KEY (chat_id, link_id) REFERENCES subscription(chat_id, link_id) ON DELETE CASCADE
);
