CREATE TABLE IF NOT EXISTS contest
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(512) NOT NULL,
    status     VARCHAR(512) NOT NULL DEFAULT '',
    deadline   VARCHAR(512) NOT NULL DEFAULT '',
    extra_data JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_contest_title_lower ON contest (LOWER(TRIM(title)));

DROP TABLE IF EXISTS contest_participant CASCADE;
