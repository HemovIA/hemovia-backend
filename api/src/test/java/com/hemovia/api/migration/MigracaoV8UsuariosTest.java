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
Ciclo 2 da Seção 2, migration V8: papéis e vínculos dos usuários.
Cada papel só pode existir com o vínculo certo (hospital, hemocentro ou veículo), e o banco é quem garante.
Roda contra um PostgreSQL real, num schema isolado, só até a V8.
*/
@Tag("integration")
class MigracaoV8UsuariosTest {

    // Banco isolado deste teste
    private BancoDeMigracao banco;

    @BeforeEach
    void prepararBanco() throws SQLException {
        // Dado um banco vazio com as migrations aplicadas até a V8
        banco = new BancoDeMigracao();
        banco.migrarAte("8");
    }

    @AfterEach
    void apagarBanco() throws SQLException {
        // E, no fim, o schema do teste é apagado
        banco.close();
    }

    // Insere um usuário com o papel e os vínculos informados; vínculo nulo vira NULL no banco
    private void inserirUsuario(String papel, UUID hospital, UUID hemocentro, UUID veiculo) throws SQLException {
        UUID id = UUID.randomUUID();
        banco.executar("INSERT INTO users (id, name, email, cpf, password, role, hospital_id, blood_center_id, vehicle_id) "
                + "VALUES (?, 'Pessoa Teste', ?, ?, 'hash', ?, ?, ?, ?)",
                id, id + "@teste.com", id.toString().substring(0, 14), papel, hospital, hemocentro, veiculo);
    }

    @Test
    void deveManterUsuarioSemPapelEAtivoPorPadrao() throws SQLException {
        // Quando criamos um usuário no formato antigo, sem informar papel nem situação
        UUID id = banco.criarUsuarioAntigo();
        // Então ele continua válido, sem papel (não inventamos papel para quem já existe) e ativo
        assertThat(banco.valor("SELECT role FROM users WHERE id = ?", id)).isNull();
        assertThat(banco.valor("SELECT active FROM users WHERE id = ?", id)).isEqualTo(true);
    }

    @Test
    void deveAceitarAdminSemNenhumVinculo() {
        // Um administrador não pertence a hospital, hemocentro nem veículo
        assertThatCode(() -> inserirUsuario("ADMIN", null, null, null)).doesNotThrowAnyException();
    }

    @Test
    void deveAceitarHemocentroComHemocentroId() throws SQLException {
        // Dado um hemocentro existente
        UUID hemocentro = banco.criarHemocentro();
        // Então o papel HEMOCENTRO é aceito quando aponta para ele
        assertThatCode(() -> inserirUsuario("HEMOCENTRO", null, hemocentro, null)).doesNotThrowAnyException();
    }

    @Test
    void deveAceitarHospitalComHospitalId() throws SQLException {
        // Dado um hospital existente
        UUID hospital = banco.criarHospital();
        // Então o papel HOSPITAL é aceito quando aponta para ele
        assertThatCode(() -> inserirUsuario("HOSPITAL", hospital, null, null)).doesNotThrowAnyException();
    }

    @Test
    void deveAceitarTransportadorComVeiculoId() throws SQLException {
        // Dado um veículo existente
        UUID veiculo = banco.criarVeiculo();
        // Então o papel TRANSPORTADOR é aceito quando aponta para ele
        assertThatCode(() -> inserirUsuario("TRANSPORTADOR", null, null, veiculo)).doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarHospitalSemHospitalId() {
        // Um usuário de hospital sem hospital não faz sentido, e o banco recusa
        banco.exigirRestricao("ck_users_role_link", () -> inserirUsuario("HOSPITAL", null, null, null));
    }

    @Test
    void deveRejeitarHospitalComOutroVinculoJunto() throws SQLException {
        // Dado um hospital e um hemocentro
        UUID hospital = banco.criarHospital();
        UUID hemocentro = banco.criarHemocentro();
        // Então o usuário de hospital não pode ter também hemocentro
        banco.exigirRestricao("ck_users_role_link", () -> inserirUsuario("HOSPITAL", hospital, hemocentro, null));
    }

    @Test
    void deveRejeitarAdminComVinculo() throws SQLException {
        // Dado um hospital existente
        UUID hospital = banco.criarHospital();
        // Então o administrador não pode estar preso a ele
        banco.exigirRestricao("ck_users_role_link", () -> inserirUsuario("ADMIN", hospital, null, null));
    }

    @Test
    void deveRejeitarHemocentroSemHemocentroId() {
        // Usuário de hemocentro sem hemocentro é recusado
        banco.exigirRestricao("ck_users_role_link", () -> inserirUsuario("HEMOCENTRO", null, null, null));
    }

    @Test
    void deveRejeitarTransportadorSemVeiculoId() {
        // Transportador sem veículo é recusado
        banco.exigirRestricao("ck_users_role_link", () -> inserirUsuario("TRANSPORTADOR", null, null, null));
    }

    @Test
    void deveRejeitarPapelDesconhecido() {
        // GERENTE não está entre os quatro papéis permitidos
        banco.exigirRestricao("ck_users_role", () -> inserirUsuario("GERENTE", null, null, null));
    }

    @Test
    void deveRejeitarVinculoParaHospitalQueNaoExiste() {
        // Um hospital_id solto, sem linha em hospital, é recusado pela chave estrangeira
        banco.exigirRestricao("users_hospital_id_fkey", () -> inserirUsuario("HOSPITAL", UUID.randomUUID(), null, null));
    }
}
