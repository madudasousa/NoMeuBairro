CREATE TABLE imageEstab (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    nome_arquivo VARCHAR(255),
    content_type VARCHAR(50),
    ordem INTEGER NOT NULL,
    estabs_id UUID NOT NULL,
    FOREIGN KEY (estabs_id) REFERENCES estabs(id) ON DELETE CASCADE
);