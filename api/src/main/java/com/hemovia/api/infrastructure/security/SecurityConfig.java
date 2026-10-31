package com.hemovia.api.infrastructure.security;

import com.hemovia.api.application.port.output.AccessTokenIssuer;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.infrastructure.configuration.ApiPrefixConfiguration;
import com.hemovia.api.infrastructure.configuration.HemoviaProperties;
import com.hemovia.api.presentation.handler.SecurityErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/*
Regras de segurança da API: sessão por cookie HttpOnly, proteção CSRF, CORS com origens explícitas
e papéis por rota. Nenhuma rota de negócio é pública.
*/
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Rotas de autenticação, já com o prefixo da API
    private static final String AUTH = ApiPrefixConfiguration.PREFIXO + "/auth";
    private static final String LOGIN = AUTH + "/login";
    private static final String TELEMETRY = ApiPrefixConfiguration.PREFIXO + "/telemetry";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AccessTokenIssuer accessTokens,
                                                   SecurityErrorHandler errorHandler) throws Exception {
        // O filtro é criado aqui, e não como @Component, para o Spring não registrá-lo uma segunda vez fora da cadeia
        JwtCookieAuthenticationFilter cookieFilter = new JwtCookieAuthenticationFilter(accessTokens);
        http
            // O token CSRF vai num cookie legível pelo JavaScript, que o devolve no header X-XSRF-TOKEN
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers(LOGIN, TELEMETRY))
            .cors(cors -> { })
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(handling -> handling.authenticationEntryPoint(errorHandler).accessDeniedHandler(errorHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, AUTH + "/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, LOGIN, AUTH + "/refresh").permitAll()
                .requestMatchers(AUTH + "/**").authenticated()
                .requestMatchers(ApiPrefixConfiguration.PREFIXO + "/users/**").hasRole(Role.ADMIN.name())
                // Usuário sem papel não tem nenhuma autoridade, então cai aqui em 403
                .anyRequest().hasAnyRole(Role.ADMIN.name(), Role.HEMOCENTRO.name(), Role.HOSPITAL.name(), Role.TRANSPORTADOR.name()))
            .addFilterBefore(cookieFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // CORS com credenciais exige origens explícitas; o curinga "*" não funciona junto com cookies
    @Bean
    public CorsConfigurationSource corsConfigurationSource(HemoviaProperties properties) {
        List<String> origins = Arrays.stream(properties.security().corsAllowedOrigins().split(","))
                .map(String::trim).filter(origin -> !origin.isEmpty()).toList();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
