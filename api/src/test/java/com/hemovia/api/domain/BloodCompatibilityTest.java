package com.hemovia.api.domain;

import com.hemovia.api.model.enums.BloodType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AEDUS07 — Matriz de Compatibilidade Sanguínea ABO/Rh.
 * Escrito ANTES de BloodCompatibility (commit "test:" seguido de "feat:").
 */
class BloodCompatibilityTest {

    @ParameterizedTest(name = "doador {0}{1} -> receptor {2}{3} = {4}")
    @CsvSource({
            // doador, rh+, receptor, rh+, compatível
            "O,  false, A,  true,  true",   // O- é doador universal
            "O,  false, AB, false, true",
            "A,  true,  O,  false, false",  // cenário negativo da história: receptor O- com doador A+
            "A,  false, A,  true,  true",   // Rh- pode doar para Rh+
            "A,  true,  A,  false, false",  // Rh+ não pode doar para Rh-
            "B,  false, AB, false, true",
            "AB, true,  AB, true,  true",
            "AB, false, A,  false, false",  // AB só doa para AB
            "B,  true,  A,  true,  false",
            "O,  true,  O,  false, false"
    })
    @DisplayName("Valida as regras ABO e Rh para transfusão de hemácias")
    void shouldApplyAboAndRhRules(BloodType donor, boolean donorRhPositive,
                                  BloodType recipient, boolean recipientRhPositive,
                                  boolean expected) {
        assertThat(BloodCompatibility.canTransfuse(donor, donorRhPositive, recipient, recipientRhPositive))
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("A matriz 8x8 tem exatamente 27 combinações compatíveis")
    void shouldHaveExactly27CompatiblePairs() {
        int compatible = 0;
        boolean[] rh = {false, true};
        for (BloodType donor : BloodType.values()) {
            for (boolean donorRh : rh) {
                for (BloodType recipient : BloodType.values()) {
                    for (boolean recipientRh : rh) {
                        if (BloodCompatibility.canTransfuse(donor, donorRh, recipient, recipientRh)) {
                            compatible++;
                        }
                    }
                }
            }
        }
        assertThat(compatible).isEqualTo(27);
    }

    @Test
    @DisplayName("Tipo sanguíneo nulo é rejeitado")
    void shouldRejectNullBloodType() {
        assertThatThrownBy(() -> BloodCompatibility.canTransfuse(null, true, BloodType.A, true))
                .isInstanceOf(NullPointerException.class);
    }
}
