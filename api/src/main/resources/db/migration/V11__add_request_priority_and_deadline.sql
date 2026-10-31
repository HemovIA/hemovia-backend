/* V11: toda requisição de sangue passa a ter uma prioridade e um prazo de entrega. */

/* Acrescenta a prioridade, o prazo e a regra dos valores permitidos. */
ALTER TABLE blood_requests
    /* As requisições antigas viram MEDIUM, a prioridade neutra, em vez de uma prioridade inventada. */
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    /* O prazo é opcional no banco só por causa das linhas antigas; quem cria requisição nova é obrigado a informá-lo. */
    ADD COLUMN delivery_deadline TIMESTAMP WITH TIME ZONE,
    /* Só existem quatro prioridades no sistema. */
    ADD CONSTRAINT ck_blood_requests_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));
