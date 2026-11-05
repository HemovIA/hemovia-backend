package com.hemovia.api.application;

import com.hemovia.api.application.port.output.AccessTokenIssuer;
import com.hemovia.api.application.port.output.AdminBootstrapSettings;
import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.application.port.output.CurrentUserProvider;
import com.hemovia.api.application.port.output.IssuedToken;
import com.hemovia.api.application.port.output.LinkedResourceChecker;
import com.hemovia.api.application.port.output.OpaqueTokenGenerator;
import com.hemovia.api.application.port.output.PasswordHasher;
import com.hemovia.api.application.port.output.RefreshTokenRepository;
import com.hemovia.api.application.port.output.SessionPolicy;
import com.hemovia.api.application.port.output.TokenHasher;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.application.usecase.auth.GetCurrentUser;
import com.hemovia.api.application.usecase.auth.Login;
import com.hemovia.api.application.usecase.auth.Logout;
import com.hemovia.api.application.usecase.auth.RefreshSession;
import com.hemovia.api.application.usecase.auth.SessionOpener;
import com.hemovia.api.application.usecase.user.BootstrapAdmin;
import com.hemovia.api.application.usecase.user.ChangeUserRole;
import com.hemovia.api.application.usecase.user.CreateUser;
import com.hemovia.api.application.usecase.user.DeactivateUser;
import com.hemovia.api.application.usecase.user.GetUser;
import com.hemovia.api.application.usecase.user.ListUsers;
import com.hemovia.api.application.usecase.user.UpdateUser;
import com.hemovia.api.application.usecase.user.UserLinkValidator;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.AuthenticatedUser;
import com.hemovia.api.domain.model.RefreshToken;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/*
Monta, em memória, tudo o que os casos de uso de autenticação e de usuários precisam.
Cada porta de saída tem um fake simples; assim os testes rodam sem Spring e sem banco.
*/
public final class CenarioDeAutenticacao {

    // Instante em que o relógio começa e senha usada pelos usuários de teste
    public static final Instant AGORA = Instant.parse("2026-08-11T12:00:00Z");
    public static final String SENHA = "senha123";

    // Fakes que os testes consultam para conferir o que aconteceu
    public final UsuariosEmMemoria usuarios = new UsuariosEmMemoria();
    public final TokensEmMemoria tokens = new TokensEmMemoria();
    public final RelogioMutavel relogio = new RelogioMutavel(AGORA);
    public final RecursosEmMemoria recursos = new RecursosEmMemoria();
    public final UsuarioLogado usuarioLogado = new UsuarioLogado();

    // Peças compartilhadas pelos casos de uso
    private final PasswordHasher hasher = new HasherFalso();
    private final SessionOpener abertura = new SessionOpener(new EmissorFalso(), new GeradorSequencial(),
            new ResumoFalso(), tokens, () -> Duration.ofDays(7), relogio);
    private final UserLinkValidator vinculos = new UserLinkValidator(recursos);

    // Casos de uso de autenticação
    public Login login() {
        return new Login(usuarios, hasher, abertura);
    }

    public RefreshSession refresh() {
        return new RefreshSession(tokens, usuarios, new ResumoFalso(), abertura, relogio);
    }

    public Logout logout() {
        return new Logout(tokens, new ResumoFalso(), relogio);
    }

    public GetCurrentUser usuarioAtual() {
        return new GetCurrentUser(usuarios, usuarioLogado);
    }

    // Casos de uso de usuários
    public CreateUser criarUsuario() {
        return new CreateUser(usuarios, hasher, vinculos);
    }

    public ListUsers listarUsuarios() {
        return new ListUsers(usuarios);
    }

    public GetUser buscarUsuario() {
        return new GetUser(usuarios);
    }

    public UpdateUser atualizarUsuario() {
        return new UpdateUser(usuarios);
    }

    public ChangeUserRole trocarPapel() {
        return new ChangeUserRole(usuarios, vinculos);
    }

    public DeactivateUser desativarUsuario() {
        return new DeactivateUser(usuarios, tokens, relogio);
    }

    public BootstrapAdmin bootstrapAdmin(AdminBootstrapSettings configuracao) {
        return new BootstrapAdmin(usuarios, hasher, configuracao);
    }

    // Guarda um usuário válido, com a senha padrão já em hash, e devolve ele
    public User salvarUsuario(String email, String cpf, Role papel, UUID vinculo) {
        User usuario = User.create("Alice Sena", new Email(email), new Cpf(cpf), "81999998888", hasher.hash(SENHA), papel, vinculo);
        return usuarios.save(usuario);
    }

    // Atalho: um administrador ativo com e-mail e CPF fixos
    public User salvarAdmin() {
        return salvarUsuario("alice@hemovia.com", "529.982.247-25", Role.ADMIN, null);
    }

    // Relógio que os testes podem avançar para simular a passagem do tempo
    public static final class RelogioMutavel implements ClockProvider {

        private Instant agora;

        RelogioMutavel(Instant inicio) {
            this.agora = inicio;
        }

        public void avancar(Duration tempo) {
            agora = agora.plus(tempo);
        }

        @Override
        public Instant now() {
            return agora;
        }

        @Override
        public LocalDate today() {
            return LocalDate.ofInstant(agora, ZoneOffset.UTC);
        }
    }

    // Repositório de usuários que guarda tudo num mapa
    public static final class UsuariosEmMemoria implements UserRepository {

        private final Map<UUID, User> guardados = new LinkedHashMap<>();

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(guardados.get(id));
        }

        @Override
        public Optional<User> findByEmail(Email email) {
            return guardados.values().stream().filter(u -> u.email().equals(email)).findFirst();
        }

        @Override
        public Optional<User> findByCpf(Cpf cpf) {
            return guardados.values().stream().filter(u -> u.cpf().equals(cpf)).findFirst();
        }

        @Override
        public boolean existsByEmail(Email email) {
            return findByEmail(email).isPresent();
        }

        @Override
        public boolean existsByCpf(Cpf cpf) {
            return findByCpf(cpf).isPresent();
        }

        @Override
        public boolean existsByRole(Role role) {
            return guardados.values().stream().anyMatch(u -> u.role() == role);
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(guardados.values());
        }

        @Override
        public User save(User user) {
            guardados.put(user.id(), user);
            return user;
        }
    }

    // Repositório de refresh tokens que guarda tudo num mapa
    public static final class TokensEmMemoria implements RefreshTokenRepository {

        private final Map<UUID, RefreshToken> guardados = new LinkedHashMap<>();

        @Override
        public RefreshToken save(RefreshToken token) {
            guardados.put(token.id(), token);
            return token;
        }

        @Override
        public Optional<RefreshToken> findByTokenHash(String tokenHash) {
            return guardados.values().stream().filter(t -> t.tokenHash().equals(tokenHash)).findFirst();
        }

        @Override
        public void revokeFamily(UUID familyId, Instant now) {
            guardados.values().stream().filter(t -> t.familyId().equals(familyId)).forEach(t -> t.revoke(now, null));
        }

        @Override
        public void revokeAllOfUser(UUID userId, Instant now) {
            guardados.values().stream().filter(t -> t.userId().equals(userId)).forEach(t -> t.revoke(now, null));
        }

        // Todos os tokens guardados, para os testes conferirem
        public List<RefreshToken> todos() {
            return new ArrayList<>(guardados.values());
        }
    }

    // Diz que existem só os hospitais, hemocentros e veículos que o teste cadastrar
    public static final class RecursosEmMemoria implements LinkedResourceChecker {

        private final Set<UUID> hospitais = new HashSet<>();
        private final Set<UUID> hemocentros = new HashSet<>();
        private final Set<UUID> veiculos = new HashSet<>();

        public UUID novoHospital() {
            UUID id = UUID.randomUUID();
            hospitais.add(id);
            return id;
        }

        public UUID novoHemocentro() {
            UUID id = UUID.randomUUID();
            hemocentros.add(id);
            return id;
        }

        public UUID novoVeiculo() {
            UUID id = UUID.randomUUID();
            veiculos.add(id);
            return id;
        }

        @Override
        public boolean hospitalExists(UUID id) {
            return hospitais.contains(id);
        }

        @Override
        public boolean bloodCenterExists(UUID id) {
            return hemocentros.contains(id);
        }

        @Override
        public boolean vehicleExists(UUID id) {
            return veiculos.contains(id);
        }
    }

    // Usuário autenticado da requisição, escolhido pelo teste
    public static final class UsuarioLogado implements CurrentUserProvider {

        private AuthenticatedUser atual;

        public void definir(AuthenticatedUser usuario) {
            this.atual = usuario;
        }

        @Override
        public AuthenticatedUser current() {
            if (atual == null) {
                throw new InvalidCredentialsException();
            }
            return atual;
        }
    }

    // Hash de mentira, previsível: "hash:" na frente da senha
    private static final class HasherFalso implements PasswordHasher {

        @Override
        public String hash(String rawPassword) {
            return "hash:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String hash) {
            return hash(rawPassword).equals(hash);
        }
    }

    // Emissor de access token de mentira: "access-" mais o id do usuário, valendo 15 minutos
    private final class EmissorFalso implements AccessTokenIssuer {

        @Override
        public IssuedToken issue(User user) {
            return new IssuedToken("access-" + user.id(), relogio.now().plus(Duration.ofMinutes(15)));
        }

        @Override
        public AuthenticatedUser verify(String token) {
            throw new UnsupportedOperationException("Os casos de uso não verificam access token");
        }
    }

    // Gerador de refresh token de mentira: opaco-1, opaco-2...
    private static final class GeradorSequencial implements OpaqueTokenGenerator {

        private int contador;

        @Override
        public String newToken() {
            contador++;
            return "opaco-" + contador;
        }
    }

    // Resumo de mentira, previsível: "sha:" na frente do token
    private static final class ResumoFalso implements TokenHasher {

        @Override
        public String sha256(String rawToken) {
            return "sha:" + rawToken;
        }
    }
}
