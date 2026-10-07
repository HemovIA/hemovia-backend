package com.hemovia.api.structures.basic;

public final class EmptyQueueException extends RuntimeException {
    public EmptyQueueException() { super("Não há requisições na fila."); }
}
