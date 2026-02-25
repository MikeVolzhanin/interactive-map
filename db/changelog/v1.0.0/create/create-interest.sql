CREATE TABLE IF NOT EXISTS interest
(
    id BIGSERIAL PRIMARY KEY,
    name_of_interest VARCHAR(128) NOT NULL UNIQUE,
    description TEXT NOT NULL
);