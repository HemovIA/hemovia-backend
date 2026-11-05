package com.hemovia.api.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hemovia.api.application.port.output.IssuedToken;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.AuthenticatedUser;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import com.hemovia.api.infrastructure.configuration.HemoviaProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclos 21 e 22 da Seção 3: o JWT leva o id e o papel, vale 15 minutos e é recusado se for adulterado, vencido ou de outro segredo.
*/
class JwtAccessTokenIssuerTest {

    // Instante fixo em que os tokens são emitidos
    private static final Instant AGORA = Instant.parse("2026-08-11T12:00:00Z");
    private static final String SEGREDO = "segredo-somente-para-testes-0123456789";

    // Emissor configurado com o segredo e o relógio informados
    private static JwtAccessTokenIssuer emissor(String segredo, Instant agora) {
        HemoviaProperties.Security seguranca = new HemoviaProperties.Security(segredo, 15, 7, true, "Lax", "http://localhost:5173");
        HemoviaProperties propriedades = new HemoviaProperties(seguranca, new HemoviaProperties.AdminBootstrap("", "", "", ""));
        return new JwtAccessTokenIssuer(propriedades, Clock.fixed(agora, ZoneOffset.UTC));
    }

    // Usuário válido com o papel e o vínculo informados
    private static User usuario(Role papel, UUID vinculo) {
        return User.create("Alice Sena", new Email("alice@hemovia.com"), new Cpf("529.982.247-25"), null, "hash", papel, vinculo);
    }

    @Test
    void deveEmitirTokenComIdPapelEValidadeDeQuinzeMinutos() {
        // Quando emitimos o token de um administrador
        User admin = usuario(Role.ADMIN, null);
        IssuedToken emitido = emissor(SEGREDO, AGORA).issue(admin);
        // Então o token traz o id no sub, o papel e expira em 15 minutos
        DecodedJWT jwt = JWT.decode(emitido.value());
        assertThat(jwt.getSubject()).isEqualTo(admin.id().toString());
        assertThat(jwt.getClaim("role").asString()).isEqualTo("ADMIN");
        assertThat(jwt.getIssuedAtAsInstant()).isEqualTo(AGORA);
        assertThat(jwt.getExpiresAtAsInstant()).isEqualTo(AGORA.plus(Duration.ofMinutes(15)));
        assertThat(emitido.expiresAt()).isEqualTo(AGORA.plus(Duration.ofMinutes(15)));
    }

    @Test
    void deveDevolverQuemEhODonoDoTokenComOVinculo() {
        // Dado o token de um usuário de hospital
        UUID hospital = UUID.randomUUID();
        User usuario = usuario(Role.HOSPITAL, hospital);
        JwtAccessTokenIssuer emissor = emissor(SEGREDO, AGORA);
        // Quando verificamos o token
        AuthenticatedUser dono = emissor.verify(emissor.issue(usuario).value());
        // Então recebemos o id, o papel e o hospital do vínculo
        assertThat(dono.userId()).isEqualTo(usuario.id());
        assertThat(dono.role()).isEqualTo(Role.HOSPITAL);
        assertThat(dono.worksAtHospital(hospital)).isTrue();
    }

    @Test
    void deveDevolverOVinculoDeHemocentroEDeVeiculo() {
        // Os outros dois vínculos também viajam no token
        JwtAccessTokenIssuer emissor = emissor(SEGREDO, AGORA);
        UUID hemocentro = UUID.randomUUID();
        UUID veiculo = UUID.randomUUID();
        assertThat(emissor.verify(emissor.issue(usuario(Role.HEMOCENTRO, hemocentro)).value()).worksAtBloodCenter(hemocentro)).isTrue();
        assertThat(emissor.verify(emissor.issue(usuario(Role.TRANSPORTADOR, veiculo)).value()).drivesVehicle(veiculo)).isTrue();
    }

    @Test
    void deveEmitirTokenSemPapelParaUsuarioAntigoEVerificarComPapelNulo() {
        // Usuário de antes dos papéis (papel nulo) gera um token sem a claim role
        User antigo = User.restore(UUID.randomUUID(), "Antigo", new Email("antigo@hemovia.com"), new Cpf("111.444.777-35"),
                null, "hash", null, true, null, null, null);
        JwtAccessTokenIssuer emissor = emissor(SEGREDO, AGORA);
        // Então a verificação funciona, mas o papel vem nulo e o acesso será negado mais adiante
        assertThat(emissor.verify(emissor.issue(antigo).value()).role()).isNull();
    }

    @Test
    void deveRecusarTokenAdulterado() {
        // Dados dois tokens válidos de papéis diferentes
        JwtAccessTokenIssuer emissor = emissor(SEGREDO, AGORA);
        String[] hospital = emissor.issue(usuario(Role.HOSPITAL, UUID.randomUUID())).value().split("\\.");
        String[] admin = emissor.issue(usuario(Role.ADMIN, null)).value().split("\\.");
        // Quando alguém cola o conteúdo de administrador na assinatura do token de hospital
        String adulterado = hospital[0] + "." + admin[1] + "." + hospital[2];
        // Então o token é recusado
        assertThatThrownBy(() -> emissor.verify(adulterado)).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void deveRecusarTokenVencido() {
        // Dado um token emitido às 12h00
        String token = emissor(SEGREDO, AGORA).issue(usuario(Role.ADMIN, null)).value();
        // Então às 12h16 (depois dos 15 minutos) ele não vale mais
        assertThatThrownBy(() -> emissor(SEGREDO, AGORA.plus(Duration.ofMinutes(16))).verify(token))
                .isInstanceOf(InvalidCredentialsException.class);
        // E às 12h14 ainda vale
        assertThat(emissor(SEGREDO, AGORA.plus(Duration.ofMinutes(14))).verify(token).role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void deveRecusarTokenAssinadoComOutroSegredo() {
        // Dado um token assinado por quem não conhece o nosso segredo
        String token = emissor("outro-segredo-completamente-diferente-9876543210", AGORA).issue(usuario(Role.ADMIN, null)).value();
        // Então ele é recusado
        assertThatThrownBy(() -> emissor(SEGREDO, AGORA).verify(token)).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void deveRecusarTextoQueNaoEhToken() {
        assertThatThrownBy(() -> emissor(SEGREDO, AGORA).verify("isto-nao-e-um-jwt")).isInstanceOf(InvalidCredentialsException.class);
    }
}
