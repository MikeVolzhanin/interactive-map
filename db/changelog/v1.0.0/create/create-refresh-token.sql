CREATE TABLE IF NOT EXISTS refresh_token
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     INT REFERENCES users (id),
    token       TEXT,
    expiry_date TIMESTAMP
)