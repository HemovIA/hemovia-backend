package com.hemovia.api.domain.valueobject;

import com.hemovia.api.domain.exception.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclo 1 da Seção 3: o CPF guarda só os dígitos e confere os dois dígitos verificadores.
*/
class CpfTest {

    @Test
    void deveAceitarCpfComMascaraEGuardarSoOsDigitos() {
        // Quando criamos o CPF com pontos e traço
        Cpf cpf = new Cpf("529.982.247-25");
        // Então só os dígitos ficam guardados
        assertThat(cpf.digits()).isEqualTo("52998224725");
    }

    @Test
    void deveAceitarCpfSemMascara() {
        // Um CPF válido sem máscara também passa
        assertThat(new Cpf("11144477735").digits()).isEqualTo("11144477735");
    }

    @Test
    void deveTratarDuasFormatacoesDoMesmoCpfComoIguais() {
        // Com máscara ou sem, é o mesmo CPF
        assertThat(new Cpf("529.982.247-25")).isEqualTo(new Cpf("52998224725"));
    }

    @Test
    void deveRejeitarDigitoVerificadorErrado() {
        // O último dígito está trocado (o certo é 5)
        assertThatThrownBy(() -> new Cpf("529.982.247-26")).isInstanceOf(InvalidDataException.class).hasMessage("CPF inválido.");
    }

    @Test
    void deveRejeitarPrimeiroDigitoVerificadorErrado() {
        // O penúltimo dígito está trocado (o certo é 2)
        assertThatThrownBy(() -> new Cpf("529.982.247-35")).isInstanceOf(InvalidDataException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"111.111.111-11", "00000000000", "99999999999"})
    void deveRejeitarSequenciasRepetidas(String sequencia) {
        // Sequências repetidas passam na conta dos dígitos, mas não são CPFs reais
        assertThatThrownBy(() -> new Cpf(sequencia)).isInstanceOf(InvalidDataException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "5299822472", "529982247255", "abc.def.ghi-jk"})
    void deveRejeitarTamanhoErradoOuLetras(String texto) {
        // Menos ou mais de 11 dígitos, ou sem dígito nenhum, não é CPF
        assertThatThrownBy(() -> new Cpf(texto)).isInstanceOf(InvalidDataException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRejeitarCpfNuloOuVazio(String texto) {
        // Campo ausente ou vazio é recusado
        assertThatThrownBy(() -> new Cpf(texto)).isInstanceOf(InvalidDataException.class);
    }
}
