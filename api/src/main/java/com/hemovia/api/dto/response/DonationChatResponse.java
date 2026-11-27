package com.hemovia.api.dto.response;

import java.util.List;

public record DonationChatResponse(String answer, List<Source> sources) {

    public record Source(String title, String url) { }
}

