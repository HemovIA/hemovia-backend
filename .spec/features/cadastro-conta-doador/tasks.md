# Tarefas do cadastro público

> feature: cadastro-conta-doador

## T-059 — Persistir o papel e o vínculo de doador [em-andamento]
- Refs: AC-131
- Arquivos: api/src/main/resources/db/migration/V18__add_donor_user_link.sql, api/src/main/java/com/hemovia/api/model/enums/Role.java, api/src/main/java/com/hemovia/api/model/User.java, api/src/main/java/com/hemovia/api/model/entity/UserEntity.java, api/src/main/java/com/hemovia/api/repository/mapper/UserPersistenceMapper.java, api/src/main/java/com/hemovia/api/service/usecase/user/UserLinkValidator.java, api/src/main/java/com/hemovia/api/repository/adapter/BloodRequestQueryStoreAdapter.java, api/src/test/java/com/hemovia/api/repository/adapter/DonorUserPersistenceIT.java, scripts/spec-java/acceptance-cases.json
- Notas: Preservar o comportamento dos papéis operacionais. A prova integrada do cadastro passa pela confirmação da conta.

## T-060 — Criar o fluxo público de confirmação no backend [em-andamento]
- Refs: AC-124, AC-125, AC-126, AC-127, AC-128, AC-129
- Arquivos: api/src/main/resources/db/migration/V19__create_donor_registration_tokens.sql, api/src/main/java/com/hemovia/api/controller/AuthController.java, api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java, api/src/main/java/com/hemovia/api/controller/config/OpenApiConfiguration.java, api/src/main/java/com/hemovia/api/dto/request/StartDonorRegistrationRequest.java, api/src/main/java/com/hemovia/api/dto/request/ConfirmDonorRegistrationRequest.java, api/src/main/java/com/hemovia/api/service/usecase/auth/RegisterDonor.java, api/src/main/java/com/hemovia/api/repository/adapter/DonorRegistrationStore.java, api/src/main/java/com/hemovia/api/model/exception/RegistrationUnavailableException.java, api/src/main/java/com/hemovia/api/controller/handler/GlobalExceptionHandler.java, api/src/main/java/com/hemovia/api/service/port/output/RegistrationEmailSender.java, api/src/main/java/com/hemovia/api/service/security/SmtpRegistrationEmailSender.java, api/src/main/resources/application.yaml, api/pom.xml, docker-compose.yml, .env.example, api/.env.example, README.md, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, api/src/test/java/com/hemovia/api/controller/DonorRegistrationIntegrationTest.java, scripts/spec-java/acceptance-cases.json
- Notas: Depende da tarefa T-059. Cobrir expiração, repetição, concorrência, proteção das rotas e tentativas de vínculo com dados divergentes.

## T-061 — Integrar o formulário à tela de login [pendente]

- Refs: AC-124, AC-130
- Arquivos: ../hemovia-frontend/src/App.tsx, ../hemovia-frontend/src/domain.ts, ../hemovia-frontend/src/http.ts, ../hemovia-frontend/src/styles.css, ../hemovia-frontend/src/registration.tsx, ../hemovia-frontend/tests/registration.spec.ts, scripts/spec-java/verify-tests.mjs
- Notas: O formulário pode ser desenvolvido junto com a persistência usando o contrato de API do design. A validação integrada fica após a implementação da API.
