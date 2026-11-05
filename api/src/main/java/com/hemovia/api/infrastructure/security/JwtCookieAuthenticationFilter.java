package com.hemovia.api.infrastructure.security;

import com.hemovia.api.application.port.output.AccessTokenIssuer;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/*
Lê o access token do cookie HEMOVIA_ACCESS e, se for válido, autentica a requisição.
Token inválido ou vencido deixa a requisição anônima, e a cadeia de segurança responde 401 nas rotas protegidas.
*/
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    // O motivo da recusa fica no log de depuração; nada disso vai para o cliente
    private static final Logger LOG = LoggerFactory.getLogger(JwtCookieAuthenticationFilter.class);

    private final AccessTokenIssuer accessTokens;

    public JwtCookieAuthenticationFilter(AccessTokenIssuer accessTokens) {
        this.accessTokens = accessTokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        accessCookie(request).ifPresent(this::authenticate);
        chain.doFilter(request, response);
    }

    // Procura o cookie de acesso entre os cookies da requisição
    private java.util.Optional<String> accessCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return java.util.Optional.empty();
        }
        return Stream.of(cookies).filter(cookie -> SessionCookies.ACCESS.equals(cookie.getName()))
                .map(Cookie::getValue).filter(value -> !value.isBlank()).findFirst();
    }

    // Confere o token e coloca o usuário no contexto; sem papel, o usuário fica autenticado mas sem nenhuma autoridade
    private void authenticate(String token) {
        try {
            AuthenticatedUser user = accessTokens.verify(token);
            List<GrantedAuthority> authorities = user.role() == null
                    ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities));
        } catch (InvalidCredentialsException invalid) {
            LOG.debug("Access token recusado: {}", invalid.getMessage());
        }
    }
}
