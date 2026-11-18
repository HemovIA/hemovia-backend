package com.hemovia.api.controller;

import com.hemovia.api.dto.request.ChangeUserRoleRequest;
import com.hemovia.api.dto.request.CreateUserRequest;
import com.hemovia.api.dto.request.UpdateUserRequest;
import com.hemovia.api.dto.response.UserResponse;
import com.hemovia.api.service.usecase.user.ChangeUserRole;
import com.hemovia.api.service.usecase.user.CreateUser;
import com.hemovia.api.service.usecase.user.DeactivateUser;
import com.hemovia.api.service.usecase.user.GetUser;
import com.hemovia.api.service.usecase.user.ListUsers;
import com.hemovia.api.service.usecase.user.UpdateUser;
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

    private final CreateUser create;
    private final ListUsers list;
    private final GetUser get;
    private final UpdateUser update;
    private final ChangeUserRole changeRole;
    private final DeactivateUser deactivate;

    public UserController(CreateUser create, ListUsers list, GetUser get,
                          UpdateUser update, ChangeUserRole changeRole, DeactivateUser deactivate) {
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
