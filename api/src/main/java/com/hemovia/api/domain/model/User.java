package com.hemovia.api.domain.model;

import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.InvalidDataException;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;

import java.util.UUID;

/*
Usuário do sistema. Tem exatamente um papel e, conforme o papel, um vínculo com hospital, hemocentro ou veículo.
Usuários antigos, de antes dos papéis, existem com papel nulo e não conseguem entrar.
*/
public final class User {

    // Identificador do usuário
    private final UUID id;
    // Dados pessoais
    private String name;
    private Email email;
    private final Cpf cpf;
    private String phoneNumber;
    // Hash da senha; a senha pura nunca chega ao domínio
    private final String passwordHash;
    // Papel e situação
    private Role role;
    private boolean active;
    // Vínculos: só um deles é preenchido, o que o papel pede
    private UUID hospitalId;
    private UUID bloodCenterId;
    private UUID vehicleId;

    private User(UUID id, String name, Email email, Cpf cpf, String phoneNumber, String passwordHash,
                 Role role, boolean active, UUID hospitalId, UUID bloodCenterId, UUID vehicleId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.hospitalId = hospitalId;
        this.bloodCenterId = bloodCenterId;
        this.vehicleId = vehicleId;
    }

    // Cria um usuário novo, ativo, com papel e vínculo válidos; "linkedId" é o id do hospital, hemocentro ou veículo
    public static User create(String name, Email email, Cpf cpf, String phoneNumber, String passwordHash,
                              Role role, UUID linkedId) {
        User user = new User(UUID.randomUUID(), requireName(name), email, cpf, clean(phoneNumber), passwordHash,
                null, true, null, null, null);
        user.assignRole(role, linkedId);
        return user;
    }

    // Reconstrói um usuário já guardado, inclusive os antigos sem papel
    public static User restore(UUID id, String name, Email email, Cpf cpf, String phoneNumber, String passwordHash,
                               Role role, boolean active, UUID hospitalId, UUID bloodCenterId, UUID vehicleId) {
        return new User(id, name, email, cpf, phoneNumber, passwordHash, role, active, hospitalId, bloodCenterId, vehicleId);
    }

    // Troca o papel e o vínculo juntos; se a troca for inválida, nada muda
    public void changeRole(Role newRole, UUID linkedId) {
        assignRole(newRole, linkedId);
    }

    // Atualiza os dados de contato
    public void updateProfile(String newName, Email newEmail, String newPhoneNumber) {
        this.name = requireName(newName);
        this.email = newEmail;
        this.phoneNumber = clean(newPhoneNumber);
    }

    // Desativa o usuário; ele deixa de conseguir entrar
    public void deactivate() {
        this.active = false;
    }

    // Só entra quem está ativo e tem papel
    public boolean canLogIn() {
        return active && role != null;
    }

    // Valida o par papel/vínculo e só então grava os três campos de uma vez
    private void assignRole(Role newRole, UUID linkedId) {
        if (newRole == null) {
            throw new InvalidDataException("O papel do usuário é obrigatório.");
        }
        boolean needsLink = newRole != Role.ADMIN;
        if (needsLink && linkedId == null) {
            throw new InvalidDataException("O papel " + newRole + " exige um vínculo.");
        }
        if (!needsLink && linkedId != null) {
            throw new InvalidDataException("O papel ADMIN não pode ter vínculo.");
        }
        this.role = newRole;
        this.hospitalId = newRole == Role.HOSPITAL ? linkedId : null;
        this.bloodCenterId = newRole == Role.HEMOCENTRO ? linkedId : null;
        this.vehicleId = newRole == Role.TRANSPORTADOR ? linkedId : null;
    }

    // O nome é obrigatório e é guardado sem espaços nas pontas
    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("O nome é obrigatório.");
        }
        return name.trim();
    }

    // Telefone opcional: vazio vira nulo
    private static String clean(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Email email() {
        return email;
    }

    public Cpf cpf() {
        return cpf;
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public UUID hospitalId() {
        return hospitalId;
    }

    public UUID bloodCenterId() {
        return bloodCenterId;
    }

    public UUID vehicleId() {
        return vehicleId;
    }
}
