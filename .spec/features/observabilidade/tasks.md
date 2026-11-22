# Tarefas de observabilidade

> feature: observabilidade

## T-055 — Instrumentar a API e preservar os contratos [concluida]
- Refs: AC-114, AC-115, AC-116, AC-120, AC-121
- Arquivos: api/pom.xml, api/src/main/resources/application.yaml, api/src/main/resources/application-observabilidade.yaml, api/Dockerfile, api/compose.yaml, render.yaml, .github/workflows/ci-cd.yml, .github/workflows/rollback.yml, api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java, api/src/main/java/com/hemovia/api/service/usecase/inventory/ManageDonations.java, api/src/main/java/com/hemovia/api/service/usecase/request/CreateHospitalBloodRequest.java, api/src/main/java/com/hemovia/api/service/observability/BusinessMetrics.java, api/src/test/java/com/hemovia/api/observability/ObservabilityIntegrationTest.java
- Notas: Isolar o Actuator, manter o probe em todos os chamadores, exportar traces e registrar métricas após o commit. Cobrir autenticação, commit e rollback.

## T-056 — Montar e provisionar a stack Docker [concluida]
- Refs: AC-115, AC-117, AC-118, AC-119, AC-120, AC-121
- Arquivos: docker-compose.yml, .env.example, prometheus/prometheus.yml, loki/loki.yml, loki/config.alloy, tempo/tempo.yml, grafana/provisioning/datasources/datasources.yml, grafana/provisioning/dashboards/dashboards.yml, grafana/dashboards/aplicacao.json, grafana/dashboards/infraestrutura.json
- Notas: Fixar o contrato de portas, nomes, formato dos logs e métricas no design para permitir trabalho paralelo com T-055. Conferir compatibilidade dos exporters no host.

## T-057 — Preparar carga e verificação ponta a ponta [concluida]
- Refs: AC-114, AC-115, AC-116, AC-117, AC-118, AC-119, AC-120, AC-121
- Arquivos: scripts/observabilidade/carga.mjs, tests/observabilidade/stack.test.mjs
- Notas: Usar Compose com dados sintéticos. Consultar os serviços e validar resultados reais. Integrar os testes ao TAP existente. Depende de T-055 e T-056.

## T-058 — Preparar acesso, evidências e relatório [concluida]
- Refs: AC-122, AC-123
- Arquivos: README.md, docs/observabilidade/relatorio.pdf, docs/observabilidade/evidencias, tests/observabilidade/entrega.test.mjs, scripts/spec-java/run-spec-tests.mjs, onpspec.config.json
- Notas: Depende de T-057. Validar o papel Viewer na demonstração local, capturar evidências com horário e verificar o PDF renderizado. O teste de acesso externo fica para a janela de correção, quando confirmada.
