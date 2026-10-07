package com.hemovia.api.structures.basic;

import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.enums.BloodBagStatus;
import java.util.Objects;

public final class StockHistoryStack {
    private Node top;
    private int size;

    private static final class Node {
        private final StockOperation value;
        private Node next;
        private Node(StockOperation value) { this.value = value; }
    }

    public void push(StockOperation operation) {
        Node node = new Node(Objects.requireNonNull(operation, "operation"));
        node.next = top;
        top = node;
        size++;
    }

    public StockOperation pop() {
        if (top == null) throw new EmptyStackException();
        StockOperation result = top.value;
        top = top.next;
        size--;
        return result;
    }

    public StockOperation peek() {
        if (top == null) throw new EmptyStackException();
        return top.value;
    }

    /** Desempilha e restaura na bolsa o status anterior registrado no evento. */
    public StockOperation undo(BloodBagStockList stock) {
        StockOperation operation = peek();
        BloodBag bag = stock.findById(operation.bagId());
        if (bag == null) throw new IllegalArgumentException("Bolsa não encontrada: " + operation.bagId());
        BloodBagStatus previous = BloodBagStatus.valueOf(operation.previousStatus());
        operation = pop();
        bag.setStatus(previous);
        return operation;
    }

    public int size() { return size; }
    public boolean isEmpty() { return top == null; }
}
