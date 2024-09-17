CREATE TABLE IF NOT EXISTS users (
    id              UUID            PRIMARY KEY,
    username        varchar(15)     NOT NULL,
    token           varchar(16)     NOT NULL,
    created         TIMESTAMP       NOT NULL,
    terminated      TIMESTAMP       NULL
);
