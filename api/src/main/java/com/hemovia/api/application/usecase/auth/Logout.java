package com.hemovia.api.application.usecase.auth;

import com.hemovia.api.application.port.input.LogoutUseCase;
import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.application.port.output.RefreshTokenRepository;
import com.hemovia.api.application.port.output.TokenHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
Encerra a sessão revogando o refresh token. É idempotente: sair duas vezes, ou com um token que não existe, não dá erro.
*/
@Service
@Transactional
public class Logout implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokens;
    private final TokenHasher tokenHasher;
    private final ClockProvider clock;

    public Logout(RefreshTokenRepository refreshTokens, TokenHasher tokenHasher, ClockProvider clock) {
        this.refreshTokens = refreshTokens;
        this.tokenHasher = tokenHasher;
        this.clock = clock;
    }

    @Override
    public void execute(String rawRefreshToken) {
        // Sem token não há o que revogar
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        // Se o token existe, é revogado; se não existe, não há nada a fazer
        refreshTokens.findByTokenHash(tokenHasher.sha256(rawRefreshToken)).ifPresent(token -> {
            token.revoke(clock.now(), null);
            refreshTokens.save(token);
        });
    }
}
