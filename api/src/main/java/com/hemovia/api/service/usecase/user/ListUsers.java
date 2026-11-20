package com.hemovia.api.service.usecase.user;

import com.hemovia.api.dto.response.UserResponse;
import com.hemovia.api.repository.port.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
Lista todos os usuários.
*/
@Service
@Transactional(readOnly = true)
public class ListUsers {

    private final UserRepository users;

    public ListUsers(UserRepository users) {
        this.users = users;
    }

    public List<UserResponse> execute() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }
}
