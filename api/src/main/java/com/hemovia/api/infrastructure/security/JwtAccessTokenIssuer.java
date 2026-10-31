package com.hemovia.api.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.BaseVerification;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hemovia.api.application.port.output.AccessTokenIssuer;
import com.hemovia.api.application.port.output.IssuedToken;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.AuthenticatedUser;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.infrastructure.configuration.HemoviaProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/*
Emite e confere o access token: JWT assinado com HMAC-256, curto (15 minutos por padrão).
O token leva o id (sub), o papel e o vínculo, para as rotas decidirem sem ir ao banco a cada requisição.
*/
@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

    // Nomes das claims próprias do sistema
    private static final String ROLE = "role";
    private static final String HOSPITAL = "hospitalId";
    private static final String BLOOD_CENTER = "bloodCenterId";
    private static final String VEHICLE = "vehicleId";

    private final Algorithm algorithm;
    private final Duration ttl;
    private final Clock clock;

    public JwtAccessTokenIssuer(HemoviaProperties properties, Clock clock) {
        this.algorithm = Algorithm.HMAC256(properties.security().jwtSecret());
        this.ttl = Duration.ofMinutes(properties.security().accessTokenMinutes());
        this.clock = clock;
    }

    @Override
    public IssuedToken issue(User user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(ttl);
        JWTCreator.Builder token = JWT.create()
                .withSubject(user.id().toString())
                .withIssuedAt(now)
                .withExpiresAt(expiresAt);
        // Usuário antigo sem papel gera token sem a claim de papel; o acesso é negado mais adiante
        if (user.role() != null) {
            token.withClaim(ROLE, user.role().name());
        }
        withLink(token, HOSPITAL, user.hospitalId());
        withLink(token, BLOOD_CENTER, user.bloodCenterId());
        withLink(token, VEHICLE, user.vehicleId());
        return new IssuedToken(token.sign(algorithm), expiresAt);
    }

    @Override
    public AuthenticatedUser verify(String token) {
        try {
            // A verificação usa o nosso relógio (o build com relógio só existe na classe base), assim os testes conseguem "viajar no tempo"
            DecodedJWT jwt = ((BaseVerification) JWT.require(algorithm)).build(() -> Date.from(clock.instant())).verify(token);
            String role = jwt.getClaim(ROLE).asString();
            return new AuthenticatedUser(UUID.fromString(jwt.getSubject()), role == null ? null : Role.valueOf(role),
                    link(jwt, HOSPITAL), link(jwt, BLOOD_CENTER), link(jwt, VEHICLE));
        } catch (JWTVerificationException | IllegalArgumentException invalid) {
            // Assinatura errada, token vencido, lixo ou claim estranha: tudo vira a mesma falha de sessão
            throw new InvalidCredentialsException("Sessão inválida ou expirada.");
        }
    }

    // Só grava a claim do vínculo quando ele existe
    private static void withLink(JWTCreator.Builder token, String name, UUID id) {
        if (id != null) {
            token.withClaim(name, id.toString());
        }
    }

    // Lê a claim do vínculo; ausente vira nulo
    private static UUID link(DecodedJWT jwt, String name) {
        String id = jwt.getClaim(name).asString();
        return id == null ? null : UUID.fromString(id);
    }
}
