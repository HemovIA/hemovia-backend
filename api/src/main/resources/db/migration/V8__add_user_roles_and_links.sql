/* V8: dá a cada usuário um papel e, conforme o papel, um vínculo com hospital, hemocentro ou veículo. */

/* Acrescenta as colunas novas e as regras que ligam cada papel ao seu vínculo. */
ALTER TABLE users
    /* O papel é opcional só para não inventar papel de quem já existe; sem papel, o acesso é negado até um ADMIN atribuir um. */
    ADD COLUMN role VARCHAR(20),
    /* Usuário desativado não entra no sistema; todos os antigos continuam ativos. */
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
    /* Hospital a que o usuário pertence, quando o papel é HOSPITAL. */
    ADD COLUMN hospital_id UUID REFERENCES hospital(id),
    /* Hemocentro a que o usuário pertence, quando o papel é HEMOCENTRO. */
    ADD COLUMN blood_center_id UUID REFERENCES blood_centers(id),
    /* Veículo que o usuário conduz, quando o papel é TRANSPORTADOR. */
    ADD COLUMN vehicle_id UUID REFERENCES vehicles(id),
    /* Só existem quatro papéis no sistema. */
    ADD CONSTRAINT ck_users_role CHECK (role IS NULL OR role IN ('ADMIN', 'HEMOCENTRO', 'HOSPITAL', 'TRANSPORTADOR')),
    /* Cada papel exige exatamente o seu vínculo e proíbe os outros dois; usuário sem papel não tem regra. */
    ADD CONSTRAINT ck_users_role_link CHECK (
        role IS NULL
        OR (role = 'ADMIN'         AND hospital_id IS NULL     AND blood_center_id IS NULL     AND vehicle_id IS NULL)
        OR (role = 'HEMOCENTRO'    AND hospital_id IS NULL     AND blood_center_id IS NOT NULL AND vehicle_id IS NULL)
        OR (role = 'HOSPITAL'      AND hospital_id IS NOT NULL AND blood_center_id IS NULL     AND vehicle_id IS NULL)
        OR (role = 'TRANSPORTADOR' AND hospital_id IS NULL     AND blood_center_id IS NULL     AND vehicle_id IS NOT NULL)
    );
