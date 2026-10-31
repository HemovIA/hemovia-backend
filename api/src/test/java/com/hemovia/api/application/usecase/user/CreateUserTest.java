package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.CenarioDeAutenticacao;
import com.hemovia.api.application.dto.request.CreateUserRequest;
import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.DuplicateResourceException;
import com.hemovia.api.domain.exception.InvalidDataException;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclos 15 a 17 da Seção 3: o ADMIN cria usuários com papel e vínculo válidos, sem duplicar e sem expor a senha.
*/
class CreateUserTest {

    private CenarioDeAutenticacao cenario;

    @BeforeEach
    void prepararCenario() {
        // Dado um cenário com uma administradora já cadastrada
        cenario = new CenarioDeAutenticacao();
        cenario.salvarAdmin();
    }

    // Pedido de criação de um usuário novo, com os dados de papel e vínculo informados
    private CreateUserRequest pedido(String email, String cpf, Role papel, UUID hospital, UUID hemocentro, UUID veiculo) {
        return new CreateUserRequest("Bruno Lima", email, cpf, "81988887777", "senhaForte1", papel, hospital, hemocentro, veiculo);
    }

    @Test
    void deveCriarUsuarioDeHospitalComHospitalExistente() {
        // Dado um hospital cadastrado
        UUID hospital = cenario.recursos.novoHospital();
        // Quando o ADMIN cria um usuário desse hospital
        UserResponse resposta = cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "111.444.777-35", Role.HOSPITAL, hospital, null, null));
        // Então o usuário nasce ativo, com o papel e o vínculo certos, e os dados normalizados
        assertThat(resposta.role()).isEqualTo(Role.HOSPITAL);
        assertThat(resposta.hospitalId()).isEqualTo(hospital);
        assertThat(resposta.active()).isTrue();
        assertThat(resposta.cpf()).isEqualTo("11144477735");
        assertThat(cenario.usuarios.findAll()).hasSize(2);
    }

    @Test
    void deveCriarOutroAdminSemVinculo() {
        UserResponse resposta = cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "111.444.777-35", Role.ADMIN, null, null, null));
        assertThat(resposta.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void deveGuardarASenhaComHashEnuncaExporNaResposta() {
        // Quando o usuário é criado
        UserResponse resposta = cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "111.444.777-35", Role.ADMIN, null, null, null));
        // Então o que foi guardado é o hash da senha, nunca a senha pura
        String guardada = cenario.usuarios.findById(resposta.id()).orElseThrow().passwordHash();
        assertThat(guardada).isEqualTo("hash:senhaForte1");
        // E a resposta não tem nenhum campo de senha
        assertThat(Arrays.stream(UserResponse.class.getRecordComponents()).map(RecordComponent::getName))
                .noneMatch(nome -> nome.toLowerCase().contains("password"));
    }

    @Test
    void deveRecusarEmailRepetidoMesmoComOutraGrafia() {
        // Dado que alice@hemovia.com já existe, outra pessoa não pode usar o mesmo e-mail, nem com maiúsculas
        assertThatThrownBy(() -> cenario.criarUsuario().execute(
                pedido("ALICE@Hemovia.com", "111.444.777-35", Role.ADMIN, null, null, null)))
                .isInstanceOf(DuplicateResourceException.class).hasMessage("E-mail já cadastrado");
        assertThat(cenario.usuarios.findAll()).hasSize(1);
    }

    @Test
    void deveRecusarCpfRepetidoMesmoComMascaraDiferente() {
        // O CPF 529.982.247-25 já é da Alice, com ou sem máscara
        assertThatThrownBy(() -> cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "52998224725", Role.ADMIN, null, null, null)))
                .isInstanceOf(DuplicateResourceException.class).hasMessage("CPF já cadastrado");
        assertThat(cenario.usuarios.findAll()).hasSize(1);
    }

    @Test
    void deveRecusarCpfInvalido() {
        assertThatThrownBy(() -> cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "123.456.789-00", Role.ADMIN, null, null, null)))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    void deveRecusarHospitalInexistente() {
        // Um hospital que ninguém cadastrou não pode ser vínculo
        UUID fantasma = UUID.randomUUID();
        assertThatThrownBy(() -> cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "111.444.777-35", Role.HOSPITAL, fantasma, null, null)))
                .isInstanceOf(ResourceNotFoundException.class).hasMessage("Hospital não encontrado: " + fantasma);
    }

    @Test
    void deveRecusarHemocentroEVeiculoInexistentes() {
        UUID fantasma = UUID.randomUUID();
        assertThatThrownBy(() -> cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "111.444.777-35", Role.HEMOCENTRO, null, fantasma, null)))
                .isInstanceOf(ResourceNotFoundException.class).hasMessage("Hemocentro não encontrado: " + fantasma);
        assertThatThrownBy(() -> cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "111.444.777-35", Role.TRANSPORTADOR, null, null, fantasma)))
                .isInstanceOf(ResourceNotFoundException.class).hasMessage("Veículo não encontrado: " + fantasma);
    }

    @Test
    void deveRecusarPapelQueExigeVinculoSemVinculo() {
        assertThatThrownBy(() -> cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "111.444.777-35", Role.HOSPITAL, null, null, null)))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    void deveRecusarAdminComVinculo() {
        UUID hospital = cenario.recursos.novoHospital();
        assertThatThrownBy(() -> cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "111.444.777-35", Role.ADMIN, hospital, null, null)))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    void deveRecusarVinculoQueNaoCombinaComOPapel() {
        // Usuário de hospital com um veículo informado junto é ambíguo e é recusado
        UUID hospital = cenario.recursos.novoHospital();
        UUID veiculo = cenario.recursos.novoVeiculo();
        assertThatThrownBy(() -> cenario.criarUsuario().execute(
                pedido("bruno@hemovia.com", "111.444.777-35", Role.HOSPITAL, hospital, null, veiculo)))
                .isInstanceOf(InvalidDataException.class);
    }
}
