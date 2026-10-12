package com.hemovia.api.controller;

import com.hemovia.api.model.DTOs.AuthResponseDTO;
import com.hemovia.api.model.DTOs.LoginRequestDTO;
import com.hemovia.api.model.DTOs.RegisterRequestDTO;
import com.hemovia.api.model.DTOs.UserResponseDTO;
import com.hemovia.api.model.User;
import com.hemovia.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDTO) {
        UserResponseDTO responseDTO = authService.register(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        AuthResponseDTO responseDTO = authService.login(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getAuthenticatedUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getPhoneNumber()
        ));
    }
}
