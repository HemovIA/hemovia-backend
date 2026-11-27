package com.hemovia.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DonationChatRequest(
        @NotNull @Size(min = 1, max = 9) List<@NotNull @Valid Message> messages) {

    // O Spring MVC pode formatar o DTO nos logs de depuração.
    @Override
    public String toString() {
        return "DonationChatRequest";
    }

    public record Message(
            @NotBlank @Pattern(regexp = "user|assistant") String role,
            @NotBlank @Size(max = 1000) String content) {

        @Override
        public String toString() {
            return "DonationChatMessage";
        }
    }
}

