CREATE TABLE users(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(150) NOT NULL,
    document   VARCHAR(14) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    perfil      VARCHAR(20) NOT NULL,
    created   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_document ON users (document);
