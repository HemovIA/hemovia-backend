package com.hemovia.api.application.port.output;

/*
Porta de saída: os dados do primeiro ADMIN, vindos das variáveis de ambiente.
*/
public interface AdminBootstrapSettings {

    String name();

    String email();

    String cpf();

    String password();

    // Só dá para criar o ADMIN se os quatro dados estiverem preenchidos
    default boolean isConfigured() {
        return isFilled(name()) && isFilled(email()) && isFilled(cpf()) && isFilled(password());
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
