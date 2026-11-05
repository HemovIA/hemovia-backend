package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.application.port.input.ListUsersUseCase;
import com.hemovia.api.application.port.output.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
Lista todos os usuários.
*/
@Service
@Transactional(readOnly = true)
public class ListUsers implements ListUsersUseCase {

    private final UserRepository users;

    public ListUsers(UserRepository users) {
        this.users = users;
    }

    @Override
    public List<UserResponse> execute() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }
}
