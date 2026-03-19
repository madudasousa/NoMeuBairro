CREATE TABLE imageEstab (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    url VARCHAR(250) NOT NULL,
    ordem VARCHAR NOT NULL,
    estabs_id UUID,
    FOREIGN KEY (estabs_id) REFERENCES estabs(id)
);