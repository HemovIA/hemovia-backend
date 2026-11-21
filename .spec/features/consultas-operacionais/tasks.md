# Tasks: Consultas operacionais

> feature: consultas-operacionais

Plano sequencial e custos confirmados; execução manual concluída. As cinco tarefas têm implementação e prova PASS. A execução utilizou o checkout atual, preservando os arquivos anteriores do usuário.

## T-041 — Contratos de paginação [concluida]
- Refs: US-031, AC-089, AC-090
- Arquivos: api/src/main/java/com/hemovia/api/dto/response/PageResponse.java, api/src/main/java/com/hemovia/api/dto/request/QueryPage.java, api/src/test/java/com/hemovia/api/service/usecase/query/QueryPageTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Base comum das próximas três tarefas; testar limites, padrões e metadados.

## T-042 — Consultas de requisições [concluida]
- Refs: US-028, AC-075, AC-076, AC-077, AC-078
- Arquivos: api/src/main/java/com/hemovia/api/controller/BloodRequestController.java, api/src/main/java/com/hemovia/api/service/usecase/request/QueryBloodRequests.java, api/src/main/java/com/hemovia/api/repository/port/BloodRequestQueryStore.java, api/src/main/java/com/hemovia/api/repository/adapter/BloodRequestQueryStoreAdapter.java, api/src/main/java/com/hemovia/api/repository/jpa/BloodRequestRepository.java, api/src/main/java/com/hemovia/api/dto/response/BloodRequestDetailsResponse.java, api/src/main/java/com/hemovia/api/dto/response/BloodRequestSummaryResponse.java, api/src/main/java/com/hemovia/api/dto/internal/RequestQuery.java, api/src/test/java/com/hemovia/api/controller/BloodRequestQueryAcceptanceTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Depende de T-041. Aplicar filtros e isolamento antes de paginar e contar. Recuperar histórico inclusive alocações canceladas.

## T-043 — Consultas de transportes [concluida]
- Refs: US-029, AC-079, AC-080, AC-081, AC-082
- Arquivos: api/src/main/java/com/hemovia/api/controller/TransportController.java, api/src/main/java/com/hemovia/api/service/usecase/transport/QueryTransports.java, api/src/main/java/com/hemovia/api/repository/port/TransportQueryStore.java, api/src/main/java/com/hemovia/api/repository/adapter/TransportQueryStoreAdapter.java, api/src/main/java/com/hemovia/api/repository/jpa/TransportRepository.java, api/src/main/java/com/hemovia/api/dto/response/TransportDetailsResponse.java, api/src/main/java/com/hemovia/api/dto/response/TransportSummaryResponse.java, api/src/main/java/com/hemovia/api/dto/internal/TransportQuery.java, api/src/test/java/com/hemovia/api/controller/TransportQueryAcceptanceTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Depende de T-041. Recuperar rota persistida e bolsas; garantir isolamento na lista, detalhe e totais.

## T-044 — Consultas de hospitais [concluida]
- Refs: US-030, AC-083, AC-084, AC-085, AC-086
- Arquivos: api/src/main/java/com/hemovia/api/controller/HospitalController.java, api/src/main/java/com/hemovia/api/service/usecase/hospital/QueryHospitals.java, api/src/main/java/com/hemovia/api/repository/port/HospitalQueryStore.java, api/src/main/java/com/hemovia/api/repository/adapter/HospitalQueryStoreAdapter.java, api/src/main/java/com/hemovia/api/repository/jpa/HospitalRepository.java, api/src/test/java/com/hemovia/api/controller/HospitalQueryAcceptanceTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Depende de T-041. Reutilizar HospitalResponse e testar busca por trecho e permissões.

## T-045 — Segurança, documentação e provas [concluida]
- Refs: US-031, AC-087, AC-088, AC-089, AC-090, AC-091, AC-092, AC-093
- Arquivos: api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java, api/src/main/java/com/hemovia/api/controller/BloodRequestController.java, api/src/main/java/com/hemovia/api/controller/TransportController.java, api/src/main/java/com/hemovia/api/controller/HospitalController.java, api/src/test/java/com/hemovia/api/controller/OperationalQueriesContractTest.java, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, scripts/spec-java/acceptance-cases.json, onpspec.config.json, README.md, .spec/features/consultas-operacionais/resultado-validacao.md
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Depende de T-042, T-043 e T-044. Preservar provas anteriores, mapear testes reais dos 19 critérios, verificar regressões e executar verify e audit --ci. Um commit por tarefa; concluir apenas com PASS.
