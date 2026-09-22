CREATE TABLE person (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL
);

CREATE TABLE blood_bags (
    id UUID PRIMARY KEY,
    rh_factor VARCHAR(255) NOT NULL,
    component VARCHAR(255) NOT NULL,
    collection_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    status VARCHAR(255) NOT NULL,
    blood_type_enum VARCHAR(255) NOT NULL,
    donor_id UUID,
    CONSTRAINT fk_bloodbag_donor FOREIGN KEY (donor_id) REFERENCES person(id)
);

CREATE TABLE donations (
    id UUID PRIMARY KEY,
    collection_date DATE NOT NULL,
    collection_center VARCHAR(255) NOT NULL,
    blood_bag_id UUID,
    CONSTRAINT fk_donation_bloodbag FOREIGN KEY (blood_bag_id) REFERENCES blood_bags(id)
);
