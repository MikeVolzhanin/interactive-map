CREATE TABLE IF NOT EXISTS users
(
    id BIGSERIAL PRIMARY KEY,
    last_name VARCHAR(128) NOT NULL,
    first_name VARCHAR(128) NOT NULL,
    middle_name VARCHAR(128),
    email VARCHAR(256) UNIQUE NOT NULL,
    phone_number VARCHAR(128) NOT NULL,
    year_of_admission SMALLINT NOT NULL CHECK (year_of_admission > 0),
    status_id SMALLINT REFERENCES status(id) NOT NULL,
    region_id SMALLINT REFERENCES region(id) NOT NULL
);