package com.hemovia.api.infrastructure.security;

import com.hemovia.api.application.dto.response.SessionTokens;
import com.hemovia.api.infrastructure.configuration.ApiPrefixConfiguration;
import com.hemovia.api.infrastructure.configuration.HemoviaProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/*
Monta os cookies de sessão (decisão D4): ambos HttpOnly, para o JavaScript do navegador nunca ler os tokens.
O access vale para toda a API; o refresh só viaja para as rotas de autenticação, o que reduz a exposição dele.
*/
@Component
public class SessionCookies {

    // Nomes dos cookies
    public static final String ACCESS = "HEMOVIA_ACCESS";
    public static final String REFRESH = "HEMOVIA_REFRESH";

    // Caminhos em que cada cookie é enviado pelo navegador
    private static final String ACCESS_PATH = "/api";
    private static final String REFRESH_PATH = ApiPrefixConfiguration.PREFIXO + "/auth";

    private final HemoviaProperties.Security security;

    public SessionCookies(HemoviaProperties properties) {
        this.security = properties.security();
    }

    // Os dois cookies de uma sessão recém-aberta, já no formato do header Set-Cookie
    public List<String> open(SessionTokens tokens) {
        return List.of(
                cookie(ACCESS, tokens.accessToken(), ACCESS_PATH, Duration.ofMinutes(security.accessTokenMinutes())),
                cookie(REFRESH, tokens.refreshToken(), REFRESH_PATH, Duration.ofDays(security.refreshTokenDays())));
    }

    // Cookies vazios com validade zero, que fazem o navegador apagar os dois (logout)
    public List<String> clear() {
        return List.of(cookie(ACCESS, "", ACCESS_PATH, Duration.ZERO), cookie(REFRESH, "", REFRESH_PATH, Duration.ZERO));
    }

    // Um cookie com os atributos de segurança vindos da configuração
    private String cookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(security.cookieSecure())
                .sameSite(security.cookieSameSite())
                .path(path)
                .maxAge(maxAge)
                .build()
                .toString();
    }
}
