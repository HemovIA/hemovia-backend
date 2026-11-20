package com.hemovia.api.service.usecase.user;

import com.hemovia.api.dto.request.ChangeUserRoleRequest;
import com.hemovia.api.dto.response.UserResponse;
import com.hemovia.api.repository.port.UserRepository;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
Troca o papel e o vínculo de um usuário de uma vez só.
*/
@Service
@Transactional
public class ChangeUserRole {

    private final UserRepository users;
    private final UserLinkValidator links;

    public ChangeUserRole(UserRepository users, UserLinkValidator links) {
        this.users = users;
        this.links = links;
    }

    public UserResponse execute(UUID userId, ChangeUserRoleRequest request) {
        User user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        UUID linkedId = links.validate(request.role(), request.hospitalId(), request.bloodCenterId(), request.vehicleId());
        user.changeRole(request.role(), linkedId);
        return UserResponse.from(users.save(user));
    }
}
