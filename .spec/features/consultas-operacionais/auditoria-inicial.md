# Relatório de auditoria

- Resultado: ❌ FAIL
- Features: 12 · Histórias de usuário: 31 · Critérios de aceite: 93
- Critérios com teste: 74/93 · Critérios provados: 74/93
- Suposições abertas: 1 · Perguntas abertas: 2

| Severidade | Problema | Feature | Detalhe | Local |
|---|---|---|---|---|
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-041 mapeia api/src/main/java/com/hemovia/api/dto/response/PageResponse.java, que não existe | .spec\features\consultas-operacionais\tasks.md:7 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-041 mapeia api/src/main/java/com/hemovia/api/dto/request/QueryPage.java, que não existe | .spec\features\consultas-operacionais\tasks.md:7 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-041 mapeia api/src/test/java/com/hemovia/api/service/usecase/query/QueryPageTest.java, que não existe | .spec\features\consultas-operacionais\tasks.md:7 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-042 mapeia api/src/main/java/com/hemovia/api/service/usecase/request/QueryBloodRequests.java, que não existe | .spec\features\consultas-operacionais\tasks.md:15 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-042 mapeia api/src/main/java/com/hemovia/api/repository/port/BloodRequestQueryStore.java, que não existe | .spec\features\consultas-operacionais\tasks.md:15 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-042 mapeia api/src/main/java/com/hemovia/api/repository/adapter/BloodRequestQueryStoreAdapter.java, que não existe | .spec\features\consultas-operacionais\tasks.md:15 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-042 mapeia api/src/main/java/com/hemovia/api/dto/response/BloodRequestDetailsResponse.java, que não existe | .spec\features\consultas-operacionais\tasks.md:15 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-042 mapeia api/src/main/java/com/hemovia/api/dto/response/BloodRequestSummaryResponse.java, que não existe | .spec\features\consultas-operacionais\tasks.md:15 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-042 mapeia api/src/test/java/com/hemovia/api/controller/BloodRequestQueryAcceptanceTest.java, que não existe | .spec\features\consultas-operacionais\tasks.md:15 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-043 mapeia api/src/main/java/com/hemovia/api/service/usecase/transport/QueryTransports.java, que não existe | .spec\features\consultas-operacionais\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-043 mapeia api/src/main/java/com/hemovia/api/repository/port/TransportQueryStore.java, que não existe | .spec\features\consultas-operacionais\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-043 mapeia api/src/main/java/com/hemovia/api/repository/adapter/TransportQueryStoreAdapter.java, que não existe | .spec\features\consultas-operacionais\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-043 mapeia api/src/main/java/com/hemovia/api/dto/response/TransportDetailsResponse.java, que não existe | .spec\features\consultas-operacionais\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-043 mapeia api/src/main/java/com/hemovia/api/dto/response/TransportSummaryResponse.java, que não existe | .spec\features\consultas-operacionais\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-043 mapeia api/src/test/java/com/hemovia/api/controller/TransportQueryAcceptanceTest.java, que não existe | .spec\features\consultas-operacionais\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-044 mapeia api/src/main/java/com/hemovia/api/service/usecase/hospital/QueryHospitals.java, que não existe | .spec\features\consultas-operacionais\tasks.md:31 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-044 mapeia api/src/test/java/com/hemovia/api/controller/HospitalQueryAcceptanceTest.java, que não existe | .spec\features\consultas-operacionais\tasks.md:31 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-045 mapeia api/src/test/java/com/hemovia/api/controller/OperationalQueriesContractTest.java, que não existe | .spec\features\consultas-operacionais\tasks.md:39 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | consultas-operacionais | a tarefa T-045 mapeia .spec/features/consultas-operacionais/resultado-validacao.md, que não existe | .spec\features\consultas-operacionais\tasks.md:39 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-075 (Listar pedidos) não tem nenhum teste anotado com @spec:AC-075 | .spec\features\consultas-operacionais\spec.md:16 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-076 (Detalhar pedido) não tem nenhum teste anotado com @spec:AC-076 | .spec\features\consultas-operacionais\spec.md:22 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-077 (Isolar pedidos por vínculo) não tem nenhum teste anotado com @spec:AC-077 | .spec\features\consultas-operacionais\spec.md:28 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-078 (Filtrar pedidos) não tem nenhum teste anotado com @spec:AC-078 | .spec\features\consultas-operacionais\spec.md:34 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-079 (Listar transportes) não tem nenhum teste anotado com @spec:AC-079 | .spec\features\consultas-operacionais\spec.md:44 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-080 (Detalhar transporte) não tem nenhum teste anotado com @spec:AC-080 | .spec\features\consultas-operacionais\spec.md:50 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-081 (Isolar transportes por vínculo) não tem nenhum teste anotado com @spec:AC-081 | .spec\features\consultas-operacionais\spec.md:56 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-082 (Filtrar transportes) não tem nenhum teste anotado com @spec:AC-082 | .spec\features\consultas-operacionais\spec.md:62 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-083 (Consultar hospitais) não tem nenhum teste anotado com @spec:AC-083 | .spec\features\consultas-operacionais\spec.md:72 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-084 (Isolar hospitais por vínculo) não tem nenhum teste anotado com @spec:AC-084 | .spec\features\consultas-operacionais\spec.md:78 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-085 (Negar cadastro ao transportador) não tem nenhum teste anotado com @spec:AC-085 | .spec\features\consultas-operacionais\spec.md:84 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-086 (Buscar hospitais) não tem nenhum teste anotado com @spec:AC-086 | .spec\features\consultas-operacionais\spec.md:90 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-087 (Exigir sessão) não tem nenhum teste anotado com @spec:AC-087 | .spec\features\consultas-operacionais\spec.md:100 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-088 (Ocultar recursos alheios) não tem nenhum teste anotado com @spec:AC-088 | .spec\features\consultas-operacionais\spec.md:106 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-089 (Validar parâmetros) não tem nenhum teste anotado com @spec:AC-089 | .spec\features\consultas-operacionais\spec.md:112 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-090 (Paginar e ordenar) não tem nenhum teste anotado com @spec:AC-090 | .spec\features\consultas-operacionais\spec.md:118 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-091 (Consultar resultado vazio) não tem nenhum teste anotado com @spec:AC-091 | .spec\features\consultas-operacionais\spec.md:124 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-092 (Documentar consultas) não tem nenhum teste anotado com @spec:AC-092 | .spec\features\consultas-operacionais\spec.md:130 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | consultas-operacionais | AC-093 (Preservar estados) não tem nenhum teste anotado com @spec:AC-093 | .spec\features\consultas-operacionais\spec.md:136 |
