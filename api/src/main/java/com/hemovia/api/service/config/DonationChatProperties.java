package com.hemovia.api.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties("hemovia.donation-chat")
public record DonationChatProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("http://127.0.0.1:11434") URI baseUrl,
        @DefaultValue("qwen2.5:7b") String model,
        @DefaultValue("3s") Duration connectTimeout,
        @DefaultValue("30s") Duration requestTimeout) {

    URI endpoint() {
        String scheme = baseUrl.getScheme();
        if ((!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                || baseUrl.getHost() == null || baseUrl.getRawUserInfo() != null || baseUrl.getRawQuery() != null
                || baseUrl.getRawFragment() != null || !(baseUrl.getPath().isEmpty() || "/".equals(baseUrl.getPath()))) {
            throw new IllegalArgumentException("Configure a origem HTTP ou HTTPS do Ollama para o chat de doação.");
        }
        return baseUrl.resolve("/api/chat");
    }
}

