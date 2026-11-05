package com.hemovia.api.domain.valueobject;

import com.hemovia.api.domain.exception.InvalidDataException;

/*
CPF: guarda só os 11 dígitos e confere os dois dígitos verificadores.
*/
public record Cpf(String digits) {

    // Quantidade de dígitos de um CPF
    private static final int TAMANHO = 11;

    public Cpf {
        // CPF ausente não é CPF
        if (digits == null) {
            throw new InvalidDataException("CPF é obrigatório.");
        }
        // Tiramos pontos, traço e espaços; a partir daqui só existem dígitos
        digits = digits.replaceAll("\\D", "");
        // Tamanho, sequência repetida e dígitos verificadores precisam estar certos
        if (!valido(digits)) {
            throw new InvalidDataException("CPF inválido.");
        }
    }

    // Confere tamanho, sequência repetida e os dois dígitos verificadores
    private static boolean valido(String d) {
        return d.length() == TAMANHO
                && d.chars().distinct().count() > 1
                && d.charAt(9) - '0' == verificador(d, 9)
                && d.charAt(10) - '0' == verificador(d, 10);
    }

    // Calcula o dígito verificador sobre os primeiros "quantidade" dígitos, com pesos de quantidade+1 até 2
    private static int verificador(String d, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (d.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
