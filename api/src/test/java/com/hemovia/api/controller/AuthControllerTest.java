package com.hemovia.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemovia.api.presentation.handler.GlobalExceptionHandler;
import com.hemovia.api.infrastructure.configuration.SystemClockProvider;
import java.time.Clock;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.exception.DuplicateResourceException;
import com.hemovia.api.model.DTOs.AuthResponseDTO;
import com.hemovia.api.model.DTOs.LoginRequestDTO;
import com.hemovia.api.model.DTOs.RegisterRequestDTO;
import com.hemovia.api.model.DTOs.UserResponseDTO;
import com.hemovia.api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler(new SystemClockProvider(Clock.systemUTC())))
                .build();
    }

    @Test
    void shouldReturn201OnSuccessfulRegistration() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Alice Sena",
                "alice@hemovia.com",
                "12345678900",
                "81999998888",
                "senha123"
        );

        UUID userId = UUID.randomUUID();
        UserResponseDTO response = new UserResponseDTO(
                userId,
                "Alice Sena",
                "alice@hemovia.com",
                "12345678900",
                "81999998888"
        );

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Alice Sena"))
                .andExpect(jsonPath("$.email").value("alice@hemovia.com"))
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn400WhenRegistrationFieldsAreInvalid() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "",
                "email-invalido",
                "",
                "81999998888",
                "123"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void deveRetornar409QuandoUsuarioJaExiste() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Alice Sena",
                "alice@hemovia.com",
                "12345678900",
                "81999998888",
                "senha123"
        );

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("E-mail já cadastrado"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void shouldReturn200AndTokenOnSuccessfulLogin() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("alice@hemovia.com");
        request.setPassword("senha123");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(new AuthResponseDTO("jwt.token.secret"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.secret"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn401OnInvalidLoginCredentials() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("alice@hemovia.com");
        request.setPassword("senhaIncorreta");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new InvalidCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }
}
