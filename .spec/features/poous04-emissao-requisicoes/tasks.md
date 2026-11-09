# Tarefas — POOUS04

> feature: poous04-emissao-requisicoes

## T-016 — Implementar a emissão e verificar os cenários HTTP [concluida]
- Refs: US-014, AC-022, AC-023, AC-024
- Arquivos: api/src/main/java/com/hemovia/api/model/BloodRequest.java, api/src/main/java/com/hemovia/api/domain/enums/RequestPriority.java, api/src/main/java/com/hemovia/api/application/dto/request/CreateBloodRequest.java, api/src/main/java/com/hemovia/api/application/dto/response/BloodRequestResponse.java, api/src/main/java/com/hemovia/api/application/port/input/CreateBloodRequestUseCase.java, api/src/main/java/com/hemovia/api/application/port/output/BloodRequestStore.java, api/src/main/java/com/hemovia/api/application/usecase/request/CreateHospitalBloodRequest.java, api/src/main/java/com/hemovia/api/infrastructure/persistence/adapter/BloodRequestStoreAdapter.java, api/src/main/java/com/hemovia/api/infrastructure/security/SecurityConfig.java, api/src/main/java/com/hemovia/api/presentation/controller/BloodRequestController.java, api/src/main/resources/db/migration/V11__add_request_priority_and_deadline.sql, api/src/test/java/com/hemovia/api/presentation/controller/BloodRequestAcceptanceTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Reusar sessão e proteção CSRF, relógio injetado, validação HTTP e repositórios existentes. Mapear as colunas da V11 preservando os registros anteriores. Testar o fluxo pela cadeia de segurança real e conferir o registro no PostgreSQL pelo ID retornado. Anotar cada cenário com seu critério de aceite.

## T-017 — Verificar o contrato OpenAPI e ligar os testes à auditoria [concluida]
- Refs: US-014, AC-025
- Arquivos: api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, scripts/spec-java/acceptance-cases.json, onpspec.config.json
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Conferir o contrato no documento gerado e manter o inventário exato de operações. Incluir todos os critérios novos no adaptador de provas Maven para TAP e os arquivos da feature no cálculo da validade das provas. Os arquivos permitem escrever os testes em paralelo com a implementação. A execução integrada e a auditoria dependem da conclusão da T-016.
