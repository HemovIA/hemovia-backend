package com.hemovia.api.domain.model;

import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.InvalidDataException;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclos 3 e 4 da Seção 3: o usuário só existe com o papel e o vínculo certos, e só entra quem está ativo e tem papel.
*/
class UserTest {

    // Dados de apoio usados em quase todos os testes
    private static final Email EMAIL = new Email("alice@hemovia.com");
    private static final Cpf CPF = new Cpf("529.982.247-25");

    // Cria um usuário com o papel e o vínculo pedidos
    private static User criar(Role papel, UUID vinculo) {
        return User.create("Alice Sena", EMAIL, CPF, "81999998888", "hash", papel, vinculo);
    }

    @Test
    void deveCriarAdminSemVinculoEAtivo() {
        // Quando criamos um administrador
        User usuario = criar(Role.ADMIN, null);
        // Então ele ganha id, fica ativo e não tem nenhum vínculo
        assertThat(usuario.id()).isNotNull();
        assertThat(usuario.isActive()).isTrue();
        assertThat(usuario.role()).isEqualTo(Role.ADMIN);
        assertThat(usuario.hospitalId()).isNull();
        assertThat(usuario.bloodCenterId()).isNull();
        assertThat(usuario.vehicleId()).isNull();
    }

    @Test
    void deveGuardarOVinculoNoCampoCertoDeCadaPapel() {
        // Dados três ids diferentes
        UUID hospital = UUID.randomUUID();
        UUID hemocentro = UUID.randomUUID();
        UUID veiculo = UUID.randomUUID();
        // Então cada papel guarda o vínculo só no seu campo
        assertThat(criar(Role.HOSPITAL, hospital).hospitalId()).isEqualTo(hospital);
        assertThat(criar(Role.HEMOCENTRO, hemocentro).bloodCenterId()).isEqualTo(hemocentro);
        assertThat(criar(Role.TRANSPORTADOR, veiculo).vehicleId()).isEqualTo(veiculo);
        assertThat(criar(Role.HOSPITAL, hospital).bloodCenterId()).isNull();
    }

    @Test
    void deveRejeitarPapelQueExigeVinculoSemVinculo() {
        // Hospital, hemocentro e transportador sem vínculo não fazem sentido
        for (Role papel : new Role[] {Role.HOSPITAL, Role.HEMOCENTRO, Role.TRANSPORTADOR}) {
            assertThatThrownBy(() -> criar(papel, null)).isInstanceOf(InvalidDataException.class);
        }
    }

    @Test
    void deveRejeitarAdminComVinculo() {
        // O administrador não pertence a nenhum hospital, hemocentro ou veículo
        assertThatThrownBy(() -> criar(Role.ADMIN, UUID.randomUUID())).isInstanceOf(InvalidDataException.class);
    }

    @Test
    void deveRejeitarUsuarioSemPapel() {
        // Todo usuário criado daqui em diante nasce com papel
        assertThatThrownBy(() -> criar(null, null)).isInstanceOf(InvalidDataException.class);
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        // O nome é obrigatório
        assertThatThrownBy(() -> User.create("  ", EMAIL, CPF, null, "hash", Role.ADMIN, null)).isInstanceOf(InvalidDataException.class);
    }

    @Test
    void devePermitirLoginDeUsuarioAtivoComPapel() {
        // Ativo e com papel entra
        assertThat(criar(Role.ADMIN, null).canLogIn()).isTrue();
    }

    @Test
    void deveBloquearLoginDeUsuarioDesativado() {
        // Dado um usuário desativado
        User usuario = criar(Role.ADMIN, null);
        usuario.deactivate();
        // Então ele não entra mais
        assertThat(usuario.isActive()).isFalse();
        assertThat(usuario.canLogIn()).isFalse();
    }

    @Test
    void deveBloquearLoginDeUsuarioAntigoSemPapel() {
        // Usuários que já existiam antes dos papéis foram restaurados com papel nulo
        User antigo = User.restore(UUID.randomUUID(), "Antigo", EMAIL, CPF, null, "hash", null, true, null, null, null);
        // Então eles não entram até um administrador atribuir um papel
        assertThat(antigo.canLogIn()).isFalse();
    }

    @Test
    void deveTrocarOPapelEOVinculoJuntos() {
        // Dado um usuário de hospital
        UUID hospital = UUID.randomUUID();
        UUID hemocentro = UUID.randomUUID();
        User usuario = criar(Role.HOSPITAL, hospital);
        // Quando ele passa a ser do hemocentro
        usuario.changeRole(Role.HEMOCENTRO, hemocentro);
        // Então o vínculo antigo some e o novo entra
        assertThat(usuario.role()).isEqualTo(Role.HEMOCENTRO);
        assertThat(usuario.hospitalId()).isNull();
        assertThat(usuario.bloodCenterId()).isEqualTo(hemocentro);
    }

    @Test
    void deveManterOPapelAntigoQuandoATrocaEhInvalida() {
        // Dado um administrador
        User usuario = criar(Role.ADMIN, null);
        // Quando tentamos torná-lo hospital sem vínculo, a troca é recusada
        assertThatThrownBy(() -> usuario.changeRole(Role.HOSPITAL, null)).isInstanceOf(InvalidDataException.class);
        // E ele continua administrador
        assertThat(usuario.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void deveAtualizarNomeEmailETelefone() {
        // Quando atualizamos o perfil
        User usuario = criar(Role.ADMIN, null);
        usuario.updateProfile("Alice Souza", new Email("alice.souza@hemovia.com"), " 81988887777 ");
        // Então os dados novos ficam, com o telefone já aparado
        assertThat(usuario.name()).isEqualTo("Alice Souza");
        assertThat(usuario.email().value()).isEqualTo("alice.souza@hemovia.com");
        assertThat(usuario.phoneNumber()).isEqualTo("81988887777");
    }
}
