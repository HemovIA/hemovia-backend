package com.hemovia.api.domain.model;

import com.hemovia.api.domain.enums.Role;

import java.util.UUID;

/*
Quem está fazendo a requisição: o usuário já autenticado, com o papel e o vínculo que o token trouxe.
Os métodos respondem se ele pertence a um hospital, hemocentro ou veículo, para as regras de acesso por vínculo.
*/
public record AuthenticatedUser(UUID userId, Role role, UUID hospitalId, UUID bloodCenterId, UUID vehicleId) {

    // Só o papel HOSPITAL trabalha em hospital, e apenas no hospital do seu vínculo
    public boolean worksAtHospital(UUID hospital) {
        return role == Role.HOSPITAL && hospital != null && hospital.equals(hospitalId);
    }

    // Só o papel HEMOCENTRO trabalha em hemocentro, e apenas no hemocentro do seu vínculo
    public boolean worksAtBloodCenter(UUID bloodCenter) {
        return role == Role.HEMOCENTRO && bloodCenter != null && bloodCenter.equals(bloodCenterId);
    }

    // Só o papel TRANSPORTADOR conduz veículo, e apenas o do seu vínculo
    public boolean drivesVehicle(UUID vehicle) {
        return role == Role.TRANSPORTADOR && vehicle != null && vehicle.equals(vehicleId);
    }
}
