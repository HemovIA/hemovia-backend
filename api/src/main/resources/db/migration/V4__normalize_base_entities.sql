-- Normalizes the legacy donation/bag relationship without inventing missing origins.
-- Flyway executes PostgreSQL migrations transactionally: every preflight failure rolls
-- back this complete version before the legacy shape is changed.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM blood_bags
        WHERE rh_factor NOT IN ('POSITIVE', 'NEGATIVE')
           OR component NOT IN ('WHOLE_BLOOD', 'RED_BLOOD_CELLS', 'PLASMA', 'PLATELETS', 'CRYOPRECIPITATE')
           OR blood_type_enum NOT IN ('A', 'B', 'AB', 'O')
           OR status NOT IN ('AVAILABLE', 'RESERVED', 'IN_TRANSIT', 'DELIVERED', 'DISCARDED')
    ) THEN
        RAISE EXCEPTION 'V4 preflight failed: blood_bags contains an unknown enum code';
    END IF;

    IF EXISTS (SELECT 1 FROM blood_bags WHERE expiration_date < collection_date) THEN
        RAISE EXCEPTION 'V4 preflight failed: blood_bags expiration precedes collection';
    END IF;

    IF EXISTS (
        SELECT 1 FROM donations
        WHERE blood_bag_id IS NOT NULL
        GROUP BY blood_bag_id
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'V4 preflight failed: a legacy blood bag is linked to multiple donations';
    END IF;

    IF EXISTS (
        SELECT 1 FROM hospital
        WHERE latitude NOT BETWEEN -90 AND 90
           OR longitude NOT BETWEEN -180 AND 180
           OR priority NOT BETWEEN 1 AND 5
    ) THEN
        RAISE EXCEPTION 'V4 preflight failed: hospital contains values outside its domain';
    END IF;
END $$;

CREATE TABLE blood_centers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    CONSTRAINT ck_blood_centers_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_blood_centers_longitude CHECK (longitude BETWEEN -180 AND 180)
);

CREATE TABLE vehicles (
    id UUID PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL,
    CONSTRAINT uk_vehicles_code UNIQUE (code),
    CONSTRAINT ck_vehicles_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT ck_vehicles_capacity_positive CHECK (capacity > 0)
);

ALTER TABLE hospital
    ADD CONSTRAINT ck_hospital_latitude CHECK (latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT ck_hospital_longitude CHECK (longitude BETWEEN -180 AND 180),
    ADD CONSTRAINT ck_hospital_priority CHECK (priority BETWEEN 1 AND 5);

ALTER TABLE donations
    ADD COLUMN donor_id UUID,
    ADD COLUMN blood_center_id UUID,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE blood_bags
    ADD COLUMN volume_ml NUMERIC(12,3),
    ADD COLUMN blood_center_id UUID,
    ADD COLUMN donation_id UUID,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- The old column is the only authoritative source for this backfill.  The
-- ambiguity preflight above makes this update one-to-one rather than arbitrary.
UPDATE blood_bags b
SET donation_id = d.id
FROM donations d
WHERE d.blood_bag_id = b.id;

ALTER TABLE donations
    ADD CONSTRAINT fk_donations_donor FOREIGN KEY (donor_id) REFERENCES person(id),
    ADD CONSTRAINT fk_donations_blood_center FOREIGN KEY (blood_center_id) REFERENCES blood_centers(id);

ALTER TABLE blood_bags
    ADD CONSTRAINT fk_blood_bags_blood_center FOREIGN KEY (blood_center_id) REFERENCES blood_centers(id),
    ADD CONSTRAINT fk_blood_bags_donation FOREIGN KEY (donation_id) REFERENCES donations(id),
    ADD CONSTRAINT ck_blood_bags_rh_factor CHECK (rh_factor IN ('POSITIVE', 'NEGATIVE')),
    ADD CONSTRAINT ck_blood_bags_component CHECK (component IN ('WHOLE_BLOOD', 'RED_BLOOD_CELLS', 'PLASMA', 'PLATELETS', 'CRYOPRECIPITATE')),
    ADD CONSTRAINT ck_blood_bags_type CHECK (blood_type_enum IN ('A', 'B', 'AB', 'O')),
    ADD CONSTRAINT ck_blood_bags_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'IN_TRANSIT', 'DELIVERED', 'DISCARDED')),
    ADD CONSTRAINT ck_blood_bags_dates CHECK (expiration_date >= collection_date),
    ADD CONSTRAINT ck_blood_bags_volume CHECK (volume_ml IS NULL OR (volume_ml > 0 AND volume_ml NOT IN ('NaN'::numeric, 'Infinity'::numeric, '-Infinity'::numeric)));

ALTER TABLE donations DROP CONSTRAINT fk_donation_bloodbag;
ALTER TABLE donations DROP COLUMN blood_bag_id;

CREATE INDEX ix_blood_bags_donation_id ON blood_bags(donation_id);
CREATE INDEX ix_blood_bags_donor_id ON blood_bags(donor_id);
CREATE INDEX ix_blood_bags_blood_center_id ON blood_bags(blood_center_id);
