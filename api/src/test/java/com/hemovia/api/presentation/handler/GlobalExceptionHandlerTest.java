package com.hemovia.api.presentation.handler;

import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.infrastructure.configuration.SystemClockProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Ciclos 6 a 10 da Seção 1: toda resposta de erro da API sai no formato ApiError da tabela 0.7.
Usamos um controller falso que lança cada tipo de erro, sem subir o Spring inteiro.
*/
class GlobalExceptionHandlerTest {

    // Instante fixo que o relógio injetado devolve, para o timestamp do erro ser previsível
    private static final Instant AGORA = Instant.parse("2026-08-11T15:30:00Z");

    // Cliente HTTP de mentira que chama o controller falso com o handler de erros ligado
    private MockMvc mvc;

    @BeforeEach
    void prepararMvc() {
        // Dado um relógio parado em AGORA, como manda a regra de injetar o Clock
        ClockProvider relogio = new SystemClockProvider(Clock.fixed(AGORA, ZoneOffset.UTC));
        // E o controller falso protegido pelo handler novo
        mvc = MockMvcBuilders.standaloneSetup(new ControllerFalso())
                .setControllerAdvice(new GlobalExceptionHandler(relogio))
                .build();
    }

    @Test
    void deveResponder404ComTodosOsCamposQuandoRecursoNaoExiste() throws Exception {
        // Quando a rota lança ResourceNotFoundException
        mvc.perform(get("/falso/nao-encontrado"))
                // Então o status HTTP é 404
                .andExpect(status().isNotFound())
                // E o corpo traz todos os campos do ApiError preenchidos
                .andExpect(jsonPath("$.timestamp").value("2026-08-11T15:30:00Z"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Hospital não encontrado: 123"))
                .andExpect(jsonPath("$.path").value("/falso/nao-encontrado"))
                // E fieldErrors existe, mas vazio, porque não é erro de validação
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void deveResponder400ComErrosCampoACampoQuandoDtoEhInvalido() throws Exception {
        // Quando o corpo tem nome em branco e idade nula
        mvc.perform(post("/falso/validar").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\",\"idade\":null}"))
                // Então a resposta é 400 de validação
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Dados de entrada inválidos."))
                // E cada campo inválido aparece com a sua própria mensagem
                .andExpect(jsonPath("$.fieldErrors.nome").value("O nome é obrigatório"))
                .andExpect(jsonPath("$.fieldErrors.idade").value("A idade é obrigatória"));
    }

    @Test
    void deveResponder400DeRequisicaoMalformadaQuandoUuidDoCaminhoEhInvalido() throws Exception {
        // Quando o caminho traz um texto que não é UUID (antes isso virava 500)
        mvc.perform(get("/falso/uuid/isto-nao-e-uuid"))
                // Então é erro do cliente: 400 MALFORMED_REQUEST
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));
    }

    @Test
    void deveResponder400DeRequisicaoMalformadaQuandoJsonEhIlegivel() throws Exception {
        // Quando o corpo é um JSON quebrado
        mvc.perform(post("/falso/validar").contentType(MediaType.APPLICATION_JSON).content("{"))
                // Então também é MALFORMED_REQUEST
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));
    }

    @Test
    void deveResponder500SemVazarAMensagemInternaQuandoOErroEhInesperado() throws Exception {
        // Quando a rota explode com uma mensagem que contém um segredo
        mvc.perform(get("/falso/erro-inesperado"))
                // Então o status é 500 INTERNAL_ERROR
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                // E a mensagem é genérica, sem nada do erro original
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    @Test
    void deveResponder405EmVezDe500QuandoOMetodoHttpNaoEhSuportado() throws Exception {
        // Quando chamamos com POST uma rota que só aceita GET
        mvc.perform(post("/falso/nao-encontrado"))
                // Então o erro é do cliente, não pode virar 500 por causa do handler genérico
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void deveEnviarRetryAfterQuandoOBufferDeTelemetriaEstaCheio() throws Exception {
        // Quando o buffer de telemetria está cheio
        mvc.perform(get("/falso/dominio/TELEMETRY_BUFFER_FULL"))
                // Então a resposta pede para o cliente tentar de novo em 1 segundo
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "1"));
    }

    @Test
    void deveResponder500QuandoOCodigoDeDominioNaoTemStatusMapeado() throws Exception {
        // Quando uma exceção de negócio chega com um código que ninguém mapeou
        mvc.perform(get("/falso/dominio/CODIGO_SEM_MAPEAMENTO"))
                // Então é falha do servidor e a mensagem original não vaza
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    @Test
    void deveManterLegadoDeIllegalArgumentComo400AteOsServicosMigrarem() throws Exception {
        // Os serviços antigos lançam IllegalArgumentException para entrada ruim e hoje respondem 400
        mvc.perform(get("/falso/legado-argumento-invalido"))
                // O comportamento continua, agora no formato ApiError
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message").value("ID inválido: abc"));
    }

    @Test
    void deveManterLegadoDeEntityNotFoundComo404AteOsServicosMigrarem() throws Exception {
        // Os serviços antigos lançam a EntityNotFoundException do JPA e hoje respondem 404
        mvc.perform(get("/falso/legado-entidade"))
                // O comportamento continua, agora no formato ApiError
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Bolsa não encontrada: 1"));
    }

    // Uma linha para cada situação da tabela 0.7 do guia: caminho que provoca o erro, status e código esperados
    static Stream<Arguments> tabela07() {
        return Stream.of(
                Arguments.of("/falso/dominio/BLOOD_BAG_EXPIRED", 400, "BLOOD_BAG_EXPIRED"),
                Arguments.of("/falso/dominio/TEMPERATURE_OUT_OF_RANGE", 400, "TEMPERATURE_OUT_OF_RANGE"),
                Arguments.of("/falso/dominio/UNAUTHORIZED", 401, "UNAUTHORIZED"),
                Arguments.of("/falso/nao-autenticado", 401, "UNAUTHORIZED"),
                Arguments.of("/falso/dominio/FORBIDDEN", 403, "FORBIDDEN"),
                Arguments.of("/falso/acesso-negado", 403, "FORBIDDEN"),
                Arguments.of("/falso/nao-encontrado", 404, "NOT_FOUND"),
                Arguments.of("/falso/dominio/DUPLICATE_RESOURCE", 409, "DUPLICATE_RESOURCE"),
                Arguments.of("/falso/dominio/RESOURCE_IN_USE", 409, "RESOURCE_IN_USE"),
                Arguments.of("/falso/dominio/VEHICLE_UNAVAILABLE", 409, "VEHICLE_UNAVAILABLE"),
                Arguments.of("/falso/dominio/INSUFFICIENT_INVENTORY", 409, "INSUFFICIENT_INVENTORY"),
                Arguments.of("/falso/dominio/BLOOD_BAG_ALREADY_RESERVED", 409, "BLOOD_BAG_ALREADY_RESERVED"),
                Arguments.of("/falso/dominio/REQUEST_ALREADY_PROCESSED", 409, "REQUEST_ALREADY_PROCESSED"),
                Arguments.of("/falso/transicao-invalida", 422, "INVALID_STATE_TRANSITION"),
                Arguments.of("/falso/dominio/INVALID_ROUTE", 422, "INVALID_ROUTE"),
                Arguments.of("/falso/dominio/INCOMPATIBLE_BLOOD", 422, "INCOMPATIBLE_BLOOD"),
                Arguments.of("/falso/dominio/TELEMETRY_BUFFER_FULL", 503, "TELEMETRY_BUFFER_FULL"),
                Arguments.of("/falso/banco-fora", 503, "SERVICE_UNAVAILABLE"));
    }

    @ParameterizedTest(name = "{0} deve virar {1} {2}")
    @MethodSource("tabela07")
    void deveMapearCadaLinhaDaTabela07(String caminho, int statusEsperado, String erroEsperado) throws Exception {
        // Quando a rota provoca a situação da linha da tabela
        mvc.perform(get(caminho))
                // Então o status HTTP e o código de erro estável batem com a tabela
                .andExpect(status().is(statusEsperado))
                .andExpect(jsonPath("$.error").value(erroEsperado))
                .andExpect(jsonPath("$.status").value(statusEsperado))
                // E o caminho da requisição vem de volta no corpo
                .andExpect(jsonPath("$.path").value(caminho));
    }
}
