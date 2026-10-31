package com.hemovia.api.presentation.handler;

import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.domain.exception.DomainException;
import com.hemovia.api.domain.exception.InvalidStateTransitionException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/*
Ponto único onde toda exceção vira uma resposta ApiError (tabela 0.7 do guia).
Estende ResponseEntityExceptionHandler para que os erros do próprio Spring MVC,
como método não suportado ou JSON ilegível, também saiam no mesmo formato e sem virar 500.
*/
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Registro de falhas inesperadas; o detalhe técnico fica aqui e nunca vai para o cliente
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Código e texto das falhas internas, sempre genéricos para não vazar detalhes
    private static final String ERRO_INTERNO = "INTERNAL_ERROR";
    private static final String MENSAGEM_ERRO_INTERNO = "Ocorreu um erro inesperado. Tente novamente mais tarde.";

    // Códigos usados por mais de um lugar do handler
    private static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    private static final String CODIGO_BUFFER_CHEIO = "TELEMETRY_BUFFER_FULL";

    // Segundos de espera pedidos ao cliente quando o buffer de telemetria enche (SOUS03)
    private static final String RETRY_AFTER_SEGUNDOS = "1";

    // Status HTTP de cada código de negócio da tabela 0.7; o domínio só conhece o código
    private static final Map<String, HttpStatus> STATUS_POR_CODIGO = Map.ofEntries(
            Map.entry("BLOOD_BAG_EXPIRED", HttpStatus.BAD_REQUEST),
            Map.entry("TEMPERATURE_OUT_OF_RANGE", HttpStatus.BAD_REQUEST),
            Map.entry("UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
            Map.entry("FORBIDDEN", HttpStatus.FORBIDDEN),
            Map.entry("NOT_FOUND", HttpStatus.NOT_FOUND),
            Map.entry("DUPLICATE_RESOURCE", HttpStatus.CONFLICT),
            Map.entry("RESOURCE_IN_USE", HttpStatus.CONFLICT),
            Map.entry("VEHICLE_UNAVAILABLE", HttpStatus.CONFLICT),
            Map.entry("INSUFFICIENT_INVENTORY", HttpStatus.CONFLICT),
            Map.entry("BLOOD_BAG_ALREADY_RESERVED", HttpStatus.CONFLICT),
            Map.entry("REQUEST_ALREADY_PROCESSED", HttpStatus.CONFLICT),
            Map.entry("INVALID_ROUTE", HttpStatus.UNPROCESSABLE_ENTITY),
            Map.entry("INCOMPATIBLE_BLOOD", HttpStatus.UNPROCESSABLE_ENTITY),
            Map.entry(CODIGO_BUFFER_CHEIO, HttpStatus.SERVICE_UNAVAILABLE));

    // Fonte do timestamp de cada erro, injetada para os testes poderem fixar o tempo
    private final ClockProvider relogio;

    public GlobalExceptionHandler(ClockProvider relogio) {
        // Guardamos a porta do relógio, nunca chamamos Instant.now() solto
        this.relogio = relogio;
    }

    // Exceções de negócio: o status vem da tabela pelo código; código sem status vira falha interna
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> tratarNegocio(DomainException excecao, HttpServletRequest requisicao) {
        // Procuramos o status do código; se ninguém mapeou, o erro é nosso e não do cliente
        HttpStatus status = STATUS_POR_CODIGO.get(excecao.code());
        if (status == null) return tratarInesperado(excecao, requisicao);
        // Montamos a resposta já com o corpo padronizado
        ResponseEntity.BodyBuilder resposta = ResponseEntity.status(status);
        // Buffer cheio manda o cliente tentar de novo depois
        if (CODIGO_BUFFER_CHEIO.equals(excecao.code())) resposta.header(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SEGUNDOS);
        return resposta.body(erro(status, excecao.code(), excecao.getMessage(), requisicao.getRequestURI()));
    }

    // Transição de estado proibida é erro de regra, não de formato: 422
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiError> tratarTransicaoInvalida(InvalidStateTransitionException excecao, HttpServletRequest requisicao) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_STATE_TRANSITION", excecao.getMessage(), requisicao);
    }

    // Usuário autenticado sem permissão para o recurso: 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> tratarAcessoNegado(AccessDeniedException excecao, HttpServletRequest requisicao) {
        return responder(HttpStatus.FORBIDDEN, "FORBIDDEN", "Você não tem permissão para acessar este recurso.", requisicao);
    }

    // Falha de autenticação do Spring Security: 401, sem dizer o que exatamente deu errado
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> tratarNaoAutenticado(AuthenticationException excecao, HttpServletRequest requisicao) {
        return responder(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Autenticação necessária ou credenciais inválidas.", requisicao);
    }

    // Banco fora do ar: 503, o cliente pode tentar de novo mais tarde
    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiError> tratarBancoIndisponivel(DataAccessResourceFailureException excecao, HttpServletRequest requisicao) {
        // O detalhe da conexão vai para o log, porque pode conter endereço do banco
        LOG.error("Banco de dados indisponível em {}", requisicao.getRequestURI(), excecao);
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "Serviço temporariamente indisponível.", requisicao);
    }

    // LEGADO: os serviços antigos lançam IllegalArgumentException para entrada ruim; sai quando migrarem (Seções 6 e 7)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> tratarArgumentoInvalidoLegado(IllegalArgumentException excecao, HttpServletRequest requisicao) {
        return responder(HttpStatus.BAD_REQUEST, MALFORMED_REQUEST, excecao.getMessage(), requisicao);
    }

    // LEGADO: os serviços antigos lançam a exceção do JPA para recurso inexistente; sai quando migrarem (Seções 6 e 7)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> tratarEntidadeNaoEncontradaLegado(EntityNotFoundException excecao, HttpServletRequest requisicao) {
        return responder(HttpStatus.NOT_FOUND, "NOT_FOUND", excecao.getMessage(), requisicao);
    }

    // Qualquer outra exceção: 500 com mensagem genérica e o detalhe só no log
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarInesperado(Exception excecao, HttpServletRequest requisicao) {
        LOG.error("Erro inesperado em {}", requisicao.getRequestURI(), excecao);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, ERRO_INTERNO, MENSAGEM_ERRO_INTERNO, requisicao);
    }

    // Bean Validation no DTO: 400 com a mensagem de cada campo inválido
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excecao, HttpHeaders cabecalhos, HttpStatusCode status, WebRequest requisicao) {
        // LinkedHashMap mantém a ordem dos campos; se um campo falhar duas vezes, vale a primeira mensagem
        Map<String, String> campos = new LinkedHashMap<>();
        excecao.getBindingResult().getFieldErrors().forEach(campo -> campos.putIfAbsent(campo.getField(), campo.getDefaultMessage()));
        ApiError corpo = new ApiError(relogio.now(), status.value(), "VALIDATION_ERROR",
                "Dados de entrada inválidos.", caminho(requisicao), campos);
        return ResponseEntity.status(status).headers(cabecalhos).body(corpo);
    }

    // Todos os demais erros do Spring MVC (JSON ilegível, UUID inválido, método errado...) passam por aqui
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception excecao, Object corpoOriginal, HttpHeaders cabecalhos, HttpStatusCode status, WebRequest requisicao) {
        // Ignoramos o corpo em inglês do Spring e montamos o nosso, em português
        ApiError corpo = erro(status, codigoDe(status), mensagemDe(status), caminho(requisicao));
        return ResponseEntity.status(status).headers(cabecalhos).body(corpo);
    }

    // Atalho para responder com status, código e mensagem já escolhidos
    private ResponseEntity<ApiError> responder(HttpStatus status, String codigo, String mensagem, HttpServletRequest requisicao) {
        return ResponseEntity.status(status).body(erro(status, codigo, mensagem, requisicao.getRequestURI()));
    }

    // Monta o ApiError sem erros por campo, que só existem na validação
    private ApiError erro(HttpStatusCode status, String codigo, String mensagem, String caminho) {
        return new ApiError(relogio.now(), status.value(), codigo, mensagem, caminho, Map.of());
    }

    // Caminho da requisição que falhou, para quem lê o erro saber onde foi
    private static String caminho(WebRequest requisicao) {
        return ((ServletWebRequest) requisicao).getRequest().getRequestURI();
    }

    // Código de erro dos erros do Spring: 400 é sempre requisição malformada, o resto usa o nome do status
    private static String codigoDe(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> MALFORMED_REQUEST;
            case 500 -> ERRO_INTERNO;
            default -> Optional.ofNullable(HttpStatus.resolve(status.value())).map(Enum::name).orElse(ERRO_INTERNO);
        };
    }

    // Mensagem em português para os erros do Spring, escolhida pelo status
    private static String mensagemDe(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "A requisição está malformada ou tem valores com tipo inválido.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não suportado para este caminho.";
            case 406 -> "Formato de resposta não aceito.";
            case 415 -> "Formato de conteúdo não suportado.";
            default -> status.is5xxServerError() ? MENSAGEM_ERRO_INTERNO : "Não foi possível processar a requisição.";
        };
    }
}
