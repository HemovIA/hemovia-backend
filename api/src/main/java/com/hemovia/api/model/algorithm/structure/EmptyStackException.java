package com.hemovia.api.model.algorithm.structure;

public final class EmptyStackException extends RuntimeException {
    public EmptyStackException() { super("Não há operações para desfazer."); }
}
