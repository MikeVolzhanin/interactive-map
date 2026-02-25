CREATE TABLE IF NOT EXISTS users_interest
(
    id BIGSERIAL PRIMARY KEY,
    users_id BIGINT REFERENCES users(id),
    interest_id SMALLINT REFERENCES interest(id)
);