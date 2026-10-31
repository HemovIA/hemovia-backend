/* V9: guarda os refresh tokens. Só o hash do token fica no banco, nunca o token puro. */

/* Cria a tabela; cada linha é um token emitido para um usuário. */
CREATE TABLE refresh_tokens (
    /* Identificador do token. */
    id UUID PRIMARY KEY,
    /* Dono do token. */
    user_id UUID NOT NULL REFERENCES users(id),
    /* Todos os tokens de uma mesma sessão compartilham a família, para revogar a sessão inteira se um token for reutilizado. */
    family_id UUID NOT NULL,
    /* SHA-256 do token em hexadecimal, que tem 64 caracteres. */
    token_hash VARCHAR(64) NOT NULL,
    /* Quando o token foi emitido. */
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    /* Quando o token deixa de valer. */
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    /* Preenchido quando o token é revogado, por logout ou por suspeita de roubo. */
    revoked_at TIMESTAMP WITH TIME ZONE,
    /* Token que tomou o lugar deste na rotação. */
    replaced_by UUID REFERENCES refresh_tokens(id),
    /* Dois tokens nunca têm o mesmo hash. */
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    /* Um token não pode nascer vencido. */
    CONSTRAINT ck_refresh_tokens_expiry CHECK (expires_at > created_at)
);

/* Acelera a busca dos tokens de um usuário, usada no logout geral. */
CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens(user_id);

/* Acelera a revogação de uma família inteira de tokens. */
CREATE INDEX ix_refresh_tokens_family_id ON refresh_tokens(family_id);
