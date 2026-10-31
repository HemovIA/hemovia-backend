package com.hemovia.api.application.usecase.auth;

import com.hemovia.api.application.dto.response.SessionTokens;
import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.application.port.output.AccessTokenIssuer;
import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.application.port.output.IssuedToken;
import com.hemovia.api.application.port.output.OpaqueTokenGenerator;
import com.hemovia.api.application.port.output.RefreshTokenRepository;
import com.hemovia.api.application.port.output.SessionPolicy;
import com.hemovia.api.application.port.output.TokenHasher;
import com.hemovia.api.domain.model.RefreshToken;
import com.hemovia.api.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

/*
Abre uma sessão para o usuário: emite o access token e um refresh token novo, guardando só o hash deste.
Login e renovação usam a mesma peça, mudando apenas a família do refresh token.
*/
@Service
public class SessionOpener {

    // Sessão aberta: os tokens para o cliente e o id do refresh token guardado (a renovação precisa dele)
    public record Opened(SessionTokens tokens, UUID refreshTokenId) { }

    private final AccessTokenIssuer accessTokens;
    private final OpaqueTokenGenerator opaqueTokens;
    private final TokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokens;
    private final SessionPolicy policy;
    private final ClockProvider clock;

    public SessionOpener(AccessTokenIssuer accessTokens, OpaqueTokenGenerator opaqueTokens, TokenHasher tokenHasher,
                         RefreshTokenRepository refreshTokens, SessionPolicy policy, ClockProvider clock) {
        this.accessTokens = accessTokens;
        this.opaqueTokens = opaqueTokens;
        this.tokenHasher = tokenHasher;
        this.refreshTokens = refreshTokens;
        this.policy = policy;
        this.clock = clock;
    }

    // Emite os dois tokens; o refresh token entra na família informada
    public Opened open(User user, UUID familyId) {
        // O valor puro vai só para o cliente; no banco entra o resumo dele
        String rawRefresh = opaqueTokens.newToken();
        RefreshToken refresh = refreshTokens.save(
                RefreshToken.issue(user.id(), familyId, tokenHasher.sha256(rawRefresh), clock.now(), policy.refreshTokenTtl()));
        IssuedToken access = accessTokens.issue(user);
        SessionTokens tokens = new SessionTokens(access.value(), access.expiresAt(), rawRefresh, refresh.expiresAt(), UserResponse.from(user));
        return new Opened(tokens, refresh.id());
    }
}
