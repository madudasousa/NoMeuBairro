CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE estabs(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id UUID NOT NULL,
    description VARCHAR(500) NOT NULL,
    address VARCHAR(250) NOT NULL,
    time VARCHAR(100) NOT NULL,
    phone VARCHAR NOT NULL,
    active BOOLEAN NOT NULL,
    createAt TIMESTAMP NOT NULL,
    updateAt TIMESTAMP NOT NULL
);

