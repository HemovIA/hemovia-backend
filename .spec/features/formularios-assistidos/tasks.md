# Tasks: Formulários assistidos

> feature: formularios-assistidos

Execução manual sequencial e modelo autorizados.

## T-046 — Cadastro e consulta de doadores [em-andamento]
- Refs: US-032, AC-094, AC-095, AC-107
- Arquivos: api/src/main/java/com/hemovia/api/model/entity/Person.java, api/src/main/java/com/hemovia/api/repository/jpa/PersonRepository.java, api/src/main/resources/db/migration/V17__add_donor_cpf.sql, api/src/main/java/com/hemovia/api/controller/DonorController.java, api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java, api/src/main/java/com/hemovia/api/dto/request/DonorRequest.java, api/src/main/java/com/hemovia/api/dto/response/DonorResponse.java, api/src/main/java/com/hemovia/api/service/usecase/donor/ManageDonors.java, api/src/test/java/com/hemovia/api/controller/DonorAcceptanceTest.java, api/src/test/java/com/hemovia/api/model/DonorMigrationIT.java, api/src/test/java/com/hemovia/api/service/usecase/donor/ManageDonorsTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio

## T-047 — Seleção pesquisável e contratos frontend [pendente]
- Refs: US-033, AC-102, AC-103
- Arquivos: ../hemovia-frontend/src/ui.tsx, ../hemovia-frontend/src/domain.ts, ../hemovia-frontend/src/styles.css
- Modelo: gpt-6.1-terra
- Esforço: medio

## T-048 — Formulários de doação e bolsa [pendente]
- Refs: US-032, US-033, AC-096, AC-097, AC-098, AC-099, AC-100
- Arquivos: ../hemovia-frontend/src/donor.tsx, ../hemovia-frontend/src/pages.tsx, ../hemovia-frontend/src/demo.ts, ../hemovia-frontend/src/ui.tsx
- Modelo: gpt-6.1-terra
- Esforço: medio

## T-049 — Seletores operacionais [pendente]
- Refs: US-033, AC-101
- Arquivos: ../hemovia-frontend/src/pages.tsx, ../hemovia-frontend/src/registries.tsx, ../hemovia-frontend/src/ui.tsx
- Modelo: gpt-6.1-terra
- Esforço: medio

## T-050 — Leituras operacionais persistidas [pendente]
- Refs: US-034, AC-104, AC-105, AC-106
- Arquivos: ../hemovia-frontend/src/App.tsx, ../hemovia-frontend/src/pages.tsx, ../hemovia-frontend/src/registries.tsx, ../hemovia-frontend/src/queries.ts, ../hemovia-frontend/src/ui.tsx
- Modelo: gpt-6.1-terra
- Esforço: medio

## T-051 — Testes integrados e documentação [pendente]
- Refs: US-035, AC-107, AC-108
- Arquivos: ../hemovia-frontend/package.json, ../hemovia-frontend/package-lock.json, ../hemovia-frontend/README.md, ../hemovia-frontend/.gitignore, ../hemovia-frontend/playwright.config.ts, ../hemovia-frontend/vite.config.ts, ../hemovia-frontend/tests/donor.spec.ts, ../hemovia-frontend/tests/qa-isolado.spec.ts, ../hemovia-frontend/src/demo.ts, ../hemovia-frontend/src/domain.ts, ../hemovia-frontend/src/http.ts, ../hemovia-frontend/src/main.tsx, ../hemovia-frontend/src/vite-env.d.ts, onpspec.config.json, scripts/spec-java/verify-tests.mjs, scripts/spec-java/acceptance-cases.json, scripts/spec-java/test-formularios.ps1, api/src/test/java/com/hemovia/api/ApiApplicationTests.java, api/src/test/java/com/hemovia/api/migration/AtualizacaoDeBancoLegadoTest.java, api/src/test/java/com/hemovia/api/migration/MigracaoV16TransportesTest.java, api/src/test/java/com/hemovia/api/migration/MigracoesEmBancoVazioTest.java, api/src/test/java/com/hemovia/api/model/MigrationSourcesTest.java, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java
- Modelo: gpt-6.1-terra
- Esforço: medio

## T-052 — Verificação e auditoria [pendente]
- Refs: US-035, AC-107, AC-108
- Arquivos: .spec/features/formularios-assistidos/resultado-validacao.md, .spec/features/formularios-assistidos/andamento.md
- Modelo: gpt-6.1-terra
- Esforço: medio

