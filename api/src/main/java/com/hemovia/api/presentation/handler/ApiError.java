package com.hemovia.api.presentation.handler;

import java.time.Instant;
import java.util.Map;

/*
Formato único de toda resposta de erro da API (doc, seção 35).
O campo error é um código estável para o front-end, e fieldErrors só vem preenchido em erro de validação.
*/
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) { }
