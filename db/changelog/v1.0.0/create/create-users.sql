CREATE TABLE IF NOT EXISTS users
(
    id BIGSERIAL PRIMARY KEY,
    last_name VARCHAR(128),
    first_name VARCHAR(128),
    middle_name VARCHAR(128),
    email VARCHAR(256) UNIQUE,
    phone_number VARCHAR(128),
    year_of_admission SMALLINT CHECK (year_of_admission > 0),
    status_id SMALLINT REFERENCES status(id),
    region_id SMALLINT REFERENCES region(id)
);