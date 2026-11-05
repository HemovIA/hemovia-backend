package com.hemovia.api.presentation.controller;

import com.hemovia.api.application.dto.request.ChangeUserRoleRequest;
import com.hemovia.api.application.dto.request.CreateUserRequest;
import com.hemovia.api.application.dto.request.UpdateUserRequest;
import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.application.port.input.ChangeUserRoleUseCase;
import com.hemovia.api.application.port.input.CreateUserUseCase;
import com.hemovia.api.application.port.input.DeactivateUserUseCase;
import com.hemovia.api.application.port.input.GetUserUseCase;
import com.hemovia.api.application.port.input.ListUsersUseCase;
import com.hemovia.api.application.port.input.UpdateUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/*
Gestão de usuários. A cadeia de segurança já limita todas estas rotas ao papel ADMIN.
*/
@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase create;
    private final ListUsersUseCase list;
    private final GetUserUseCase get;
    private final UpdateUserUseCase update;
    private final ChangeUserRoleUseCase changeRole;
    private final DeactivateUserUseCase deactivate;

    public UserController(CreateUserUseCase create, ListUsersUseCase list, GetUserUseCase get,
                          UpdateUserUseCase update, ChangeUserRoleUseCase changeRole, DeactivateUserUseCase deactivate) {
        this.create = create;
        this.list = list;
        this.get = get;
        this.update = update;
        this.changeRole = changeRole;
        this.deactivate = deactivate;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(request));
    }

    @GetMapping
    public List<UserResponse> list() {
        return list.execute();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return get.execute(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return update.execute(id, request);
    }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable UUID id, @Valid @RequestBody ChangeUserRoleRequest request) {
        return changeRole.execute(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivate.execute(id);
        return ResponseEntity.noContent().build();
    }
}
