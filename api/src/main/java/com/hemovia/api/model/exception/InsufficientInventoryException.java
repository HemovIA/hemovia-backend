package com.hemovia.api.model.exception;

public final class InsufficientInventoryException extends DomainException {
    public InsufficientInventoryException() { super("Estoque compatível insuficiente para atender à requisição."); }
    @Override public String code() { return "INSUFFICIENT_INVENTORY"; }
}
