package com.hemovia.api.presentation.controller;

import com.hemovia.api.application.port.output.AccessTokenIssuer;
import com.hemovia.api.application.port.output.IssuedToken;
import com.hemovia.api.application.port.output.PasswordHasher;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import com.hemovia.api.infrastructure.security.SessionCookies;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Ciclos 25 a 31 da Seção 3: login, refresh, logout, CSRF, papéis e CORS pela cadeia de segurança real,
com PostgreSQL de verdade. Cada teste roda numa transação que é desfeita no fim.
*/
@Tag("integration")
@SpringBootTest
@Transactional
@TestPropertySource(properties = {"hemovia.security.cookie-secure=true", "hemovia.security.cookie-same-site=Lax",
        "hemovia.security.cors-allowed-origins=http://localhost:5173"})
class SessaoPorCookieIntegrationTest {

    private static final String SENHA = "senha-segura-123";

    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private UserRepository usuarios;
    @Autowired
    private PasswordHasher hasher;
    @Autowired
    private AccessTokenIssuer emissor;

    private MockMvc mvc;
    private User admin;

    @BeforeEach
    void preparar() {
        // A cadeia de segurança real entra no MockMvc, como numa chamada de verdade
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        admin = usuarios.save(User.create("Admin Teste", new Email(UUID.randomUUID() + "@hemovia.com"),
                cpfAleatorio(), "81999998888", hasher.hash(SENHA), Role.ADMIN, null));
    }

    // ---------- ciclo 25: login por cookie ----------

    @Test
    void deveEntrarComEmailEEntregarOsTokensSoEmCookiesHttpOnly() throws Exception {
        MvcResult resultado = fazerLogin(admin.email().value(), SENHA).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(admin.email().value()))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();
        // O corpo nunca carrega token nem senha
        String corpo = resultado.getResponse().getContentAsString();
        assertThat(corpo).doesNotContain("accessToken", "refreshToken", "password", "passwordHash");
        // Os dois cookies existem, são HttpOnly e Secure, com o caminho certo de cada um
        String access = cabecalhoDoCookie(resultado, SessionCookies.ACCESS);
        String refresh = cabecalhoDoCookie(resultado, SessionCookies.REFRESH);
        assertThat(access).contains("HttpOnly", "Secure", "SameSite=Lax", "Path=/api");
        assertThat(refresh).contains("HttpOnly", "Secure", "SameSite=Lax", "Path=/api/v1/auth");
    }

    @Test
    void deveEntrarComCpfNoMesmoCampoIdentifier() throws Exception {
        fazerLogin(admin.cpf().digits(), SENHA).andExpect(status().isOk());
    }

    @Test
    void deveRecusarSenhaErradaComMensagemGenerica() throws Exception {
        fazerLogin(admin.email().value(), "senha-errada").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Credenciais inválidas."));
    }

    @Test
    void deveRecusarUsuarioInexistenteComAMesmaMensagem() throws Exception {
        fazerLogin("ninguem@hemovia.com", SENHA).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas."));
    }

    // ---------- ciclo 26: rotas protegidas ----------

    @Test
    void deveResponderMeComOCookieDeAcesso() throws Exception {
        MvcResult login = fazerLogin(admin.email().value(), SENHA).andReturn();
        mvc.perform(get("/api/v1/auth/me").cookie(cookie(login, SessionCookies.ACCESS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(admin.id().toString()));
    }

    @Test
    void deveNegarMeSemCookieNoFormatoApiError() throws Exception {
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/me"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deveNegarCookieDeAcessoComTokenInvalido() throws Exception {
        mvc.perform(get("/api/v1/auth/me").cookie(new Cookie(SessionCookies.ACCESS, "lixo.sem.assinatura")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveNegarRotaDeNegocioSemSessao() throws Exception {
        // Nenhuma rota de negócio é pública
        mvc.perform(get("/api/v1/donations")).andExpect(status().isUnauthorized());
    }

    // ---------- ciclo 27: CSRF ----------

    @Test
    void deveEntregarOCookieXsrfNaRotaCsrf() throws Exception {
        MvcResult resultado = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isNoContent()).andReturn();
        assertThat(resultado.getResponse().getHeaders(HttpHeaders.SET_COOKIE)).anyMatch(h -> h.startsWith("XSRF-TOKEN="));
        // O cookie XSRF precisa ser legível pelo JavaScript, então não pode ser HttpOnly
        assertThat(cabecalhoDoCookie(resultado, "XSRF-TOKEN")).doesNotContain("HttpOnly");
    }

    @Test
    void deveBarrarEscritaSemOHeaderXsrf() throws Exception {
        Cookie acesso = cookie(fazerLogin(admin.email().value(), SENHA).andReturn(), SessionCookies.ACCESS);
        mvc.perform(post("/api/v1/users").cookie(acesso).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    // ---------- ciclo 28: papéis ----------

    @Test
    void deveDeixarOAdminCriarUsuario() throws Exception {
        Cookie acesso = cookie(fazerLogin(admin.email().value(), SENHA).andReturn(), SessionCookies.ACCESS);
        String email = UUID.randomUUID() + "@hemovia.com";
        comCsrf(post("/api/v1/users").cookie(acesso).contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Novo Admin","email":"%s","cpf":"%s","password":"senha-nova-123","role":"ADMIN"}
                """.formatted(email, cpfAleatorio().digits())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("senha-nova-123"))));
    }

    @Test
    void deveNegarGestaoDeUsuariosParaQuemNaoEAdmin() throws Exception {
        Cookie hospital = cookieDeAcessoCom(Role.HOSPITAL);
        mvc.perform(get("/api/v1/users").cookie(hospital)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void deveNegarRotaDeNegocioParaUsuarioSemPapel() throws Exception {
        // Usuário antigo sem papel entra, mas não acessa nada de negócio
        Cookie semPapel = cookieDeAcessoCom(null);
        mvc.perform(get("/api/v1/donations").cookie(semPapel)).andExpect(status().isForbidden());
    }

    @Test
    void deveNaoTerMaisARotaDeRegistroPublica() throws Exception {
        Cookie acesso = cookie(fazerLogin(admin.email().value(), SENHA).andReturn(), SessionCookies.ACCESS);
        comCsrf(post("/api/v1/auth/register").cookie(acesso).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }

    // ---------- ciclo 29: refresh com rotação ----------

    @Test
    void deveRenovarASessaoTrocandoOsDoisCookies() throws Exception {
        MvcResult login = fazerLogin(admin.email().value(), SENHA).andReturn();
        Cookie refreshAntigo = cookie(login, SessionCookies.REFRESH);
        MvcResult renovado = comCsrf(post("/api/v1/auth/refresh").cookie(refreshAntigo)).andExpect(status().isOk()).andReturn();
        // O refresh token é rotacionado: o novo é diferente do antigo
        assertThat(cookie(renovado, SessionCookies.REFRESH).getValue()).isNotEqualTo(refreshAntigo.getValue());
        assertThat(cabecalhoDoCookie(renovado, SessionCookies.ACCESS)).contains("HttpOnly");
    }

    @Test
    void deveRevogarAFamiliaQuandoUmRefreshTokenVelhoForReapresentado() throws Exception {
        MvcResult login = fazerLogin(admin.email().value(), SENHA).andReturn();
        Cookie primeiro = cookie(login, SessionCookies.REFRESH);
        MvcResult renovado = comCsrf(post("/api/v1/auth/refresh").cookie(primeiro)).andExpect(status().isOk()).andReturn();
        Cookie segundo = cookie(renovado, SessionCookies.REFRESH);
        // Reapresentar o primeiro (já revogado) é sinal de roubo: 401 e a família inteira cai
        comCsrf(post("/api/v1/auth/refresh").cookie(primeiro)).andExpect(status().isUnauthorized());
        comCsrf(post("/api/v1/auth/refresh").cookie(segundo)).andExpect(status().isUnauthorized());
    }

    @Test
    void deveNegarRefreshSemCookie() throws Exception {
        comCsrf(post("/api/v1/auth/refresh")).andExpect(status().isUnauthorized());
    }

    // ---------- ciclo 30: logout ----------

    @Test
    void deveEncerrarASessaoApagandoOsCookiesERevogandoORefresh() throws Exception {
        MvcResult login = fazerLogin(admin.email().value(), SENHA).andReturn();
        Cookie acesso = cookie(login, SessionCookies.ACCESS);
        Cookie refresh = cookie(login, SessionCookies.REFRESH);
        MvcResult saida = comCsrf(post("/api/v1/auth/logout").cookie(acesso, refresh)).andExpect(status().isNoContent()).andReturn();
        assertThat(cabecalhoDoCookie(saida, SessionCookies.ACCESS)).contains("Max-Age=0");
        assertThat(cabecalhoDoCookie(saida, SessionCookies.REFRESH)).contains("Max-Age=0");
        // O refresh usado no logout não serve mais
        comCsrf(post("/api/v1/auth/refresh").cookie(refresh)).andExpect(status().isUnauthorized());
    }

    // ---------- ciclo 31: CORS ----------

    @Test
    void deveLiberarCorsComCredenciaisParaOFrontEndConfigurado() throws Exception {
        mvc.perform(options("/api/v1/auth/login").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void deveRecusarCorsDeOrigemDesconhecida() throws Exception {
        mvc.perform(options("/api/v1/auth/login").header("Origin", "http://site-malicioso.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    // ---------- apoio ----------

    // Faz o login sem CSRF, que é a única escrita liberada dele
    private org.springframework.test.web.servlet.ResultActions fazerLogin(String identificador, String senha) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"%s\",\"password\":\"%s\"}".formatted(identificador, senha)));
    }

    // Pega um token CSRF de verdade na rota /auth/csrf e o devolve no cookie e no header, como o front-end faz
    private org.springframework.test.web.servlet.ResultActions comCsrf(MockHttpServletRequestBuilder requisicao) throws Exception {
        MvcResult csrf = mvc.perform(get("/api/v1/auth/csrf")).andReturn();
        String token = cookie(csrf, "XSRF-TOKEN").getValue();
        return mvc.perform(requisicao.cookie(new Cookie("XSRF-TOKEN", token)).header("X-XSRF-TOKEN", token));
    }

    // Monta um cookie de acesso para um usuário de papel dado, sem passar pelo login
    private Cookie cookieDeAcessoCom(Role papel) {
        UUID vinculo = papel == null ? null : UUID.randomUUID();
        User usuario = User.restore(UUID.randomUUID(), "Fulano", new Email(UUID.randomUUID() + "@hemovia.com"),
                cpfAleatorio(), null, "hash", papel, true, papel == Role.HOSPITAL ? vinculo : null, null, null);
        IssuedToken token = emissor.issue(usuario);
        return new Cookie(SessionCookies.ACCESS, token.value());
    }

    // Linha completa do Set-Cookie de um cookie pelo nome
    private static String cabecalhoDoCookie(MvcResult resultado, String nome) {
        List<String> cabecalhos = resultado.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        return cabecalhos.stream().filter(h -> h.startsWith(nome + "=")).findFirst()
                .orElseThrow(() -> new AssertionError("Cookie ausente: " + nome));
    }

    // Reconstrói o cookie que o navegador devolveria, só com nome e valor
    private static Cookie cookie(MvcResult resultado, String nome) {
        String linha = cabecalhoDoCookie(resultado, nome);
        String valor = linha.substring(nome.length() + 1, linha.indexOf(';') < 0 ? linha.length() : linha.indexOf(';'));
        return new Cookie(nome, valor);
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
}
