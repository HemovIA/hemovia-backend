package com.hemovia.api.service.usecase.auth;

import com.hemovia.api.dto.response.UserResponse;
import com.hemovia.api.service.port.input.GetCurrentUserUseCase;
import com.hemovia.api.service.port.output.CurrentUserProvider;
import com.hemovia.api.repository.port.UserRepository;
import com.hemovia.api.model.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
Devolve os dados do usuário que fez a requisição. Se o dono do token já não existe, a sessão não vale mais.
*/
@Service
@Transactional(readOnly = true)
public class GetCurrentUser implements GetCurrentUserUseCase {

    private final UserRepository users;
    private final CurrentUserProvider currentUser;

    public GetCurrentUser(UserRepository users, CurrentUserProvider currentUser) {
        this.users = users;
        this.currentUser = currentUser;
    }

    @Override
    public UserResponse execute() {
        return users.findById(currentUser.current().userId())
                .map(UserResponse::from)
                .orElseThrow(InvalidCredentialsException::new);
    }
}
