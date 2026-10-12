package com.hemovia.api.service;

import com.hemovia.api.model.DTOs.AuthResponseDTO;
import com.hemovia.api.model.DTOs.LoginRequestDTO;
import com.hemovia.api.model.DTOs.RegisterRequestDTO;
import com.hemovia.api.model.DTOs.UserResponseDTO;

public interface AuthService {

    UserResponseDTO register(RegisterRequestDTO registerRequestDTO);

    AuthResponseDTO login(LoginRequestDTO loginRequestDTO);
}
