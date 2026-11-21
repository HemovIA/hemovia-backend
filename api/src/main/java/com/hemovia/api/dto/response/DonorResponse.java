package com.hemovia.api.dto.response;
import java.util.UUID;
public record DonorResponse(UUID id, String name, String cpf, String email, String phoneNumber) { }
