package com.hemovia.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartDonorRegistrationRequest(
        @NotBlank(message = "Informe o e-mail.") @Size(max = 255) String email) { }
