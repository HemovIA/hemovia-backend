package com.hemovia.api.service.implementation;

import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.exception.DuplicateResourceException;
import com.hemovia.api.model.DTOs.AuthResponseDTO;
import com.hemovia.api.model.DTOs.LoginRequestDTO;
import com.hemovia.api.model.DTOs.RegisterRequestDTO;
import com.hemovia.api.model.DTOs.UserResponseDTO;
import com.hemovia.api.model.User;
import com.hemovia.api.repository.UserRepository;
import com.hemovia.api.service.AuthService;
import com.hemovia.api.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UserResponseDTO register(RegisterRequestDTO dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        String normalizedCpf = normalizeCpf(dto.getCpf());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("E-mail já cadastrado");
        }

        if (userRepository.existsByCpf(normalizedCpf)) {
            throw new DuplicateResourceException("CPF já cadastrado");
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(normalizedEmail);
        user.setCpf(normalizedCpf);
        user.setPhoneNumber(dto.getPhoneNumber() != null ? dto.getPhoneNumber().trim() : null);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);
        return toResponseDTO(savedUser);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        String identifier = dto.getIdentifier();
        if (identifier == null || identifier.isBlank() || dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new InvalidCredentialsException("E-mail/CPF e senha são obrigatórios");
        }

        String trimmedIdentifier = identifier.trim();
        Optional<User> userOptional;

        if (trimmedIdentifier.contains("@")) {
            userOptional = userRepository.findByEmail(trimmedIdentifier.toLowerCase());
        } else {
            String digits = normalizeCpf(trimmedIdentifier);
            userOptional = userRepository.findByCpf(digits);
            if (userOptional.isEmpty()) {
                userOptional = userRepository.findByEmail(trimmedIdentifier.toLowerCase());
            }
        }

        User user = userOptional.orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user.getId());
        return new AuthResponseDTO(token);
    }

    private String normalizeCpf(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("[^0-9]", "");
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getPhoneNumber()
        );
    }
}
