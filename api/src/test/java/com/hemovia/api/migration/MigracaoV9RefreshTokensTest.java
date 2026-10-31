package com.hemovia.api.migration;

import com.hemovia.api.support.BancoDeMigracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/*
Ciclo 2 da Seção 2, migration V9: tabela de refresh tokens.
Só o hash do token fica no banco, nunca o token puro, e cada hash é único.
Roda contra um PostgreSQL real, num schema isolado, só até a V9.
*/
@Tag("integration")
class MigracaoV9RefreshTokensTest {

    // Datas fixas, para o teste não depender do relógio
    private static final String CRIADO_EM = "2026-08-11 12:00:00+00";
    private static final String EXPIRA_EM = "2026-08-18 12:00:00+00";

    // Banco isolado deste teste
    private BancoDeMigracao banco;

    // Usuário dono dos tokens
    private UUID usuario;

    @BeforeEach
    void prepararBanco() throws SQLException {
        // Dado um banco vazio com as migrations aplicadas até a V9 e um usuário para ser o dono dos tokens
        banco = new BancoDeMigracao();
        banco.migrarAte("9");
        usuario = banco.criarUsuarioAntigo();
    }

    @AfterEach
    void apagarBanco() throws SQLException {
        // No fim, o schema do teste é apagado
        banco.close();
    }

    // Um hash de 64 caracteres, como o SHA-256 em hexadecimal, repetindo a letra recebida
    private static String hash(char letra) {
        return String.valueOf(letra).repeat(64);
    }

    // Insere um token e devolve o id; as datas chegam como texto e o banco converte
    private UUID inserirToken(UUID dono, String hash, String criadoEm, String expiraEm) throws SQLException {
        UUID id = UUID.randomUUID();
        banco.executar("INSERT INTO refresh_tokens (id, user_id, family_id, token_hash, created_at, expires_at) "
                + "VALUES (?, ?, ?, ?, CAST(? AS TIMESTAMPTZ), CAST(? AS TIMESTAMPTZ))",
                id, dono, UUID.randomUUID(), hash, criadoEm, expiraEm);
        return id;
    }

    @Test
    void deveAceitarTokenValido() {
        // Um token com hash de 64 caracteres e validade depois da criação entra normalmente
        assertThatCode(() -> inserirToken(usuario, hash('a'), CRIADO_EM, EXPIRA_EM)).doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarHashRepetido() throws SQLException {
        // Dado um token já gravado
        inserirToken(usuario, hash('a'), CRIADO_EM, EXPIRA_EM);
        // Então outro com o mesmo hash é recusado
        banco.exigirRestricao("uk_refresh_tokens_hash", () -> inserirToken(usuario, hash('a'), CRIADO_EM, EXPIRA_EM));
    }

    @Test
    void deveRejeitarHashMaiorQue64Caracteres() {
        // O hash cabe em 64 caracteres, então um texto maior (um token puro, por exemplo) é recusado
        banco.exigirRejeicao("character varying(64)", () -> inserirToken(usuario, "a".repeat(65), CRIADO_EM, EXPIRA_EM));
    }

    @Test
    void deveRejeitarValidadeIgualOuAnteriorACriacao() {
        // Um token que já nasce vencido (validade igual à criação) é recusado
        banco.exigirRestricao("ck_refresh_tokens_expiry", () -> inserirToken(usuario, hash('b'), CRIADO_EM, CRIADO_EM));
    }

    @Test
    void deveRejeitarTokenDeUsuarioQueNaoExiste() {
        // Token sem dono real é recusado pela chave estrangeira
        banco.exigirRestricao("refresh_tokens_user_id_fkey", () -> inserirToken(UUID.randomUUID(), hash('c'), CRIADO_EM, EXPIRA_EM));
    }

    @Test
    void deveAceitarTokenSubstituidoPorOutroDaMesmaTabela() throws SQLException {
        // Dado um token antigo e o novo que o substitui na rotação
        UUID antigo = inserirToken(usuario, hash('d'), CRIADO_EM, EXPIRA_EM);
        UUID novo = inserirToken(usuario, hash('e'), CRIADO_EM, EXPIRA_EM);
        // Quando marcamos o antigo como substituído pelo novo
        banco.executar("UPDATE refresh_tokens SET replaced_by = ? WHERE id = ?", novo, antigo);
        // Então a ligação fica gravada
        assertThat(banco.valor("SELECT replaced_by FROM refresh_tokens WHERE id = ?", antigo)).isEqualTo(novo);
    }

    @Test
    void deveRejeitarSubstitutoQueNaoExiste() throws SQLException {
        // Dado um token qualquer
        UUID token = inserirToken(usuario, hash('f'), CRIADO_EM, EXPIRA_EM);
        // Então apontar o substituto para um id inexistente é recusado
        banco.exigirRestricao("refresh_tokens_replaced_by_fkey",
                () -> banco.executar("UPDATE refresh_tokens SET replaced_by = ? WHERE id = ?", UUID.randomUUID(), token));
    }

    @Test
    void deveCriarIndicesDeUsuarioEFamilia() throws SQLException {
        // As consultas por usuário e por família de tokens têm índice próprio
        banco.exigirIndice("ix_refresh_tokens_user_id");
        banco.exigirIndice("ix_refresh_tokens_family_id");
    }
}
