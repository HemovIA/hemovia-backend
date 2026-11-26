package com.hemovia.api.service.port.output;

import com.hemovia.api.model.valueobject.Email;

public interface RegistrationEmailSender {
    void requireAvailable();

    void send(Email email, String token);
}
