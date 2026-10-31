package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.port.output.LinkedResourceChecker;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.InvalidDataException;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/*
Escolhe o vínculo que combina com o papel e confere se o hospital, hemocentro ou veículo existe.
Vínculo que não combina com o papel, ou mais de um vínculo, é recusado.
*/
@Service
public class UserLinkValidator {

    private final LinkedResourceChecker resources;

    public UserLinkValidator(LinkedResourceChecker resources) {
        this.resources = resources;
    }

    // Devolve o id do vínculo do papel (nulo para ADMIN); lança exceção se algo não bate
    public UUID validate(Role role, UUID hospitalId, UUID bloodCenterId, UUID vehicleId) {
        if (role == null) {
            throw new InvalidDataException("O papel do usuário é obrigatório.");
        }
        UUID chosen = switch (role) {
            case ADMIN -> null;
            case HOSPITAL -> hospitalId;
            case HEMOCENTRO -> bloodCenterId;
            case TRANSPORTADOR -> vehicleId;
        };
        // Só o vínculo do papel pode vir preenchido; qualquer outro é erro do cliente
        long informed = Stream.of(hospitalId, bloodCenterId, vehicleId).filter(Objects::nonNull).count();
        if (informed != (chosen == null ? 0 : 1)) {
            throw new InvalidDataException("O vínculo informado não corresponde ao papel " + role + ".");
        }
        if (chosen != null) {
            requireExists(role, chosen);
        }
        return chosen;
    }

    // Lança "não encontrado" se o recurso do vínculo não existe
    private void requireExists(Role role, UUID id) {
        boolean exists = switch (role) {
            case HOSPITAL -> resources.hospitalExists(id);
            case HEMOCENTRO -> resources.bloodCenterExists(id);
            case TRANSPORTADOR -> resources.vehicleExists(id);
            case ADMIN -> true;
        };
        if (!exists) {
            String resource = switch (role) {
                case HOSPITAL -> "Hospital";
                case HEMOCENTRO -> "Hemocentro";
                default -> "Veículo";
            };
            throw new ResourceNotFoundException(resource, id);
        }
    }
}
