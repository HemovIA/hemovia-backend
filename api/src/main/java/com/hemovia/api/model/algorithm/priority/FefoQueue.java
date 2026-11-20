package com.hemovia.api.model.algorithm.priority;

import com.hemovia.api.model.entity.BloodBag;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

// Prepares a FEFO queue from bags already filtered for compatibility.
public final class FefoQueue {
    private FefoQueue() { }

    // Returns a new queue containing only bags expiring after the reference date.
    public static Queue<BloodBag> prepare(Collection<BloodBag> compatibleBags, LocalDate referenceDate) {
        Objects.requireNonNull(compatibleBags, "compatibleBags");
        Objects.requireNonNull(referenceDate, "referenceDate");
        Queue<BloodBag> queue = new PriorityQueue<>(Comparator.comparing(BloodBag::getExpirationDate));
        for (BloodBag bag : compatibleBags) {
            if (bag.getExpirationDate().isAfter(referenceDate)) {
                queue.add(bag);
            }
        }
        return queue;
    }
}
