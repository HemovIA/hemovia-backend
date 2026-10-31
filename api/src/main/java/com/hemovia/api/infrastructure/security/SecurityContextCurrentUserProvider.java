package com.hemovia.api.infrastructure.security;

import com.hemovia.api.application.port.output.CurrentUserProvider;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/*
Lê o usuário autenticado do contexto do Spring Security, onde o filtro de cookie o colocou.
*/
@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    @Override
    public AuthenticatedUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // O principal só é do nosso tipo quando o filtro de cookie autenticou a requisição
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new InvalidCredentialsException("Autenticação necessária.");
    }
}
