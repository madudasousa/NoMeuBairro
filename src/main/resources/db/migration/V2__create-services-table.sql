CREATE TABLE services (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    estabs_id UUID,
    FOREIGN KEY (estabs_id) REFERENCES estabs(id)
);