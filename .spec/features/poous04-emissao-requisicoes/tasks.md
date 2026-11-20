# Tarefas — POOUS04

> feature: poous04-emissao-requisicoes

## T-016 — Implementar a emissão e verificar os cenários HTTP [concluida]
- Refs: US-014, AC-022, AC-023, AC-024
- Arquivos: api/src/main/java/com/hemovia/api/model/entity/BloodRequest.java, api/src/main/java/com/hemovia/api/model/enums/RequestPriority.java, api/src/main/java/com/hemovia/api/dto/request/CreateBloodRequest.java, api/src/main/java/com/hemovia/api/dto/response/BloodRequestResponse.java, api/src/main/java/com/hemovia/api/service/usecase/request/CreateHospitalBloodRequest.java, api/src/main/java/com/hemovia/api/repository/port/BloodRequestStore.java, api/src/main/java/com/hemovia/api/repository/adapter/BloodRequestStoreAdapter.java, api/src/main/java/com/hemovia/api/dto/internal/NewBloodRequest.java, api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java, api/src/main/java/com/hemovia/api/controller/BloodRequestController.java, api/src/main/resources/db/migration/V11__add_request_priority_and_deadline.sql, api/src/test/java/com/hemovia/api/controller/BloodRequestAcceptanceTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Reusar sessão e proteção CSRF, relógio injetado, validação HTTP e repositórios existentes. Mapear as colunas da V11 preservando os registros anteriores. Testar o fluxo pela cadeia de segurança real e conferir o registro no PostgreSQL pelo ID retornado. Anotar cada cenário com seu critério de aceite.

## T-017 — Verificar o contrato OpenAPI e ligar os testes à auditoria [concluida]
- Refs: US-014, AC-025
- Arquivos: api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, scripts/spec-java/acceptance-cases.json, onpspec.config.json
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Conferir o contrato no documento gerado e manter o inventário exato de operações. Incluir todos os critérios novos no adaptador de provas Maven para TAP e os arquivos da feature no cálculo da validade das provas. Os arquivos permitem escrever os testes em paralelo com a implementação. A execução integrada e a auditoria dependem da conclusão da T-016.
