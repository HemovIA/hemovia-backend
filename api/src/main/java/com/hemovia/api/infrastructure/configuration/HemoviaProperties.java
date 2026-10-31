package com.hemovia.api.infrastructure.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/*
Propriedades da aplicação sob o prefixo "hemovia", lidas do application.yaml e validadas ao subir.
Se algo obrigatório faltar, a aplicação não sobe: melhor falhar na partida do que rodar insegura.
*/
@ConfigurationProperties(prefix = "hemovia")
@Validated
public record HemoviaProperties(
        @Valid @NotNull Security security,
        @Valid @DefaultValue AdminBootstrap adminBootstrap) {

    // Configurações de segurança: segredo do JWT, duração das sessões, cookies e CORS
    public record Security(
            // Sem segredo ou com menos de 32 caracteres a assinatura do token seria fraca demais
            @NotBlank @Size(min = 32) String jwtSecret,
            // Duração do access token em minutos
            @Min(1) int accessTokenMinutes,
            // Duração do refresh token em dias
            @Min(1) int refreshTokenDays,
            // Cookie só trafega por HTTPS quando verdadeiro
            boolean cookieSecure,
            // Política SameSite dos cookies, como Lax ou Strict
            @NotBlank String cookieSameSite,
            // Origens do front-end autorizadas pelo CORS, separadas por vírgula
            @NotBlank String corsAllowedOrigins) { }

    // Dados do primeiro ADMIN, criado na subida a partir de variáveis de ambiente; podem ficar vazios
    public record AdminBootstrap(String name, String email, String cpf, String password) { }
}
