package com.hemovia.api.service.usecase.chat;

import com.hemovia.api.dto.request.DonationChatRequest.Message;
import com.hemovia.api.dto.response.DonationChatResponse;
import com.hemovia.api.model.exception.InvalidDataException;
import com.hemovia.api.service.chat.DonationChatCatalog;
import com.hemovia.api.service.port.output.DonationChatInterpreter;

import java.util.List;

public class AnswerDonationQuestion {

    private final DonationChatCatalog catalog;
    private final DonationChatInterpreter interpreter;

    public AnswerDonationQuestion(DonationChatCatalog catalog, DonationChatInterpreter interpreter) {
        this.catalog = catalog;
        this.interpreter = interpreter;
    }

    public DonationChatResponse answer(List<Message> messages) {
        if (!"user".equals(messages.getLast().role())) {
            throw new InvalidDataException("A última mensagem deve ser uma pergunta do usuário.");
        }

        List<Message> context = messages.stream()
                .map(message -> new Message(message.role(), message.content().strip()))
                .toList();
        return catalog.respond(interpreter.selectCards(context));
    }
}

