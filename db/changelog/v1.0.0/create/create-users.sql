CREATE TABLE IF NOT EXISTS users
(
    id                         BIGSERIAL PRIMARY KEY,
    email                      VARCHAR(256) UNIQUE NOT NULL,
    password                   TEXT                NOT NULL,

    -- Подтверждение через код
    email_verified             BOOLEAN   DEFAULT FALSE,
    verification_code          VARCHAR(6),          -- 6-значный код
    verification_code_expiry   TIMESTAMP,           -- когда истекает
    verification_attempts_left SMALLINT  DEFAULT 3, -- защита от подбора

    -- Остальные поля профиля
    last_name                  VARCHAR(128),
    first_name                 VARCHAR(128),
    middle_name                VARCHAR(128),
    phone_number               VARCHAR(128) UNIQUE,
    year_of_admission          SMALLINT,
    status_id                  SMALLINT REFERENCES status (id),
    region_id                  SMALLINT REFERENCES region (id),

    profile_completed          BOOLEAN   DEFAULT FALSE,
    registered_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);