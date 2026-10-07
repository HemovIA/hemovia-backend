package com.hemovia.api.structures.basic;

public final class EmptyStackException extends RuntimeException {
    public EmptyStackException() { super("Não há operações para desfazer."); }
}
