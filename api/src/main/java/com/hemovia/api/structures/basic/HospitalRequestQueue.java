package com.hemovia.api.structures.basic;

import java.util.Objects;

public final class HospitalRequestQueue {
    private Node front;
    private Node rear;
    private int size;

    private static final class Node {
        private final HospitalRequest value;
        private Node next;
        private Node(HospitalRequest value) { this.value = value; }
    }

    public void enqueue(HospitalRequest request) {
        Node node = new Node(Objects.requireNonNull(request, "request"));
        if (rear == null) front = rear = node;
        else { rear.next = node; rear = node; }
        size++;
    }

    public HospitalRequest dequeue() {
        if (front == null) throw new EmptyQueueException();
        HospitalRequest result = front.value;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return result;
    }

    public HospitalRequest peek() {
        if (front == null) throw new EmptyQueueException();
        return front.value;
    }

    public int size() { return size; }
    public boolean isEmpty() { return front == null; }
}
