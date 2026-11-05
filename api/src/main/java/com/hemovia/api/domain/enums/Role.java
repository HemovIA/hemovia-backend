package com.hemovia.api.domain.enums;

/*
Papéis do sistema (decisão D5). Cada usuário tem exatamente um papel.
*/
public enum Role {
    // Administra usuários e cadastros; não pertence a nenhum local
    ADMIN,
    // Funcionário de um hemocentro
    HEMOCENTRO,
    // Funcionário de um hospital
    HOSPITAL,
    // Condutor de um veículo de transporte
    TRANSPORTADOR
}
