package com.hemovia.api.presentation.controller;

import com.hemovia.api.application.dto.request.LoginRequest;
import com.hemovia.api.application.dto.response.SessionTokens;
import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.application.port.input.GetCurrentUserUseCase;
import com.hemovia.api.application.port.input.LoginUseCase;
import com.hemovia.api.application.port.input.LogoutUseCase;
import com.hemovia.api.application.port.input.RefreshSessionUseCase;
import com.hemovia.api.infrastructure.security.SessionCookies;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
Rotas de autenticação. Os tokens viajam só em cookies HttpOnly e nunca aparecem no corpo da resposta.
O prefixo /api/v1 é acrescentado automaticamente a todo @RestController.
*/
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase login;
    private final RefreshSessionUseCase refresh;
    private final LogoutUseCase logout;
    private final GetCurrentUserUseCase currentUser;
    private final SessionCookies cookies;

    public AuthController(LoginUseCase login, RefreshSessionUseCase refresh, LogoutUseCase logout,
                          GetCurrentUserUseCase currentUser, SessionCookies cookies) {
        this.login = login;
        this.refresh = refresh;
        this.logout = logout;
        this.currentUser = currentUser;
        this.cookies = cookies;
    }

    // Entrega o cookie XSRF-TOKEN para o front-end ler e devolver no header das próximas escritas
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        // O token é gerado sob demanda; ler o valor força o Spring a gravar o cookie na resposta
        if (token != null) {
            token.getToken();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return withSession(login.execute(request));
    }

    // O refresh token vem do cookie, que o navegador só envia para este caminho
    @PostMapping("/refresh")
    public ResponseEntity<UserResponse> refresh(@CookieValue(name = SessionCookies.REFRESH, required = false) String refreshToken) {
        return withSession(refresh.execute(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = SessionCookies.REFRESH, required = false) String refreshToken) {
        logout.execute(refreshToken);
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.NO_CONTENT);
        cookies.clear().forEach(cookie -> response.header(HttpHeaders.SET_COOKIE, cookie));
        return response.build();
    }

    @GetMapping("/me")
    public UserResponse me() {
        return currentUser.execute();
    }

    // Responde 200 com o usuário no corpo e os dois cookies de sessão nos headers
    private ResponseEntity<UserResponse> withSession(SessionTokens tokens) {
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        cookies.open(tokens).forEach(cookie -> response.header(HttpHeaders.SET_COOKIE, cookie));
        return response.body(tokens.user());
    }
}
