CREATE TABLE IF NOT EXISTS contest_participant
(
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(256)  NOT NULL,
    contest_name VARCHAR(512)  NOT NULL,
    extra_data   JSONB         NOT NULL DEFAULT '{}'::jsonb,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_contest_participant_email_contest UNIQUE (email, contest_name)
);

CREATE INDEX IF NOT EXISTS idx_contest_participant_contest_name ON contest_participant (contest_name);
