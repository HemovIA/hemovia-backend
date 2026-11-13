package com.hemovia.api.model.algorithm.priority;

import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.enums.BloodBagStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FefoQueueTest {
    private static final LocalDate REFERENCE = LocalDate.of(2030, 1, 10);

    @Test
    @DisplayName("@spec:AC-026 bolsas com menor validade têm prioridade")
    void ordenaBolsasPorValidadeMaisProxima() {
        BloodBag threeDays = bag(REFERENCE.plusDays(3));
        BloodBag fifteenDays = bag(REFERENCE.plusDays(15));
        BloodBag oneDay = bag(REFERENCE.plusDays(1));
        List<BloodBag> input = new ArrayList<>(List.of(threeDays, fifteenDays, oneDay));

        Queue<BloodBag> queue = FefoQueue.prepare(input, REFERENCE);

        assertSame(oneDay, queue.poll());
        assertSame(threeDays, queue.poll());
        assertSame(fifteenDays, queue.poll());
        assertNull(queue.poll());
        assertEquals(List.of(threeDays, fifteenDays, oneDay), input);
        assertEquals(REFERENCE.plusDays(3), threeDays.getExpirationDate());
        assertEquals(REFERENCE.plusDays(15), fifteenDays.getExpirationDate());
        assertEquals(REFERENCE.plusDays(1), oneDay.getExpirationDate());
        assertEquals(BloodBagStatus.AVAILABLE, threeDays.getStatus());
        assertEquals(BloodBagStatus.AVAILABLE, fifteenDays.getStatus());
        assertEquals(BloodBagStatus.AVAILABLE, oneDay.getStatus());
    }

    @Test
    @DisplayName("@spec:AC-027 bolsas vencidas ou que vencem hoje não entram na fila")
    void excluiBolsasVencidasOuQueVencemNaDataDeReferencia() {
        BloodBag expired = bag(REFERENCE.minusDays(1));
        BloodBag today = bag(REFERENCE);
        BloodBag eligible = bag(REFERENCE.plusDays(1));
        List<BloodBag> input = new ArrayList<>(List.of(expired, today, eligible));

        Queue<BloodBag> queue = FefoQueue.prepare(input, REFERENCE);

        assertSame(eligible, queue.poll());
        assertNull(queue.poll());
        assertEquals(List.of(expired, today, eligible), input);
        assertTrue(FefoQueue.prepare(List.of(expired, today), REFERENCE).isEmpty());
        assertTrue(FefoQueue.prepare(List.of(), REFERENCE).isEmpty());
    }

    private static BloodBag bag(LocalDate expirationDate) {
        BloodBag bag = new BloodBag();
        bag.setExpirationDate(expirationDate);
        bag.setStatus(BloodBagStatus.AVAILABLE);
        return bag;
    }
}
