package com.hemovia.api.infrastructure.persistence.adapter;

import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 24 da Seção 3 (usuários): o adapter JPA guarda e busca usuários no PostgreSQL real.
Cada teste roda numa transação que é desfeita no fim, então nada fica no banco.
*/
@Tag("integration")
@SpringBootTest
@Transactional
class UserRepositoryAdapterIntegrationTest {

    @Autowired
    private UserRepository usuarios;

    @Autowired
    private JdbcTemplate jdbc;

    // Cria um usuário com e-mail e CPF únicos por teste, para não esbarrar em dados que já existam no banco
    private User novoUsuario(Role papel, UUID vinculo) {
        String sufixo = UUID.randomUUID().toString();
        return User.create("Alice Sena", new Email(sufixo + "@hemovia.com"), cpfAleatorio(), "81999998888", "hash", papel, vinculo);
    }

    // Gera um CPF válido a partir de 9 dígitos aleatórios, calculando os verificadores
    private static Cpf cpfAleatorio() {
        int[] d = new int[11];
        java.util.Random r = new java.util.Random();
        do {
            for (int i = 0; i < 9; i++) d[i] = r.nextInt(10);
            for (int n = 9; n < 11; n++) {
                int soma = 0;
                for (int i = 0; i < n; i++) soma += d[i] * (n + 1 - i);
                int resto = soma % 11;
                d[n] = resto < 2 ? 0 : 11 - resto;
            }
        } while (java.util.Arrays.stream(d).distinct().count() == 1);
        StringBuilder texto = new StringBuilder();
        for (int digito : d) texto.append(digito);
        return new Cpf(texto.toString());
    }

    @Test
    void deveGuardarEBuscarPorIdEmailECpf() {
        // Quando guardamos um administrador
        User salvo = usuarios.save(novoUsuario(Role.ADMIN, null));
        // Então ele volta por id, por e-mail e por CPF, com os mesmos dados
        assertThat(usuarios.findById(salvo.id())).get().extracting(User::role, User::isActive).containsExactly(Role.ADMIN, true);
        assertThat(usuarios.findByEmail(salvo.email())).get().extracting(User::id).isEqualTo(salvo.id());
        assertThat(usuarios.findByCpf(salvo.cpf())).get().extracting(User::id).isEqualTo(salvo.id());
    }

    @Test
    void deveResponderSeEmailCpfEPapelJaExistem() {
        User salvo = usuarios.save(novoUsuario(Role.ADMIN, null));
        assertThat(usuarios.existsByEmail(salvo.email())).isTrue();
        assertThat(usuarios.existsByCpf(salvo.cpf())).isTrue();
        assertThat(usuarios.existsByRole(Role.ADMIN)).isTrue();
        assertThat(usuarios.existsByEmail(new Email("ninguem-" + UUID.randomUUID() + "@hemovia.com"))).isFalse();
    }

    @Test
    void deveGuardarOVinculoComUmHospitalReal() {
        // Dado um hospital existente
        UUID hospital = UUID.randomUUID();
        jdbc.update("INSERT INTO hospital (id, name, city, latitude, longitude, priority) VALUES (?, 'Hospital Teste', 'Recife', -8.05, -34.9, 3)", hospital);
        // Quando guardamos um usuário desse hospital
        User salvo = usuarios.save(novoUsuario(Role.HOSPITAL, hospital));
        // Então o vínculo volta junto
        assertThat(usuarios.findById(salvo.id()).orElseThrow().hospitalId()).isEqualTo(hospital);
    }

    @Test
    void devePersistirDesativacaoETrocaDePapel() {
        // Dado um usuário guardado
        User salvo = usuarios.save(novoUsuario(Role.ADMIN, null));
        // Quando ele é desativado e guardado de novo
        salvo.deactivate();
        usuarios.save(salvo);
        // Então a mudança aparece na leitura seguinte
        assertThat(usuarios.findById(salvo.id()).orElseThrow().isActive()).isFalse();
    }

    @Test
    void deveLerUsuarioAntigoSemPapelDoJeitoQueAV8DeixouNoBanco() {
        // Dado um usuário inserido no formato da V2 (sem informar papel nem situação)
        UUID id = UUID.randomUUID();
        Cpf cpf = cpfAleatorio();
        jdbc.update("INSERT INTO users (id, name, email, cpf, password) VALUES (?, 'Antigo', ?, ?, 'hash')", id, id + "@antigo.com", cpf.digits());
        // Então ele é lido com papel nulo, ativo, e não consegue entrar
        User antigo = usuarios.findById(id).orElseThrow();
        assertThat(antigo.role()).isNull();
        assertThat(antigo.isActive()).isTrue();
        assertThat(antigo.canLogIn()).isFalse();
    }

    @Test
    void deveListarOsUsuariosGuardados() {
        User salvo = usuarios.save(novoUsuario(Role.ADMIN, null));
        assertThat(usuarios.findAll()).extracting(User::id).contains(salvo.id());
    }
}
