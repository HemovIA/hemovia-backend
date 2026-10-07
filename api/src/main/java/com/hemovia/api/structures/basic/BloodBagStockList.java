package com.hemovia.api.structures.basic;

import com.hemovia.api.model.BloodBag;
import java.util.Objects;
import java.util.UUID;

/** Lista simplesmente encadeada em memória, independente do repositório JPA. */
public final class BloodBagStockList {
    private Node head;
    private int size;

    private static final class Node {
        private final BloodBag value;
        private Node next;
        private Node(BloodBag value) { this.value = value; }
    }

    public void add(BloodBag bag) {
        Objects.requireNonNull(bag, "bag");
        Node node = new Node(bag);
        if (head == null) head = node;
        else {
            Node current = head;
            while (current.next != null) current = current.next;
            current.next = node;
        }
        size++;
    }

    public BloodBag findById(UUID id) {
        for (Node current = head; current != null; current = current.next)
            if (Objects.equals(current.value.getId(), id)) return current.value;
        return null;
    }

    public boolean removeById(UUID id) {
        Node previous = null, current = head;
        while (current != null) {
            if (Objects.equals(current.value.getId(), id)) {
                if (previous == null) head = current.next;
                else previous.next = current.next;
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    public int size() { return size; }
    public boolean isEmpty() { return head == null; }
}
