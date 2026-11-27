package com.hemovia.api.service.config;

import com.hemovia.api.model.exception.DonationChatUnavailableException;
import com.hemovia.api.service.chat.DonationChatCatalog;
import com.hemovia.api.service.chat.ModelDonationChatInterpreter;
import com.hemovia.api.service.port.output.DonationChatInterpreter;
import com.hemovia.api.service.usecase.chat.AnswerDonationQuestion;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DonationChatProperties.class)
public class DonationChatConfiguration {

    @Bean
    DonationChatCatalog donationChatCatalog() throws IOException {
        try (InputStream catalog = new ClassPathResource("donation-chat/catalog.json").getInputStream()) {
            return new DonationChatCatalog(catalog);
        }
    }

    @Bean
    DonationChatInterpreter donationChatInterpreter(DonationChatProperties properties, DonationChatCatalog catalog) {
        if (!properties.enabled()) {
            return messages -> {
                throw new DonationChatUnavailableException();
            };
        }
        return new ModelDonationChatInterpreter(catalog, properties.endpoint(), properties.model(),
                properties.connectTimeout(), properties.requestTimeout());
    }

    @Bean
    AnswerDonationQuestion answerDonationQuestion(DonationChatCatalog catalog, DonationChatInterpreter interpreter) {
        return new AnswerDonationQuestion(catalog, interpreter);
    }
}

