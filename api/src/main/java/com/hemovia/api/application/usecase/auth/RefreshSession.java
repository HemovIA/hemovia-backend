package com.hemovia.api.application.usecase.auth;

import com.hemovia.api.application.dto.response.SessionTokens;
import com.hemovia.api.application.port.input.RefreshSessionUseCase;
import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.application.port.output.RefreshTokenRepository;
import com.hemovia.api.application.port.output.TokenHasher;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.RefreshToken;
import com.hemovia.api.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/*
Renova a sessão trocando o refresh token por outro (rotação). Se alguém reapresentar um token que já foi trocado,
é sinal de roubo: a família inteira é revogada. Por isso a transação não desfaz o trabalho quando a exceção sobe.
*/
@Service
@Transactional(noRollbackFor = InvalidCredentialsException.class)
public class RefreshSession implements RefreshSessionUseCase {

    private final RefreshTokenRepository refreshTokens;
    private final UserRepository users;
    private final TokenHasher tokenHasher;
    private final SessionOpener sessions;
    private final ClockProvider clock;

    public RefreshSession(RefreshTokenRepository refreshTokens, UserRepository users, TokenHasher tokenHasher,
                          SessionOpener sessions, ClockProvider clock) {
        this.refreshTokens = refreshTokens;
        this.users = users;
        this.tokenHasher = tokenHasher;
        this.sessions = sessions;
        this.clock = clock;
    }

    @Override
    public SessionTokens execute(String rawRefreshToken) {
        // Sem token não há o que renovar
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidCredentialsException();
        }
        RefreshToken current = refreshTokens.findByTokenHash(tokenHasher.sha256(rawRefreshToken))
                .orElseThrow(InvalidCredentialsException::new);
        Instant now = clock.now();
        // Token já revogado voltando a ser usado: derruba a família inteira
        if (current.isRevoked()) {
            refreshTokens.revokeFamily(current.familyId(), now);
            throw new InvalidCredentialsException();
        }
        // Token vencido não renova nada
        if (!current.isUsableAt(now)) {
            throw new InvalidCredentialsException();
        }
        // O dono precisa continuar podendo entrar (não pode ter sido desativado)
        User user = users.findById(current.userId()).filter(User::canLogIn).orElseThrow(InvalidCredentialsException::new);
        // O novo token herda a família; o antigo é revogado apontando para ele
        SessionOpener.Opened opened = sessions.open(user, current.familyId());
        current.revoke(now, opened.refreshTokenId());
        refreshTokens.save(current);
        return opened.tokens();
    }
}
