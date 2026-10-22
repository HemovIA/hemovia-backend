# Tasks: Documentação da API com Swagger

> feature: documentacao-swagger

Três tarefas propostas, sem implementação iniciada. Recomenda-se execução sequencial devido às dependências e aos arquivos compartilhados. Um commit por tarefa, com correções posteriores rastreadas; nenhuma tarefa concluída sem os critérios associados terem prova PASS. Arquivos existentes citados são evolução/regressão, não criação atribuída a esta feature.

## T-013 — Integrar Swagger UI, OpenAPI e configuração de acesso [pendente]

- Refs: US-010, AC-016, AC-017
- Arquivos: api/pom.xml, api/src/main/java/com/hemovia/api/config/OpenApiConfig.java, api/src/main/java/com/hemovia/api/config/SecurityConfig.java, api/src/main/resources/application.yaml, api/.env.example, api/src/test/java/com/hemovia/api/documentation/OpenApiAvailabilityIT.java, api/src/test/java/com/hemovia/api/documentation/OpenApiDisabledIT.java, api/src/test/resources/application-base-entities-test.yaml
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Fixar starter springdoc WebMVC 3.1.1 e confirmar compatibilidade com Boot 4.1.1. Metadados da API, URLs padrão, Swagger sem Petstore e opção única de habilitar/desabilitar UI e documento. Preservar regras atuais de autorização e tornar acesso documental explícito. Testes HTTP com geração real e contexto separado para desabilitação, sem usar banco pessoal. Novo download de dependências exige acesso de rede/cache disponível; registrar bloqueio real sem aprovação fictícia.

## T-014 — Documentar contratos, modelos e autenticação JWT [pendente]

- Refs: US-011, US-012, AC-018, AC-019, AC-020
- Arquivos: api/src/main/java/com/hemovia/api/config/OpenApiConfig.java, api/src/main/java/com/hemovia/api/controller/AuthController.java, api/src/main/java/com/hemovia/api/controller/HospitalController.java, api/src/main/java/com/hemovia/api/controller/DonationController.java, api/src/main/java/com/hemovia/api/controller/BloodBagController.java, api/src/main/java/com/hemovia/api/model/DTOs/AuthResponseDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/UserResponseDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/LoginRequestDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/RegisterRequestDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/HospitalRequestDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/HospitalResponseDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/DonationRequestDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/DonationResponseDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/BloodBagRequestDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/BloodBagResponseDTO.java, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Depende de T-013. Quatorze operações, tags por domínio, resumos em português, operationIds únicos, parâmetros, corpos e respostas reais. Documentar códigos HTTP sem transformar handlers ou requisitos de negócio. Declarar somente /auth/me com Bearer, esconder principal interno, marcar senhas como entrada. Schemas e exemplos sintéticos refletem JSON, campos obrigatórios/opcionais, datas e relação singular/coleção. Exemplos de criação devem passar pelas validações atuais, considerando validade futura. Validar documento gerado em HTTP e preservar regressões.

## T-015 — Consolidar provas, instruções e documento de auditoria [pendente]

- Refs: US-010, US-011, US-012, US-013, AC-016, AC-017, AC-018, AC-019, AC-020, AC-021
- Arquivos: api/src/test/java/com/hemovia/api/documentation/OpenApiAvailabilityIT.java, api/src/test/java/com/hemovia/api/documentation/OpenApiDisabledIT.java, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, scripts/spec-java/acceptance-cases.json, scripts/spec-java/verify-tests.mjs, scripts/spec-java/test-postgres.ps1, onpspec.config.json, README.md, .spec/features/documentacao-swagger/testes.md, .spec/features/documentacao-swagger/resultado-validacao.md, .spec/features/documentacao-swagger/auditoria.md
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Depende de T-013/T-014. Manifesto por caso real para os seis critérios, sem remover provas anteriores. Incluir explicitamente todas as fontes desta feature no escopo, sem declarar todo legado como criação nova. Executar suíte completa com PostgreSQL isolado e verify das três features; documentar URLs, configuração, exemplos e login/Authorize. Salvar audit --ci --md no documento de auditoria solicitado para esta feature, registrar saída e limitações, concluir tarefas somente com PASS. Cada operação/documento deve ser validado por comportamento, não só pela existência de anotações. Se possível, smoke test visual da UI após testes HTTP, sem substituir provas automatizadas.
