/* V13: cada leitura de telemetria passa a registrar onde o veículo estava. */

/* Acrescenta a posição e as regras de coordenadas válidas. */
ALTER TABLE telemetry_readings
    /* Latitude da leitura; opcional porque as leituras antigas não têm posição (quem envia leitura nova é obrigado a informá-la). */
    ADD COLUMN latitude DOUBLE PRECISION,
    /* Longitude da leitura, pelo mesmo motivo. */
    ADD COLUMN longitude DOUBLE PRECISION,
    /* A latitude vai de -90 a 90. */
    ADD CONSTRAINT ck_telemetry_latitude CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    /* A longitude vai de -180 a 180. */
    ADD CONSTRAINT ck_telemetry_longitude CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180);
