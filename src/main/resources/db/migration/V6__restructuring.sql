-- V6: Reestruturação do sistema de ativação e vínculo user → estab

-- 1. Adiciona as duas colunas de controle de ativação na tabela estabs
--    active_owner = controlado pelo dono (pode ligar/desligar a qualquer momento)
--    active_admin = controlado pelo ADM (soberano — só ADM pode reverter)
ALTER TABLE estabs ADD COLUMN active_owner BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE estabs ADD COLUMN active_admin BOOLEAN NOT NULL DEFAULT TRUE;

-- 2. Migra dados da coluna antiga para as duas novas antes de removê-la
UPDATE estabs SET active_owner = active, active_admin = active;

-- 3. Remove a coluna antiga — substituída pelas duas acima
ALTER TABLE estabs DROP COLUMN active;

-- 4. Vincula users ao estab — nullable porque ADM não tem estabelecimento
ALTER TABLE users ADD COLUMN estab_id UUID REFERENCES estabs(id) ON DELETE SET NULL;

-- 5. Adiciona enum ADM ao perfil (caso use CHECK constraint no banco)
-- Se não tiver constraint, pode ignorar essa linha
--ALTER TABLE users DROP COLUMN perfil;

--PERFIL ADM
-- document '00000000000',
--  senha "admin123"
