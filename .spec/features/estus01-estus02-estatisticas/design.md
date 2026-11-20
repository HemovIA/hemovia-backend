# Desenho das estatísticas de estoque e demanda

## Camadas e contratos congelados

- model/algorithm/statistics contém Frequency(long value, long count), DescriptiveStatistics e PercentageDistribution puros, sem Spring, banco ou DTO HTTP. Frequency aceita valor não negativo e frequência positiva. Coleções de entrada não são modificadas.
- DescriptiveStatistics.calculate(List<Frequency>) retorna Result(long count, long total, BigDecimal mean, BigDecimal median, BigDecimal populationVariance, BigDecimal populationStandardDeviation, BigDecimal sampleVariance, BigDecimal sampleStandardDeviation, boolean insufficientSample), record público aninhado na classe. Campos decimais usam duas casas HALF_UP. Vazio e n=1 têm dispersão zero e insufficientSample=true. Para vazio média/mediana/total também são zero. Usar BigDecimal DECIMAL128 nos intermediários, duas passagens ponderadas para dispersão e sqrt antes de arredondar. Mediana por posições e frequência, sem expandir registros; somas de count/total com detecção de overflow em vez de wraparound. Não somar totais por hospital como se fossem observações.
- PercentageDistribution.calculate(List<Long> counts) retorna List<BigDecimal> na mesma ordem, percentuais com duas casas. Total zero produz zeros. Para total positivo, calcular pontos-base inteiros por truncamento e distribuir os pontos restantes pelas maiores partes fracionárias, com desempate pela posição de entrada, para somar exatamente 100,00. Rejeitar negativos e não modificar a entrada.
- dto/internal/InventoryCount(BloodType bloodType, RhFactor rhFactor, BloodComponent component, long count) e DemandCount(UUID hospitalId, String hospitalName, int quantity, long count) são projeções agregadas da persistência.
- repository/port/StatisticsStore expõe List<InventoryCount> inventory(LocalDate referenceDate) e List<DemandCount> demand(Instant from, Instant to). Adapter em repository/adapter/StatisticsStoreAdapter implementa ambas. É uma porta de leitura agregada, não uma interface para cada serviço.
- service/usecase/statistics/CalculateInventorySummary.execute() retorna InventorySummaryResponse. CalculateDemandStatistics.execute(Instant from, Instant to) retorna DemandSummaryResponse. Ambos readOnly; somente o inventário precisa de ClockProvider. Não misturar entidades/persistência em controller ou DTOs e não criar Javadoc novo.

## Respostas HTTP

- InventorySummaryResponse(Instant generatedAt, LocalDate referenceDate, long totalBags, List<BloodTypeInventoryResponse> byBloodType, List<ComponentInventoryResponse> byComponent, List<InventoryCombinationResponse> byBloodTypeAndComponent).
- BloodTypeInventoryResponse(BloodType bloodType, RhFactor rhFactor, long count, BigDecimal percentage).
- ComponentInventoryResponse(BloodComponent component, long count, BigDecimal percentage).
- InventoryCombinationResponse(BloodType bloodType, RhFactor rhFactor, BloodComponent component, long count, BigDecimal percentage).
- DemandSummaryResponse(Instant from, Instant to, DemandMetricsResponse overall, List<HospitalDemandResponse> byHospital).
- DemandMetricsResponse(long requestCount, long totalBags, BigDecimal mean, BigDecimal median, BigDecimal populationVariance, BigDecimal populationStandardDeviation, BigDecimal sampleVariance, BigDecimal sampleStandardDeviation, boolean insufficientSample, String warning). Warning n<2: Amostra insuficiente para dispersão (n < 2). Caso contrário, null. O aviso é uma convenção explícita para a dispersão amostral; a populacional de n=1 também é zero.
- HospitalDemandResponse(UUID hospitalId, String hospitalName, DemandMetricsResponse metrics).
- Todos os DTOs são records; listas copiadas defensivamente. Categorias do estoque seguem a ordem dos enums (ABO, Rh, componente); hospitais seguem hospitalId. Contagens são inteiros de 64 bits, médias/desvios em bolsas, variâncias em bolsas ao quadrado e percentuais de 0 a 100. Percentuais usam rateio de arredondamento, portanto uma fração de 1/3 pode aparecer como 33,34 em uma categoria e 33,33 nas outras.
- StatisticsController: GET /statistics/inventory (200/401/403) e /statistics/demand (200/400/401/403). Segurança GET /api/v1/statistics/** somente ADMIN e HEMOCENTRO, leitura da rede confirmada. Sem CSRF em GET, sem escrita. OpenAPI application/json, cookieAuth, from/to opcionais ISO 8601 com fuso; validar UUIDs e datas conforme tipos já existentes. Inventário de operações passa de 39 para 41.

## Consultas e consistência

- Uma consulta agrupada por ABO/Rh/componente para o inventário, com status AVAILABLE, expirationDate > referenceDate UTC, bloodCenter.deletedAt null e NOT EXISTS item RESERVED/IN_TRANSIT para a bolsa. Reusar a mesma definição de elegibilidade da alocação, sem pessimistic locks, sem iterar entidades nem consultar uma vez por categoria. generatedAt/referenceDate vêm de uma única leitura do relógio.
- Uma consulta agrupada por hospital.id, hospital.name e quantity, contando requisições não CANCELLED. from <= createdAt < to; omitir a condição quando o respectivo limite for null. Usar parâmetros de consulta, sem interpolar dados do usuário nem depender de parâmetros null sem tipo reconhecível pelo PostgreSQL. Não buscar a entidade BloodRequest inteira ou reproduzir todas as requisições em memória.
- Construir os grupos gerais e por hospital a partir da mesma consulta, para não obter populações inconsistentes sob gravações simultâneas. Não incluir hospitais sem requisições selecionadas como observações zero. Preservar história de todos os estados não cancelados.
- Preencher categorias ausentes do estoque no serviço, após consultar contagens reais. Somar contagens sem overflow silencioso. Nenhuma migração é necessária; preservar V1–V16 e os inventários finais V16.

## Tarefas e execução

Usuário confirmou T-037 (cálculos puros) e T-038 (consultas agregadas) em paralelo com contratos acima fixos e arquivos disjuntos; T-039 integra serviços, API e permissões depois; T-040 prova os cenários em PostgreSQL e documenta/audita ao final. Todas as quatro tarefas usam GPT-6.1 Terra com esforço médio, confirmado pelo usuário. Usar agentes nativos no checkout atual; não executar o headless com branches padrão spec/, pois a branch real aprovada usa feat/estus01-estus02-estatisticas.

## Verificação

Testes puros de frequências ponderadas, mediana par, valores grandes, n=0/1 e arredondamento. Testes PostgreSQL de 200 bolsas, estoque vazio, todos os filtros de elegibilidade, repetição de hospital/quantidade, cancelamentos, bordas de período e autorização. Para contagens globais, usar schema exclusivo do contexto de testes (Flyway/JPA/Hikari com o mesmo schema), evitando truncar ou depender dos dados de outros testes. Provar ausência de mutações por snapshots de IDs/status/versões. Atualizar o inventário OpenAPI sem enfraquecer seus checks e registrar os critérios no manifesto. Só congelar fontes depois de revisar os cenários; então verify desta feature, renovação das dez provas anteriores e audit --ci sem avisos. Todos os testes devem rodar no PostgreSQL temporário, nunca no banco da aplicação.

## Implementação e validação funcional

Em 20/11/2026, a suíte completa executou 453 testes com zero falhas, erros ou skips. Foram acrescentados 17 casos: nove testes puros, sete testes HTTP com PostgreSQL e um teste do contrato OpenAPI. A execução confirma as 200 bolsas, categorias completas e percentuais reconciliados, todos os filtros de estoque (incluindo bolsa sem centro e reserva ativa com status inconsistente), observações individuais e frequências desiguais, população/amostra vazia e unitária, mediana par, limites inclusivo/exclusivo e parciais de período, equivalência de fusos, permissões e ausência de mutações.

Os testes HTTP usam schema stats_UUID isolado com configurações equivalentes em Flyway, Hibernate e Hikari. O PostgreSQL é temporário, preparado pelo script scripts/spec-java/test-postgres.ps1; nenhum banco da aplicação foi consultado ou modificado. Não houve nova migração: a versão final permanece V16. A execução usa JDK 26.0.2 e MAVEN_OPTS=-Daether.syncContext.named.factory=noop, com alvo Java 21 do projeto.

| Tarefa | Commit local | Resultado |
|---|---|---|
| T-037 | c1a45f1 | Estatísticas ponderadas e percentuais com maiores restos |
| T-038 | fae81da | Consultas agregadas de estoque e demanda |
| T-039 | 1fd121b | Serviços, DTOs, permissões e dois endpoints GET |
| T-040 | 8f84d35 | Aceitação PostgreSQL, contrato de 41 operações, rastreabilidade e guia |

Os artefatos gerados do plano registram a proposta anterior à execução. Os agentes nativos trabalharam em contextos separados, com os arquivos previstos e revisão no checkout compartilhado; o executor headless e suas branches padrão não foram usados. A única branch criada para esta entrega é feat/estus01-estus02-estatisticas, a partir de develop 2918638.

A prova desta feature e as dez provas anteriores foram geradas pelo verify, todas com exitCode 0. Os onze relatórios Maven completos foram conferidos: cada rodada terminou com 453 testes aprovados e nenhum skip. A auditoria final em modo CI saiu 0, com 74/74 critérios com teste e provados, nenhuma suposição/pergunta aberta e zero avisos. O ciclo de lições foi consultado; não houve falha nova de implementação a transformar em lição, apenas os sinais históricos de preparação e renovação de evidências. O relatório mecânico está em auditoria.md.
