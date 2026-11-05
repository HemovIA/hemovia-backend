package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.port.input.BootstrapAdminUseCase;
import com.hemovia.api.application.port.output.AdminBootstrapSettings;
import com.hemovia.api.application.port.output.PasswordHasher;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
Cria o primeiro ADMIN a partir das variáveis de ambiente, só se ainda não existir nenhum.
Sem as variáveis, ou com e-mail/CPF já usados por outra conta, apenas avisa no log e deixa a aplicação subir.
*/
@Service
@Transactional
public class BootstrapAdmin implements BootstrapAdminUseCase {

    // Aviso e confirmação da criação ficam no log da aplicação
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapAdmin.class);

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final AdminBootstrapSettings settings;

    public BootstrapAdmin(UserRepository users, PasswordHasher hasher, AdminBootstrapSettings settings) {
        this.users = users;
        this.hasher = hasher;
        this.settings = settings;
    }

    @Override
    public void execute() {
        // Já existe administrador: nada a fazer, e é por isso que reiniciar não duplica
        if (users.existsByRole(Role.ADMIN)) {
            return;
        }
        // Sem as variáveis ninguém conseguirá administrar o sistema, então avisamos
        if (!settings.isConfigured()) {
            LOG.warn("Não existe nenhum ADMIN e as variáveis ADMIN_NAME, ADMIN_EMAIL, ADMIN_CPF e ADMIN_PASSWORD não estão preenchidas.");
            return;
        }
        Email email = new Email(settings.email());
        Cpf cpf = new Cpf(settings.cpf());
        // E-mail ou CPF de outra conta: não sobrescrevemos conta alheia
        if (users.existsByEmail(email) || users.existsByCpf(cpf)) {
            LOG.warn("O e-mail ou CPF do ADMIN inicial já pertence a outra conta; o ADMIN não foi criado.");
            return;
        }
        users.save(User.create(settings.name(), email, cpf, null, hasher.hash(settings.password()), Role.ADMIN, null));
        LOG.info("Primeiro ADMIN criado: {}", email.value());
    }
}
