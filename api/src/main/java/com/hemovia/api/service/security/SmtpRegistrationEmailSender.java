package com.hemovia.api.service.security;

import com.hemovia.api.model.exception.RegistrationUnavailableException;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.service.port.output.RegistrationEmailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component
public class SmtpRegistrationEmailSender implements RegistrationEmailSender {
    private final ObjectProvider<JavaMailSender> mail;
    private final String host;
    private final String from;
    private final URI frontendUrl;

    public SmtpRegistrationEmailSender(ObjectProvider<JavaMailSender> mail,
                                       @Value("${spring.mail.host:}") String host,
                                       @Value("${hemovia.registration.sender:}") String from,
                                       @Value("${hemovia.registration.frontend-url:http://localhost:5173/}") URI frontendUrl) {
        this.mail = mail;
        this.host = host;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void requireAvailable() {
        if (host.isBlank() || from.isBlank() || frontendUrl.getHost() == null
                || !Set.of("http", "https").contains(frontendUrl.getScheme())
                || frontendUrl.getRawQuery() != null || frontendUrl.getRawFragment() != null
                || frontendUrl.getUserInfo() != null || mail.getIfAvailable() == null) {
            throw new RegistrationUnavailableException();
        }
    }

    @Override
    public void send(Email email, String token) {
        String link = frontendUrl + "#confirmar-cadastro=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email.value());
        message.setSubject("Confirme seu cadastro na Hemovia");
        message.setText("""
                Abra o link abaixo para confirmar seu e-mail e concluir seu cadastro na Hemovia.
                O link é válido por 30 minutos.

                %s

                Se você não solicitou este cadastro, ignore esta mensagem.
                """.formatted(link));
        try {
            mail.getObject().send(message);
        } catch (MailException exception) {
            throw new RegistrationUnavailableException();
        }
    }
}
