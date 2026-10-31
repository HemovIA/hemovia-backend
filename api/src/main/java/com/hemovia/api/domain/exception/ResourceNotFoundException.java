package com.hemovia.api.domain.exception;

/*
Lançada quando um recurso pedido não existe, como um hospital com um id desconhecido.
*/
public final class ResourceNotFoundException extends DomainException {

    // Guardamos o nome do recurso e o id para a mensagem dizer exatamente o que faltou
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " não encontrado: " + id);
    }

    // Código estável que a apresentação traduz em status HTTP
    @Override
    public String code() {
        return "NOT_FOUND";
    }
}
