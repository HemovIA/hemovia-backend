package com.hemovia.api.domain.valueobject;

import com.hemovia.api.domain.exception.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclo 2 da Seção 3: o e-mail é normalizado para minúsculas e precisa ter formato válido.
*/
class EmailTest {

    @Test
    void deveNormalizarParaMinusculasEAparar() {
        // Maiúsculas e espaços nas pontas não devem criar dois e-mails diferentes
        assertThat(new Email("  Alice.Sena@Hemovia.COM ").value()).isEqualTo("alice.sena@hemovia.com");
    }

    @Test
    void deveTratarFormatacoesDiferentesComoOMesmoEmail() {
        // Mesma pessoa, mesma caixa de entrada
        assertThat(new Email("ALICE@hemovia.com")).isEqualTo(new Email("alice@hemovia.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sem-arroba", "@hemovia.com", "alice@", "alice@hemovia", "alice sena@hemovia.com", "a@@b.com"})
    void deveRejeitarFormatoInvalido(String texto) {
        // Qualquer coisa fora do formato usuario@dominio.sufixo é recusada
        assertThatThrownBy(() -> new Email(texto)).isInstanceOf(InvalidDataException.class).hasMessage("E-mail com formato inválido.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRejeitarEmailNuloOuVazio(String texto) {
        // Campo ausente ou vazio também é recusado
        assertThatThrownBy(() -> new Email(texto)).isInstanceOf(InvalidDataException.class);
    }
}
