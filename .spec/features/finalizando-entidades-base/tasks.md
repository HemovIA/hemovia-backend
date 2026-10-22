# Tasks: Finalizando entidades base

> feature: finalizando-entidades-base

Proposta: oito tarefas em sequência, sem implementação iniciada. Cada tarefa terá commit próprio e só será marcada concluída quando seus critérios tiverem prova PASS. A ordem considera dependências de tipos, contratos e esquema; arquivo disjunto não elimina dependência de compilação. Arquivos existentes citados como regressão não são atribuídos a esta feature como criação original.

## T-005 — Preparar provas Java/PostgreSQL e recuperar a validação histórica [pendente]

- Refs: US-008, AC-014
- Arquivos: onpspec.config.json, api/pom.xml, api/src/test/resources/application-base-entities-test.yaml, api/src/test/java/com/hemovia/api/support/BaseEntitiesPostgres.java, api/src/test/java/com/hemovia/api/support/BaseEntityFixtures.java, scripts/spec-java/test-postgres.ps1, api/src/test/java/com/hemovia/api/ApiApplicationTests.java, scripts/spec-java/verify-tests.mjs, scripts/spec-java/read-surefire.mjs, scripts/spec-java/acceptance-cases.json, tests/finalizando-entidades-base/reporter.test.mjs, tests/revisao-projeto/helpers/snapshot.mjs, tests/revisao-projeto/alinhamento.test.mjs, tests/revisao-projeto/api-dados-seguranca.test.mjs, tests/revisao-projeto/qualidade-operacao.test.mjs
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Primeiro pré-requisito. Preparar perfil isolado com PostgreSQL real e execução Maven completa; não reutilizar .env pessoal nem H2 como prova PostgreSQL. Traduzir resultados Surefire recentes por caso em TAP, falhando com skip, ausência de caso, XML antigo ou falha global. Testar o adaptador. Expandir globs para os testes Java e fontes exatas desta feature listadas nas tarefas, documentando o alcance. Corrigir os três testes documentais históricos via git show do commit analisado, preservando suas asserções. Não mudar o relatório histórico nem saltar testes existentes. Pode ficar com critérios pendentes até existirem todos os casos da feature.

## T-006 — Consolidar pessoas, locais, veículos e tipos comuns [pendente]

- Refs: US-004, AC-005, AC-006
- Arquivos: api/src/main/java/com/hemovia/api/model/Person.java, api/src/main/java/com/hemovia/api/model/Hospital.java, api/src/main/java/com/hemovia/api/model/BloodCenter.java, api/src/main/java/com/hemovia/api/model/Vehicle.java, api/src/main/java/com/hemovia/api/model/enums/BloodType.java, api/src/main/java/com/hemovia/api/model/enums/RhFactor.java, api/src/main/java/com/hemovia/api/model/enums/BloodComponent.java, api/src/main/java/com/hemovia/api/repository/PersonRepository.java, api/src/main/java/com/hemovia/api/repository/BloodCenterRepository.java, api/src/main/java/com/hemovia/api/repository/VehicleRepository.java, api/src/test/java/com/hemovia/api/model/BaseEntitiesUnitTest.java
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Depende de T-005 para a infraestrutura de prova. Não fundir Person/User. Implementar validação explícita de campos e coordenadas, código único do veículo e capacidade em bolsas. Substituir setters de Hospital que silenciam erro por rejeição verificável. Testar inclusive zero, limites, nulos e duplicidade no banco. User é verificado pelo teste de regressão sem alteração estrutural.

## T-007 — Evoluir doações e bolsas com compatibilidade dos contratos [pendente]

- Refs: US-005, US-009, AC-007, AC-008, AC-015
- Arquivos: api/src/main/java/com/hemovia/api/model/Donation.java, api/src/main/java/com/hemovia/api/model/BloodBag.java, api/src/main/java/com/hemovia/api/model/enums/BloodBagStatus.java, api/src/main/java/com/hemovia/api/model/DTOs/DonationRequestDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/DonationResponseDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/BloodBagRequestDTO.java, api/src/main/java/com/hemovia/api/model/DTOs/BloodBagResponseDTO.java, api/src/main/java/com/hemovia/api/service/implementation/DonationServiceImpl.java, api/src/main/java/com/hemovia/api/service/implementation/BloodBagServiceImpl.java, api/src/main/java/com/hemovia/api/repository/DonationRepository.java, api/src/main/java/com/hemovia/api/repository/BloodBagRepository.java, api/src/main/java/com/hemovia/api/exceptions/GlobalExceptionHandler.java, api/src/test/java/com/hemovia/api/model/BaseEntitiesUnitTest.java, api/src/test/java/com/hemovia/api/model/BaseEntitiesPersistenceIT.java, api/src/test/java/com/hemovia/api/service/implementation/BloodBagServiceImplTest.java
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Depende de T-006. Passar para Donation 1:N BloodBag e Person 1:N bolsas; sem cascata destrutiva. Acrescentar origem estruturada, enums, datas civis, volume opcional e versão. Resolver donorId em vez de descartá-lo e validar vínculos informados. Adaptar DTOs/serviços na mesma tarefa para manter compilação e criação atual de uma bolsa; bloodBag singular só representa exatamente uma bolsa, bloodBags representa a coleção completa. Testar UTC e America/Sao_Paulo. Migração e provas de esquema ficam integradas em T-011. Ajustar dados dos testes existentes preservando seus comportamentos testados.

## T-008 — Modelar requisições, alocações e histórico de reservas [pendente]

- Refs: US-006, AC-009, AC-010
- Arquivos: api/src/main/java/com/hemovia/api/model/BloodRequest.java, api/src/main/java/com/hemovia/api/model/Allocation.java, api/src/main/java/com/hemovia/api/model/AllocationItem.java, api/src/main/java/com/hemovia/api/model/enums/RequestStatus.java, api/src/main/java/com/hemovia/api/model/enums/AllocationStatus.java, api/src/main/java/com/hemovia/api/model/enums/ReservationStatus.java, api/src/main/java/com/hemovia/api/repository/BloodRequestRepository.java, api/src/main/java/com/hemovia/api/repository/AllocationRepository.java, api/src/main/java/com/hemovia/api/repository/AllocationItemRepository.java, api/src/test/java/com/hemovia/api/model/BaseEntitiesPersistenceIT.java
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Depende de T-006/T-007. Requisição nova tem PENDING, hospital e quantidade positiva em unidades de bolsa; não colidir com structures/basic/HospitalRequest. Associação ativa de bolsa deve ser exclusiva e histórico liberado preservado. Preparar versão de agregados, teste de atualização obsoleta e duas transações reais contra o índice parcial definido em T-011. Não implementar matching, FEFO ou endpoints operacionais novos.

## T-009 — Modelar rotas e seus pontos ordenados [pendente]

- Refs: US-007, AC-011
- Arquivos: api/src/main/java/com/hemovia/api/model/DeliveryRoute.java, api/src/main/java/com/hemovia/api/model/RouteWaypoint.java, api/src/main/java/com/hemovia/api/repository/DeliveryRouteRepository.java, api/src/test/java/com/hemovia/api/model/BaseEntitiesPersistenceIT.java
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Depende de T-006. Origem BloodCenter, destino Hospital, pontos ordenados persistidos, distância em km e duração em segundos. Validar campos/medidas e ordem única por rota; provar round-trip e rejeição de referências inválidas em T-011. Potencial candidata a faixa independente após concluir os tipos comuns; a proposta atual a mantém sequencial. Sem grafo ou Dijkstra.

## T-010 — Modelar transporte, telemetria e movimentos de estoque [pendente]

- Refs: US-007, AC-012
- Arquivos: api/src/main/java/com/hemovia/api/model/Transport.java, api/src/main/java/com/hemovia/api/model/TelemetryReading.java, api/src/main/java/com/hemovia/api/model/StockMovement.java, api/src/main/java/com/hemovia/api/model/enums/TransportStatus.java, api/src/main/java/com/hemovia/api/model/enums/MovementType.java, api/src/main/java/com/hemovia/api/repository/TransportRepository.java, api/src/main/java/com/hemovia/api/repository/TelemetryReadingRepository.java, api/src/main/java/com/hemovia/api/repository/StockMovementRepository.java, api/src/test/java/com/hemovia/api/model/BaseEntitiesPersistenceIT.java
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Depende de T-007/T-008/T-009. Vincular transporte a alocação/rota/veículo; obter bolsas pelos itens da alocação. Telemetria identifica sensor, instantes e temperatura em Celsius sem limites clínicos inventados. Movimentos mantêm histórico, bolsa e ator conhecido, sem exclusão em cascata. Validar cronologia e FKs. Não automatizar cancelamento, expedição, entrega ou ingestão.

## T-011 — Migrar e provar o esquema PostgreSQL [pendente]

- Refs: US-005, US-006, US-007, US-008, AC-005, AC-006, AC-007, AC-008, AC-009, AC-010, AC-011, AC-012, AC-013
- Arquivos: api/src/main/resources/db/migration/V4__normalize_base_entities.sql, api/src/main/resources/db/migration/V5__create_requests_allocations.sql, api/src/main/resources/db/migration/V6__create_logistics_telemetry.sql, api/src/main/resources/db/migration/V7__create_stock_movements.sql, api/src/test/java/com/hemovia/api/model/MigrationSourcesTest.java, api/src/test/java/com/hemovia/api/model/BaseEntitiesPersistenceIT.java, api/src/test/resources/fixtures/base-entities-migration-checksums.json
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Depende de todas as entidades. Preservar V1–V3/checksums e dados/IDs. Validar dados antes do backfill de donation_id, enums e constraints; rejeitar vínculo ambíguo e códigos desconhecidos sem apagar dados. Criar FKs, CHECKs, unicidades, índices de consulta e reserva ativa; testar schema-validation do Hibernate, base vazia, base legada, falhas com rollback e migrate repetido. Nenhum resultado de H2 substitui PostgreSQL. Os testes de persistência preparados nas tarefas anteriores só recebem prova após o esquema real funcionar.

## T-012 — Consolidar regressão, documentação e auditoria [pendente]

- Refs: US-008, US-009, AC-014, AC-015
- Arquivos: api/src/test/java/com/hemovia/api/controller/ExistingContractsRegressionTest.java, api/src/test/java/com/hemovia/api/controller/AuthControllerTest.java, api/src/test/java/com/hemovia/api/service/implementation/AuthServiceImplTest.java, api/src/test/java/com/hemovia/api/service/JwtServiceTest.java, api/src/test/java/com/hemovia/api/ApiApplicationTests.java, scripts/spec-java/acceptance-cases.json, onpspec.config.json, README.md, .spec/features/finalizando-entidades-base/modelo-entidades.md, .spec/features/finalizando-entidades-base/testes.md, .spec/features/finalizando-entidades-base/resultado-validacao.md
- Modelo: gpt-5.6-terra
- Esforço: medio
- Notas: Depende de T-005 a T-011. Provar contratos existentes, autenticação sem exposição de senha, coleção de bolsas e singular compatível, sem ciclos JSON. Preservar testes existentes e explicar adaptações legítimas de dados/contratos internos. Consolidar manifesto por critério e escopo de fontes auditadas; documentar entidades, campos, estados, cardinalidades, unidades, limites legados e comandos. Executar suíte completa com PostgreSQL isolado, verify de ambas as features e audit --ci; registrar evidências/impedimentos reais, sem declarar implementação concluída por teste documental. Só então marcar tarefas concluídas e spec auditada.

## Etapa de testes autorizada

Em 22/10/2026 o usuário pediu escrever os testes unitários e de integração dos critérios de aceite. Foram criados testes comportamentais em BaseEntitiesUnitTest, ExistingContractsRegressionTest, MigrationSourcesTest e BaseEntitiesPersistenceIT, com fixtures reflexivas que mantêm compilação sem implementar entidades. A falta de classe/método falha explicitamente, nunca é skip ou uma implementação simulada. Casos SQL legados estão no próprio teste, com dados sintéticos. Nenhuma tarefa da implementação completa foi marcada concluída; o estado RED e os comandos estão em testes.md e resultado-validacao.md.


