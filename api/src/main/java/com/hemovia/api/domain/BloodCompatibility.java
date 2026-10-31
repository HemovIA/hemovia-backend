package com.hemovia.api.domain;

import com.hemovia.api.model.enums.BloodType;

import java.util.Objects;

/**
 * AEDUS07 — Matriz de compatibilidade ABO/Rh para transfusão de hemácias.
 * A matriz 8x8 é montada uma única vez; cada consulta é um acesso O(1)
 * a matrix[receptor][doador].
 */
public final class BloodCompatibility {

    private static final boolean[][] MATRIX = build();

    private BloodCompatibility() {
    }

    public static boolean canTransfuse(BloodType donorType, boolean donorRhPositive,
                                       BloodType recipientType, boolean recipientRhPositive) {
        Objects.requireNonNull(donorType, "donorType");
        Objects.requireNonNull(recipientType, "recipientType");
        return MATRIX[index(recipientType, recipientRhPositive)][index(donorType, donorRhPositive)];
    }

    private static int index(BloodType type, boolean rhPositive) {
        return type.ordinal() * 2 + (rhPositive ? 1 : 0);
    }

    private static boolean[][] build() {
        BloodType[] types = BloodType.values();
        boolean[][] matrix = new boolean[types.length * 2][types.length * 2];
        boolean[] rh = {false, true};
        for (BloodType recipient : types) {
            for (boolean recipientRh : rh) {
                for (BloodType donor : types) {
                    for (boolean donorRh : rh) {
                        matrix[index(recipient, recipientRh)][index(donor, donorRh)] =
                                aboCompatible(donor, recipient) && (!donorRh || recipientRh);
                    }
                }
            }
        }
        return matrix;
    }

    private static boolean aboCompatible(BloodType donor, BloodType recipient) {
        return switch (donor) {
            case O -> true;
            case A -> recipient == BloodType.A || recipient == BloodType.AB;
            case B -> recipient == BloodType.B || recipient == BloodType.AB;
            case AB -> recipient == BloodType.AB;
        };
    }
}
