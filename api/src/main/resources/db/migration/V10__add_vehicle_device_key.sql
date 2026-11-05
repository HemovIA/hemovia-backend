/* V10: o dispositivo de telemetria de cada veículo se identifica por uma chave; aqui fica só o hash dela. */

/* Acrescenta o hash da chave e garante que dois veículos nunca dividam a mesma chave. */
ALTER TABLE vehicles
    /* Opcional, porque os veículos já cadastrados ainda não têm dispositivo; o NULL não conta como repetido na unicidade. */
    ADD COLUMN device_key_hash VARCHAR(64),
    /* Uma chave identifica um único veículo, para a leitura nunca ser atribuída ao veículo errado. */
    ADD CONSTRAINT uk_vehicles_device_key_hash UNIQUE (device_key_hash);
