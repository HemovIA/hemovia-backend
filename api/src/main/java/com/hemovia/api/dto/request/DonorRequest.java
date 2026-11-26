package com.hemovia.api.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record DonorRequest(@NotBlank @Size(max = 255) String name, @NotBlank @Size(max = 32) String cpf, @Email @Size(max = 255) String email, @Size(max = 255) String phoneNumber) { }

