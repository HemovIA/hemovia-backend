package com.hemovia.api.service.usecase.auth;

import com.hemovia.api.dto.request.ConfirmDonorRegistrationRequest;
import com.hemovia.api.model.User;
import com.hemovia.api.model.entity.Person;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.exception.DuplicateResourceException;
import com.hemovia.api.model.exception.InvalidDataException;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.repository.adapter.DonorRegistrationStore;
import com.hemovia.api.repository.jpa.PersonRepository;
import com.hemovia.api.repository.port.UserRepository;
import com.hemovia.api.service.port.output.ClockProvider;
import com.hemovia.api.service.port.output.PasswordHasher;
import com.hemovia.api.service.port.output.RegistrationEmailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RegisterDonor {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration LINK_LIFETIME = Duration.ofMinutes(30);
    private static final Duration SEND_INTERVAL = Duration.ofMinutes(1);
    private static final String CONFLICT_MESSAGE =
            "Não foi possível concluir o cadastro. Confira se você já tem uma conta ou atualize seus dados no hemocentro.";

    private final DonorRegistrationStore registrations;
    private final UserRepository users;
    private final PersonRepository people;
    private final PasswordHasher hasher;
    private final RegistrationEmailSender mail;
    private final ClockProvider clock;

    public RegisterDonor(DonorRegistrationStore registrations, UserRepository users, PersonRepository people,
                         PasswordHasher hasher, RegistrationEmailSender mail, ClockProvider clock) {
        this.registrations = registrations;
        this.users = users;
        this.people = people;
        this.hasher = hasher;
        this.mail = mail;
        this.clock = clock;
    }

    @Transactional
    public void start(String rawEmail) {
        Email email = new Email(rawEmail);
        mail.requireAvailable();
        if (users.existsByEmail(email)) {
            return;
        }
        Instant now = clock.now();
        registrations.deleteExpired(now);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        if (registrations.issue(email.value(), digest(token), now, now.plus(LINK_LIFETIME), now.minus(SEND_INTERVAL))) {
            mail.send(email, token);
        }
    }

    @Transactional(noRollbackFor = DuplicateResourceException.class)
    public void confirm(ConfirmDonorRegistrationRequest request) {
        Cpf cpf = new Cpf(request.cpf());
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidDataException("A senha deve ocupar até 72 bytes.");
        }
        String hash = digest(request.token());
        Email verifiedEmail = registrations.lockValidEmail(hash, clock.now()).map(Email::new)
                .orElseThrow(() -> new InvalidDataException("Link inválido, expirado ou já utilizado. Solicite um novo link."));
        registrations.consume(hash);
        UUID donorId = people.findByCpf(cpf.digits()).map(person -> matchingDonor(person, verifiedEmail)).orElse(null);
        User account = User.create(request.name(), verifiedEmail, cpf, null, hasher.hash(request.password()),
                Role.DOADOR, donorId);
        if (!registrations.insertDonor(account)) {
            throw new DuplicateResourceException(CONFLICT_MESSAGE);
        }
    }

    private UUID matchingDonor(Person donor, Email verifiedEmail) {
        if (!verifiedEmail.value().equalsIgnoreCase(donor.getEmail())) {
            throw new DuplicateResourceException(CONFLICT_MESSAGE);
        }
        return donor.getId();
    }

    private String digest(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 indisponível", impossible);
        }
    }
}
