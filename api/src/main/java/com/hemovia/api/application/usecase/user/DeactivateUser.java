package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.port.input.DeactivateUserUseCase;
import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.application.port.output.RefreshTokenRepository;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import com.hemovia.api.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
Desativa o usuário e revoga todas as sessões dele, para ele ser expulso em vez de esperar o token vencer.
*/
@Service
@Transactional
public class DeactivateUser implements DeactivateUserUseCase {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final ClockProvider clock;

    public DeactivateUser(UserRepository users, RefreshTokenRepository refreshTokens, ClockProvider clock) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.clock = clock;
    }

    @Override
    public void execute(UUID userId) {
        User user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        user.deactivate();
        users.save(user);
        refreshTokens.revokeAllOfUser(userId, clock.now());
    }
}
