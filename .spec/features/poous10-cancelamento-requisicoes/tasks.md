# Tarefas de cancelamento de requisições

> feature: poous10-cancelamento-requisicoes

## T-025 — Implementar cancelamento e liberação transacional [concluida]
- Refs: US-020, AC-041, AC-042, AC-043, AC-044, AC-045, AC-046
- Arquivos: api/src/main/java/com/hemovia/api/service/usecase/request/CancelBloodRequest.java, api/src/main/java/com/hemovia/api/repository/port/RequestCancellationStore.java, api/src/main/java/com/hemovia/api/repository/adapter/RequestCancellationStoreAdapter.java, api/src/main/java/com/hemovia/api/repository/jpa/BloodRequestRepository.java, api/src/main/java/com/hemovia/api/repository/jpa/AllocationRepository.java, api/src/main/java/com/hemovia/api/repository/jpa/AllocationItemRepository.java, api/src/main/java/com/hemovia/api/repository/jpa/BloodBagRepository.java, api/src/main/java/com/hemovia/api/repository/jpa/TransportRepository.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Contrato BloodRequestResponse execute(UUID requestId). Hospital da sessão, requisição bloqueada antes de validar estado, estados permitidos PENDING e ALLOCATED. Preservar histórico e coordenar bloqueios com AllocateBloodRequest. Relógio injetado em released_at; falhas propagam para rollback integral.

## T-026 — Expor o cancelamento autenticado e documentado no Swagger [concluida]
- Refs: US-020, AC-044, AC-047
- Arquivos: api/src/main/java/com/hemovia/api/controller/BloodRequestController.java, api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Usar o contrato da T-025, exigir HOSPITAL e CSRF, documentar 200/400/401/403/404/422 sem corpo. Atualizar o inventário de operações OpenAPI preservando os testes existentes. Arquivos disjuntos da T-025; a compilação integrada depende das duas.

## T-027 — Provar cancelamento, rollback e concorrência com PostgreSQL [concluida]
- Refs: AC-041, AC-042, AC-043, AC-044, AC-045, AC-046, AC-047
- Arquivos: api/src/test/java/com/hemovia/api/controller/BloodRequestCancellationAcceptanceTest.java, scripts/spec-java/acceptance-cases.json, onpspec.config.json, README.md
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Depois das T-025 e T-026. Testar liberação de três bolsas, pedido pendente, estados proibidos, acesso por hospital, CSRF, inexistentes, UUID inválido, rollback de commit e corridas entre cancelamento/alocação e cancelamentos duplicados. Comprovar reutilização das bolsas, registrar os critérios na auditoria e documentar o endpoint.
