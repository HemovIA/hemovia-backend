package com.hemovia.api.controller;

import com.hemovia.api.dto.request.ConfirmDonorRegistrationRequest;
import com.hemovia.api.dto.request.LoginRequest;
import com.hemovia.api.dto.request.StartDonorRegistrationRequest;
import com.hemovia.api.dto.internal.SessionTokens;
import com.hemovia.api.dto.response.UserResponse;
import com.hemovia.api.service.usecase.auth.GetCurrentUser;
import com.hemovia.api.service.usecase.auth.Login;
import com.hemovia.api.service.usecase.auth.Logout;
import com.hemovia.api.service.usecase.auth.RefreshSession;
import com.hemovia.api.service.usecase.auth.RegisterDonor;
import com.hemovia.api.controller.security.SessionCookies;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticação")
public class AuthController {

    private final Login login;
    private final RefreshSession refresh;
    private final Logout logout;
    private final GetCurrentUser currentUser;
    private final SessionCookies cookies;
    private final RegisterDonor registration;

    public AuthController(Login login, RefreshSession refresh, Logout logout,
                          GetCurrentUser currentUser, SessionCookies cookies, RegisterDonor registration) {
        this.login = login;
        this.refresh = refresh;
        this.logout = logout;
        this.currentUser = currentUser;
        this.cookies = cookies;
        this.registration = registration;
    }

    // Entrega o cookie XSRF-TOKEN para o front-end ler e devolver no header das próximas escritas
    @GetMapping("/csrf")
    @Operation(operationId = "authCsrf", summary = "Obter token CSRF")
    @ApiResponse(responseCode = "204", description = "Operação concluída")
    public ResponseEntity<Void> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        // O token é gerado sob demanda; ler o valor força o Spring a gravar o cookie na resposta
        if (token != null) {
            token.getToken();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    @Operation(operationId = "authStartDonorRegistration", summary = "Solicitar confirmação de cadastro por e-mail")
    @ApiResponse(responseCode = "202", description = "Solicitação recebida")
    @ApiResponse(responseCode = "503", description = "Envio de e-mail indisponível")
    public ResponseEntity<Void> startRegistration(@Valid @RequestBody StartDonorRegistrationRequest request) {
        registration.start(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/register/confirm")
    @Operation(operationId = "authConfirmDonorRegistration", summary = "Confirmar e-mail e criar conta de doador")
    @ApiResponse(responseCode = "201", description = "Conta criada")
    @ApiResponse(responseCode = "400", description = "Dados ou link inválidos")
    @ApiResponse(responseCode = "409", description = "Cadastro não pôde ser concluído")
    public ResponseEntity<Void> confirmRegistration(@Valid @RequestBody ConfirmDonorRegistrationRequest request) {
        registration.confirm(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(operationId = "authLogin", summary = "Entrar com e-mail ou CPF")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return withSession(login.execute(request));
    }

    // O refresh token vem do cookie, que o navegador só envia para este caminho
    @PostMapping("/refresh")
    @Operation(operationId = "authRefresh", summary = "Renovar sessão por cookie")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    public ResponseEntity<UserResponse> refresh(@Parameter(hidden = true) @CookieValue(name = SessionCookies.REFRESH, required = false) String refreshToken) {
        return withSession(refresh.execute(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(operationId = "authLogout", summary = "Encerrar sessão")
    @ApiResponse(responseCode = "204", description = "Operação concluída")
    public ResponseEntity<Void> logout(@Parameter(hidden = true) @CookieValue(name = SessionCookies.REFRESH, required = false) String refreshToken) {
        logout.execute(refreshToken);
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.NO_CONTENT);
        cookies.clear().forEach(cookie -> response.header(HttpHeaders.SET_COOKIE, cookie));
        return response.build();
    }

    @GetMapping("/me")
    @Operation(operationId = "authMe", summary = "Consultar usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
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
