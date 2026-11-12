package com.hemovia.api.model.algorithm.structure;

public final class EmptyQueueException extends RuntimeException {
    public EmptyQueueException() { super("Não há requisições na fila."); }
}
