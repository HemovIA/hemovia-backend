# Tarefas de transporte e entrega

> feature: poous09-poous11-transporte-entrega

## T-033 — Implementar início e conclusão transacionais [concluida]
- Refs: US-023, US-024, AC-057, AC-058, AC-059, AC-060, AC-061, AC-062, AC-063
- Arquivos: api/src/main/java/com/hemovia/api/service/usecase/transport/StartTransport.java, api/src/main/java/com/hemovia/api/service/usecase/transport/CompleteTransport.java, api/src/main/java/com/hemovia/api/service/usecase/transport/TransportLifecycle.java, api/src/main/java/com/hemovia/api/repository/port/TransportLifecycleStore.java, api/src/main/java/com/hemovia/api/repository/adapter/TransportLifecycleStoreAdapter.java, api/src/main/java/com/hemovia/api/repository/jpa/TransportRepository.java, api/src/main/java/com/hemovia/api/repository/jpa/AllocationRepository.java, api/src/main/java/com/hemovia/api/repository/jpa/VehicleRepository.java, api/src/main/java/com/hemovia/api/model/exception/VehicleUnavailableException.java, api/src/main/java/com/hemovia/api/dto/request/StartTransportRequest.java, api/src/main/java/com/hemovia/api/dto/response/TransportResponse.java, api/src/main/resources/db/migration/V16__one_active_transport_per_vehicle.sql
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Contratos de design.md; bloquear pedido antes de carregar entidades e depois veículo/bolsas; dono, lote, rota, janela, capacidade e estados coerentes. DTOs permitem compilação independente. V16 preserva migrações antigas. Não tocar frota/segurança/testes integrados nem rodar Maven simultaneamente.

## T-034 — Disponibilizar cadastro e listagem de veículos [concluida]
- Refs: US-025, AC-065
- Arquivos: api/src/main/java/com/hemovia/api/service/usecase/vehicle/ManageVehicles.java, api/src/main/java/com/hemovia/api/dto/request/VehicleRequest.java, api/src/main/java/com/hemovia/api/dto/response/VehicleResponse.java, api/src/main/java/com/hemovia/api/controller/VehicleController.java, api/src/test/java/com/hemovia/api/service/usecase/vehicle/ManageVehiclesTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Independente da T-033, usar métodos atuais de VehicleRepository sem editá-lo. Validação, código aparado/duplicado, listagem ordenada, Swagger JSON. Segurança vem na T-035; não rodar Maven simultaneamente.

## T-035 — Expor transporte com segurança e Swagger [concluida]
- Refs: US-023, US-024, US-025, AC-057, AC-061, AC-064, AC-065, AC-066
- Arquivos: api/src/main/java/com/hemovia/api/controller/TransportController.java, api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java, api/src/main/java/com/hemovia/api/controller/config/OpenApiConfiguration.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Depois de T-033/T-034. POST start HEMOCENTRO, complete HOSPITAL, POST vehicles ADMIN e GET com papéis de negócio. CSRF obrigatório, application/json e erros documentados; preservar autorização anterior.

## T-036 — Provar o ciclo e renovar a auditoria [concluida]
- Refs: AC-057, AC-058, AC-059, AC-060, AC-061, AC-062, AC-063, AC-064, AC-065, AC-066
- Arquivos: api/src/test/java/com/hemovia/api/controller/TransportAcceptanceTest.java, api/src/test/java/com/hemovia/api/controller/VehicleAcceptanceTest.java, api/src/test/java/com/hemovia/api/migration/MigracaoV16TransportesTest.java, api/src/test/java/com/hemovia/api/migration/MigracaoV15RotasReaisTest.java, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, api/src/test/java/com/hemovia/api/ApiApplicationTests.java, api/src/test/java/com/hemovia/api/migration/AtualizacaoDeBancoLegadoTest.java, api/src/test/java/com/hemovia/api/migration/MigracoesEmBancoVazioTest.java, api/src/test/java/com/hemovia/api/model/MigrationSourcesTest.java, scripts/spec-java/acceptance-cases.json, onpspec.config.json, README.md
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Depois da implementação. PostgreSQL isolado, sem mapas externos. Estados de todo o lote, limites, autorização, rollback no commit e concorrência determinística. Preservar checksums/testes; atualizar inventários finais V16 e 39 operações. Guia Swagger e telemetria futura explícita. Registrar critérios/fontes, suíte inteira, renovação das provas e audit --ci.
