package com.hemovia.api.presentation.handler;

import com.hemovia.api.application.port.output.ClockProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/*
Responde as falhas que acontecem na cadeia de segurança, antes de qualquer controller:
401 quando não há sessão válida e 403 quando falta permissão ou o token CSRF. Sai no mesmo formato ApiError.
*/
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ClockProvider clock;

    public SecurityErrorHandler(ClockProvider clock) {
        this.clock = clock;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        write(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Autenticação necessária ou credenciais inválidas.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, "FORBIDDEN", "Você não tem permissão para acessar este recurso.");
    }

    // Escreve o JSON na mão: o corpo é pequeno e fixo, e assim não dependemos do conversor do MVC (que ainda nem rodou)
    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        String json = "{\"timestamp\":\"" + clock.now() + "\",\"status\":" + status.value()
                + ",\"error\":\"" + code + "\",\"message\":\"" + message + "\",\"path\":\""
                + escape(request.getRequestURI()) + "\",\"fieldErrors\":{}}";
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(json);
    }

    // O caminho vem do cliente, então aspas e barras invertidas precisam de escape para o JSON continuar válido
    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
