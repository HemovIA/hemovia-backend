package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.CenarioDeAutenticacao;
import com.hemovia.api.application.port.output.AdminBootstrapSettings;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/*
Ciclo 20 da Seção 3: o primeiro ADMIN nasce das variáveis de ambiente, uma única vez e sem derrubar a subida.
*/
class BootstrapAdminTest {

    // Variáveis ADMIN_* de exemplo, como o deploy as informaria
    private record Configuracao(String name, String email, String cpf, String password) implements AdminBootstrapSettings { }

    private static final Configuracao PREENCHIDA = new Configuracao("Admin Inicial", "admin@hemovia.com", "529.982.247-25", "troque-esta-senha");

    private CenarioDeAutenticacao cenario;

    @BeforeEach
    void prepararCenario() {
        // Dado um sistema novo, sem nenhum usuário
        cenario = new CenarioDeAutenticacao();
    }

    @Test
    void deveCriarOPrimeiroAdminQuandoNaoExisteNenhum() {
        // Quando a aplicação sobe com as variáveis preenchidas
        cenario.bootstrapAdmin(PREENCHIDA).execute();
        // Então existe um ADMIN ativo, com a senha em hash
        assertThat(cenario.usuarios.findAll()).hasSize(1);
        User admin = cenario.usuarios.findAll().get(0);
        assertThat(admin.role()).isEqualTo(Role.ADMIN);
        assertThat(admin.isActive()).isTrue();
        assertThat(admin.email().value()).isEqualTo("admin@hemovia.com");
        assertThat(admin.passwordHash()).isEqualTo("hash:troque-esta-senha");
    }

    @Test
    void naoDeveDuplicarOAdminEmReinicios() {
        // Quando a aplicação sobe duas vezes
        cenario.bootstrapAdmin(PREENCHIDA).execute();
        cenario.bootstrapAdmin(PREENCHIDA).execute();
        // Então continua existindo um só usuário
        assertThat(cenario.usuarios.findAll()).hasSize(1);
    }

    @Test
    void naoDeveCriarAdminQuandoJaExisteUm() {
        // Dado que outro ADMIN já existe
        cenario.salvarAdmin();
        // Quando a aplicação sobe com as variáveis preenchidas
        cenario.bootstrapAdmin(PREENCHIDA).execute();
        // Então nada é criado
        assertThat(cenario.usuarios.findAll()).hasSize(1);
    }

    @Test
    void deveApenasAvisarQuandoAsVariaveisEstaoVazias() {
        // Quando a aplicação sobe sem as variáveis ADMIN_*, nada é criado e nada quebra
        Configuracao vazia = new Configuracao("", "", "", "");
        assertThatCode(() -> cenario.bootstrapAdmin(vazia).execute()).doesNotThrowAnyException();
        assertThat(cenario.usuarios.findAll()).isEmpty();
    }

    @Test
    void deveApenasAvisarQuandoOEmailJaPertenceAOutroUsuario() {
        // Dado um usuário comum que já usa o e-mail das variáveis
        cenario.salvarUsuario("admin@hemovia.com", "111.444.777-35", Role.HOSPITAL, cenario.recursos.novoHospital());
        // Quando a aplicação sobe, o bootstrap não cria nada e não derruba a subida
        assertThatCode(() -> cenario.bootstrapAdmin(PREENCHIDA).execute()).doesNotThrowAnyException();
        assertThat(cenario.usuarios.findAll()).hasSize(1);
    }
}
