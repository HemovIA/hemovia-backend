package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.dto.request.ChangeUserRoleRequest;
import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.application.port.input.ChangeUserRoleUseCase;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import com.hemovia.api.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
Troca o papel e o vínculo de um usuário de uma vez só.
*/
@Service
@Transactional
public class ChangeUserRole implements ChangeUserRoleUseCase {

    private final UserRepository users;
    private final UserLinkValidator links;

    public ChangeUserRole(UserRepository users, UserLinkValidator links) {
        this.users = users;
        this.links = links;
    }

    @Override
    public UserResponse execute(UUID userId, ChangeUserRoleRequest request) {
        User user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        UUID linkedId = links.validate(request.role(), request.hospitalId(), request.bloodCenterId(), request.vehicleId());
        user.changeRole(request.role(), linkedId);
        return UserResponse.from(users.save(user));
    }
}
