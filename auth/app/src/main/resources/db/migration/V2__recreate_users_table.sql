TRUNCATE TABLE users;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    id              UUID            PRIMARY KEY,
    username        varchar(16)     NOT NULL,
    token           varchar(16)     NOT NULL,
    state           varchar(32)     NOT NULL,
    created_date    TIMESTAMP       NOT NULL,
    updated_date    TIMESTAMP       NOT NULL
);
