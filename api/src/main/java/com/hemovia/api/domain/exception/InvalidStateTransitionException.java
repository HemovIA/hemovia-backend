package com.hemovia.api.domain.exception;

/*
Lançada quando uma mudança de estado é proibida, como reativar uma bolsa descartada.
Estende IllegalStateException porque a história POOUS09 pede exatamente isso.
*/
public final class InvalidStateTransitionException extends IllegalStateException {

    // A mensagem diz de qual estado para qual estado a transição foi barrada
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
