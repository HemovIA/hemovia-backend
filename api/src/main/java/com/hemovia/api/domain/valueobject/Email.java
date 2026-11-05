package com.hemovia.api.domain.valueobject;

import com.hemovia.api.domain.exception.InvalidDataException;

import java.util.Locale;
import java.util.regex.Pattern;

/*
E-mail: valida o formato e guarda sempre em minúsculas, para duas grafias da mesma caixa não virarem duas contas.
*/
public record Email(String value) {

    // Formato mínimo: algo, arroba, algo, ponto, algo, sem espaços
    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        // E-mail ausente não é e-mail
        if (value == null) {
            throw new InvalidDataException("E-mail é obrigatório.");
        }
        // Normalizamos antes de validar
        value = value.trim().toLowerCase(Locale.ROOT);
        // Fora do formato, recusamos
        if (!FORMATO.matcher(value).matches()) {
            throw new InvalidDataException("E-mail com formato inválido.");
        }
    }
}
