package com.hemovia.api.controller;

import com.hemovia.api.dto.request.DonationChatRequest;
import com.hemovia.api.dto.response.DonationChatResponse;
import com.hemovia.api.model.exception.InvalidDataException;
import com.hemovia.api.service.usecase.chat.AnswerDonationQuestion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DonationChatController {

    public static final String MESSAGES_PATH = "/donation-chat/messages";
    private final AnswerDonationQuestion answerQuestion;

    public DonationChatController(AnswerDonationQuestion answerQuestion) {
        this.answerQuestion = answerQuestion;
    }

    @Operation(summary = "Responder a uma dúvida sobre doação de sangue total",
            security = @SecurityRequirement(name = "cookieAuth"))
    @ApiResponse(responseCode = "200", description = "Orientação em português com fontes oficiais",
            useReturnTypeSchema = true)
    @ApiResponse(responseCode = "400", description = "Histórico inválido")
    @ApiResponse(responseCode = "401", description = "Sessão obrigatória")
    @ApiResponse(responseCode = "403", description = "Papel de doador e token CSRF obrigatórios")
    @ApiResponse(responseCode = "503", description = "Assistente temporariamente indisponível")
    @PostMapping(MESSAGES_PATH)
    public ResponseEntity<DonationChatResponse> answer(@Valid @RequestBody DonationChatRequest request,
                                                       BindingResult validation) {
        if (validation.hasErrors()) {
            throw new InvalidDataException("O histórico de mensagens é inválido.");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(answerQuestion.answer(request.messages()));
    }
}

