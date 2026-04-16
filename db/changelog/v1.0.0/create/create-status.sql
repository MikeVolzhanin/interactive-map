CREATE TABLE IF NOT EXISTS status
(
    id                 SERIAL PRIMARY KEY,
    level_of_education VARCHAR(128) NOT NULL UNIQUE
);