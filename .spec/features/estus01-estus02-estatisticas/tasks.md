# Tarefas de estatísticas de estoque e demanda

> feature: estus01-estus02-estatisticas

## T-037 — Implementar cálculos estatísticos e percentuais [concluida]
- Refs: US-026, US-027, AC-068, AC-070, AC-071
- Arquivos: api/src/main/java/com/hemovia/api/model/algorithm/statistics/Frequency.java, api/src/main/java/com/hemovia/api/model/algorithm/statistics/DescriptiveStatistics.java, api/src/main/java/com/hemovia/api/model/algorithm/statistics/PercentageDistribution.java, api/src/test/java/com/hemovia/api/model/algorithm/statistics/DescriptiveStatisticsTest.java, api/src/test/java/com/hemovia/api/model/algorithm/statistics/PercentageDistributionTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Contratos fixos em design.md. BigDecimal, mediana ponderada sem expandir dados, variâncias populacional/amostral e n<2; percentuais por maiores restos com soma 100,00. Testar médias/medianas pares e ímpares, repetição, vazio, amostra única, estabilidade com valores grandes, overflow e imutabilidade. Independente de banco e da T-038.

## T-038 — Consultar estoque e demanda por agregação [concluida]
- Refs: US-026, US-027, AC-067, AC-069, AC-072
- Arquivos: api/src/main/java/com/hemovia/api/dto/internal/InventoryCount.java, api/src/main/java/com/hemovia/api/dto/internal/DemandCount.java, api/src/main/java/com/hemovia/api/repository/port/StatisticsStore.java, api/src/main/java/com/hemovia/api/repository/adapter/StatisticsStoreAdapter.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Independente da T-037; uma consulta de inventário e uma de demanda, sem entities HTTP, N+1 ou locks de escrita. Excluir bolsas não elegíveis/centros excluídos/reservas ativas; agrupar demanda por hospital e quantidade com frequência e filtro de createdAt opcional inclusivo/exclusivo. Parâmetros nomeados e nulos seguros. Não criar migração, tocar repositórios existentes nem executar Maven simultaneamente.

## T-039 — Expor indicadores com serviços, segurança e Swagger [concluida]
- Refs: US-026, US-027, AC-067, AC-068, AC-070, AC-071, AC-072, AC-073, AC-074
- Arquivos: api/src/main/java/com/hemovia/api/service/usecase/statistics/CalculateInventorySummary.java, api/src/main/java/com/hemovia/api/service/usecase/statistics/CalculateDemandStatistics.java, api/src/main/java/com/hemovia/api/controller/StatisticsController.java, api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java, api/src/main/java/com/hemovia/api/dto/response/InventorySummaryResponse.java, api/src/main/java/com/hemovia/api/dto/response/BloodTypeInventoryResponse.java, api/src/main/java/com/hemovia/api/dto/response/ComponentInventoryResponse.java, api/src/main/java/com/hemovia/api/dto/response/InventoryCombinationResponse.java, api/src/main/java/com/hemovia/api/dto/response/DemandSummaryResponse.java, api/src/main/java/com/hemovia/api/dto/response/DemandMetricsResponse.java, api/src/main/java/com/hemovia/api/dto/response/HospitalDemandResponse.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Depois de T-037/T-038. Casos de uso readOnly; inventário com todas as categorias e ClockProvider UTC; demanda valida from < to, agrega requisições individuais no geral e por hospital e mapeia aviso de n<2. Records com listas defensivas, decimais documentados. GETs ADMIN/HEMOCENTRO sem alterar segurança antiga; OpenAPI JSON e datas/unidades/erros reais, sem frontend ou indicadores futuros.

## T-040 — Provar estatísticas e atualizar guia e auditoria [concluida]
- Refs: AC-067, AC-068, AC-069, AC-070, AC-071, AC-072, AC-073, AC-074
- Arquivos: api/src/test/java/com/hemovia/api/controller/StatisticsAcceptanceTest.java, api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, scripts/spec-java/acceptance-cases.json, onpspec.config.json, README.md
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Depois da implementação. PostgreSQL com schema isolado para não interferir nos demais testes globais. Cobrir 200 bolsas, filtros, percentuais reconciliados, vazio, métricas globais/por hospital, amostra única, datas/bordas/limites parciais, cancelamentos, papéis e ausência de mutações. Inventário OpenAPI 39->41 com os dois GETs, schemas e segurança/erros/unidades. Manifesto por critério e fontes novas nos globs. Guia Swagger com dados sintéticos e interpretação populacional/amostral. Conferir sucesso Maven completo, renovar provas e audit --ci; não modificar migrações nem checks antigos para mascarar regressões.
