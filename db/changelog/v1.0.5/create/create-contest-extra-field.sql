CREATE TABLE IF NOT EXISTS contest_extra_field
(
    id          BIGSERIAL PRIMARY KEY,
    field_key   VARCHAR(128) NOT NULL UNIQUE,
    field_label VARCHAR(256) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_contest_extra_field_label ON contest_extra_field (field_label);
