package com.hemovia.api.application.usecase.auth;

import com.hemovia.api.application.dto.request.LoginRequest;
import com.hemovia.api.application.dto.response.SessionTokens;
import com.hemovia.api.application.port.input.LoginUseCase;
import com.hemovia.api.application.port.output.PasswordHasher;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.exception.InvalidDataException;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/*
Login por e-mail ou CPF no mesmo campo. Qualquer falha devolve a mesma mensagem genérica,
para ninguém descobrir se um e-mail ou CPF existe no sistema.
*/
@Service
@Transactional
public class Login implements LoginUseCase {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final SessionOpener sessions;

    public Login(UserRepository users, PasswordHasher hasher, SessionOpener sessions) {
        this.users = users;
        this.hasher = hasher;
        this.sessions = sessions;
    }

    @Override
    public SessionTokens execute(LoginRequest request) {
        // Só entra quem existe, está ativo, tem papel e acertou a senha
        User user = find(request.identifier())
                .filter(User::canLogIn)
                .filter(found -> hasher.matches(request.password(), found.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        // Cada login abre uma sessão nova, com uma família de refresh tokens só dela
        return sessions.open(user, UUID.randomUUID()).tokens();
    }

    // Com arroba é e-mail; sem arroba é CPF. Texto fora do formato vira "não achei", não erro de formato.
    private Optional<User> find(String identifier) {
        String trimmed = identifier.trim();
        try {
            return trimmed.contains("@") ? users.findByEmail(new Email(trimmed)) : users.findByCpf(new Cpf(trimmed));
        } catch (InvalidDataException invalidFormat) {
            return Optional.empty();
        }
    }
}
