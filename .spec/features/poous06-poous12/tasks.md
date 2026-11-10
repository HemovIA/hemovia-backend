# Tarefas de alocação e reserva concorrente

> feature: poous06-poous12

## T-021 — Implementar matching, FEFO e reserva transacional [concluida]
- Refs: US-017, US-018, AC-030, AC-031, AC-033, AC-034, AC-035
- Arquivos: api/src/main/java/com/hemovia/api/application/dto/response/AllocationResponse.java, api/src/main/java/com/hemovia/api/application/port/input/AllocateBloodRequestUseCase.java, api/src/main/java/com/hemovia/api/application/port/output/RequestAllocationStore.java, api/src/main/java/com/hemovia/api/application/usecase/request/AllocateBloodRequest.java, api/src/main/java/com/hemovia/api/domain/exception/InsufficientInventoryException.java, api/src/main/java/com/hemovia/api/domain/exception/RequestAlreadyProcessedException.java, api/src/main/java/com/hemovia/api/infrastructure/persistence/adapter/RequestAllocationStoreAdapter.java, api/src/main/java/com/hemovia/api/repository/BloodRequestRepository.java, api/src/main/java/com/hemovia/api/repository/BloodBagRepository.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Fixar o contrato `AllocationResponse execute(UUID requestId)` para a tarefa HTTP. Buscar e bloquear a requisição antes de confirmar PENDING. Buscar e bloquear as bolsas do hemocentro com status AVAILABLE, componente e validade elegíveis, em ordem estável, excluindo reservas ativas legadas. Consumir a fila FEFO só quando houver quantidade suficiente. Fazer as alterações na mesma transação e deixar falhas inesperadas provocarem rollback.

## T-022 — Expor a alocação autenticada no Swagger [concluida]
- Refs: US-017, AC-030, AC-031, AC-032, AC-033
- Arquivos: api/src/main/java/com/hemovia/api/presentation/controller/BloodRequestController.java, api/src/main/java/com/hemovia/api/infrastructure/security/SecurityConfig.java, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Expor `POST /requests/{id}/allocate` para o papel HEMOCENTRO usando o contrato da T-021. Documentar 201, 400, 401, 403, 404, 409 e 422 com o formato de erro existente. Preservar a emissão da POOUS04. Os arquivos são distintos dos da T-021 e a compilação integrada exige ambas.

## T-023 — Provar matching, concorrência e rollback com PostgreSQL [concluida]
- Refs: AC-030, AC-031, AC-032, AC-033, AC-034, AC-035
- Arquivos: api/src/test/java/com/hemovia/api/presentation/controller/BloodRequestAllocationAcceptanceTest.java, scripts/spec-java/acceptance-cases.json, onpspec.config.json
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Após T-021 e T-022, rodar os cenários da API com transações reais e conferir a persistência por SQL. Usar requisições concorrentes para a última bolsa e uma falha no banco após as escritas para provar rollback. Registrar os métodos JUnit no manifesto e atualizar os globs e provas antes do audit.
