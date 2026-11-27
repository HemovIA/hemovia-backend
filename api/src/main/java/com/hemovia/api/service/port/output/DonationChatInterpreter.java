package com.hemovia.api.service.port.output;

import com.hemovia.api.dto.request.DonationChatRequest.Message;

import java.util.List;

public interface DonationChatInterpreter {

    List<String> selectCards(List<Message> messages);
}

