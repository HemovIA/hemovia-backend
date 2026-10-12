package com.hemovia.api.service.implementation;

import com.hemovia.api.exceptions.InvalidCredentialsException;
import com.hemovia.api.exceptions.UserAlreadyExistsException;
import com.hemovia.api.model.DTOs.AuthResponseDTO;
import com.hemovia.api.model.DTOs.LoginRequestDTO;
import com.hemovia.api.model.DTOs.RegisterRequestDTO;
import com.hemovia.api.model.DTOs.UserResponseDTO;
import com.hemovia.api.model.User;
import com.hemovia.api.repository.UserRepository;
import com.hemovia.api.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Alice Sena",
                "Alice.Sena@Hemovia.com",
                "123.456.789-00",
                "81999998888",
                "senhaForte123"
        );

        when(userRepository.existsByEmail("alice.sena@hemovia.com")).thenReturn(false);
        when(userRepository.existsByCpf("12345678900")).thenReturn(false);
        when(passwordEncoder.encode("senhaForte123")).thenReturn("hash_bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponseDTO response = authService.register(dto);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Alice Sena");
        assertThat(response.getEmail()).isEqualTo("alice.sena@hemovia.com");
        assertThat(response.getCpf()).isEqualTo("12345678900");
        assertThat(response.getPhoneNumber()).isEqualTo("81999998888");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hash_bcrypt");
    }

    @Test
    void shouldFailRegistrationWhenEmailAlreadyExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Alice Sena",
                "alice@hemovia.com",
                "12345678900",
                "81999998888",
                "senha123"
        );

        when(userRepository.existsByEmail("alice@hemovia.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("E-mail já cadastrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldFailRegistrationWhenCpfAlreadyExists() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Alice Sena",
                "alice@hemovia.com",
                "123.456.789-00",
                "81999998888",
                "senha123"
        );

        when(userRepository.existsByEmail("alice@hemovia.com")).thenReturn(false);
        when(userRepository.existsByCpf("12345678900")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfullyWithEmail() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Alice Sena", "alice@hemovia.com", "12345678900", "81999998888", "hash_bcrypt");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("alice@hemovia.com");
        dto.setPassword("senha123");

        when(userRepository.findByEmail("alice@hemovia.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hash_bcrypt")).thenReturn(true);
        when(jwtService.generateToken(userId)).thenReturn("mock.jwt.token");

        AuthResponseDTO response = authService.login(dto);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        verify(jwtService).generateToken(userId);
    }

    @Test
    void shouldLoginSuccessfullyWithCpf() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Alice Sena", "alice@hemovia.com", "12345678900", "81999998888", "hash_bcrypt");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setCpf("123.456.789-00");
        dto.setPassword("senha123");

        when(userRepository.findByCpf("12345678900")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hash_bcrypt")).thenReturn(true);
        when(jwtService.generateToken(userId)).thenReturn("mock.jwt.token");

        AuthResponseDTO response = authService.login(dto);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        verify(jwtService).generateToken(userId);
    }

    @Test
    void shouldFailLoginWhenUserNotFound() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("naoexiste@hemovia.com");
        dto.setPassword("senha123");

        when(userRepository.findByEmail("naoexiste@hemovia.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Credenciais inválidas");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void shouldFailLoginWhenPasswordDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Alice Sena", "alice@hemovia.com", "12345678900", "81999998888", "hash_bcrypt");

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("alice@hemovia.com");
        dto.setPassword("senhaErrada");

        when(userRepository.findByEmail("alice@hemovia.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaErrada", "hash_bcrypt")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Credenciais inválidas");

        verify(jwtService, never()).generateToken(any());
    }
}
