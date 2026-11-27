# Tarefas do chat com IA

> feature: chat-com-ia

## T-062 — Criar a base oficial e a API de conversa [concluida]
- Refs: AC-132, AC-133, AC-135, AC-136, AC-137, AC-138, AC-139
- Arquivos: api/src/main/resources/donation-chat/catalog.json, api/src/main/resources/application.yaml, api/src/main/java/com/hemovia/api/controller/DonationChatController.java, api/src/main/java/com/hemovia/api/controller/config/DonationChatRequestConfiguration.java, api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java, api/src/main/java/com/hemovia/api/controller/handler/GlobalExceptionHandler.java, api/src/main/java/com/hemovia/api/dto/request/DonationChatRequest.java, api/src/main/java/com/hemovia/api/dto/response/DonationChatResponse.java, api/src/main/java/com/hemovia/api/service/config/DonationChatConfiguration.java, api/src/main/java/com/hemovia/api/service/config/DonationChatProperties.java, api/src/main/java/com/hemovia/api/service/chat/DonationChatCatalog.java, api/src/main/java/com/hemovia/api/service/chat/ModelDonationChatInterpreter.java, api/src/main/java/com/hemovia/api/service/port/output/DonationChatInterpreter.java, api/src/main/java/com/hemovia/api/service/usecase/chat/AnswerDonationQuestion.java, api/src/main/java/com/hemovia/api/model/exception/DonationChatUnavailableException.java, api/src/test/java/com/hemovia/api/controller/DonationChatIntegrationTest.java, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, api/src/test/java/com/hemovia/api/service/chat/DonationChatCatalogTest.java, api/src/test/java/com/hemovia/api/service/chat/ModelDonationChatInterpreterTest.java, scripts/spec-java/acceptance-cases.json, api/.env.example, README.md
- Modelo: gpt-6.2-sol
- Esforço: medio
- Notas: Usar o contrato descrito no design e testar a integração com um servidor de inferência falso. A tarefa pode rodar em paralelo com a interface.

## T-063 — Integrar o chat à Área do Doador [concluida]
- Refs: AC-132, AC-133, AC-134, AC-138
- Arquivos: ../hemovia-frontend/src/donor-account.tsx, ../hemovia-frontend/src/donor-chat.tsx, ../hemovia-frontend/src/http.ts, ../hemovia-frontend/src/styles.css, ../hemovia-frontend/tests/donor-chat.spec.ts
- Modelo: gpt-6.2-sol
- Esforço: medio
- Notas: Usar o contrato descrito no design e interceptar a API nos testes da interface.

## T-064 — Validar a integração e registrar as provas [concluida]
- Refs: AC-132, AC-133, AC-134, AC-135, AC-136, AC-137, AC-138, AC-139
- Arquivos: scripts/spec-java/verify-tests.mjs, scripts/spec-java/test-formularios.ps1, ../hemovia-frontend/tests/donor-chat-integrated.spec.ts, onpspec.config.json, .spec/verification/chat-com-ia.json
- Modelo: gpt-6.2-sol
- Esforço: medio
- Notas: Executar após as tarefas T-062 e T-063. Combinar as provas JUnit e Playwright, rodar build do frontend e verificar a amostra de tatuagem no fluxo conectado. Executar verify e audit --ci.
