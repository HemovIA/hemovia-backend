ALTER TABLE users
    ADD COLUMN donor_id UUID REFERENCES person(id),
    ADD CONSTRAINT uk_users_donor_id UNIQUE (donor_id),
    ADD CONSTRAINT ck_users_donor_link CHECK (donor_id IS NULL OR (role IS NOT NULL AND role = 'DOADOR')),
    DROP CONSTRAINT ck_users_role,
    DROP CONSTRAINT ck_users_role_link;

ALTER TABLE users
    ADD CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'HEMOCENTRO', 'HOSPITAL', 'TRANSPORTADOR', 'DOADOR')),
    ADD CONSTRAINT ck_users_role_link CHECK (
        role IS NULL
        OR (role IN ('ADMIN', 'DOADOR') AND hospital_id IS NULL AND blood_center_id IS NULL AND vehicle_id IS NULL)
        OR (role = 'HOSPITAL' AND hospital_id IS NOT NULL AND blood_center_id IS NULL AND vehicle_id IS NULL)
        OR (role = 'HEMOCENTRO' AND hospital_id IS NULL AND blood_center_id IS NOT NULL AND vehicle_id IS NULL)
        OR (role = 'TRANSPORTADOR' AND hospital_id IS NULL AND blood_center_id IS NULL AND vehicle_id IS NOT NULL)
    );
