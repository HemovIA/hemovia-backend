# Spec: Transporte e entrega — POOUS09 e POOUS11

> feature: poous09-poous11-transporte-entrega
> status: auditada

## Contexto

Fechar o ciclo entre a alocação com rota e o recebimento no hospital. Fontes: https://github.com/HemovIA/hemovia-backend/issues/11 e https://github.com/HemovIA/hemovia-backend/issues/13. Base develop; branch feat/poous09-poous11-transporte-entrega.

## Histórias

### US-023 — Iniciar transporte (POOUS09)
Como despachante do hemocentro, quero iniciar o transporte da alocação do meu centro com um veículo disponível, para acompanhar a saída das bolsas pela rota reservada.

#### AC-057 — Saída com rota e bolsas vinculadas
- **Dado** uma requisição ALLOCATED, alocação do centro autenticado com rota válida, bolsas e itens RESERVED e veículo suficiente
- **Quando** envia allocationId e vehicleId em POST /api/v1/transports/start
- **Então** recebe 201 com id, allocationId, requestId, routeId, vehicleId, status IN_TRANSIT, startedAt e completedAt nulo; pedido, alocação, itens e bolsas passam juntos para IN_TRANSIT, preservando a rota.

#### AC-058 — Saída inválida mantém a reserva
- **Dado** pedido PENDING, CANCELLED, IN_TRANSIT ou DELIVERED, alocação sem rota, reservas inconsistentes ou janela de chegada vencida
- **Quando** tenta iniciar transporte
- **Então** recebe 422 sem criar transporte nem alterar estados ou rota; alocação ou veículo inexistente retorna 404.

#### AC-059 — Capacidade e disponibilidade do veículo
- **Dado** veículo com capacidade inferior ao lote ou com transporte IN_TRANSIT
- **Quando** tenta iniciar viagem nele
- **Então** capacidade insuficiente retorna 422 e veículo ocupado retorna 409 VEHICLE_UNAVAILABLE, sem alteração do estoque ou pedido.

#### AC-060 — Concorrência entre saídas e cancelamento
- **Dado** tentativas simultâneas na mesma alocação, em alocações distintas no mesmo veículo, ou início e cancelamento do mesmo pedido
- **Quando** as transações disputam os recursos
- **Então** somente uma saída por alocação e uma viagem ativa por veículo podem confirmar; cancelamento e início se excluem, sem estados parciais ou liberação de bolsas em trânsito.

### US-024 — Confirmar recebimento (POOUS11)
Como recebedor do hospital de destino, quero confirmar a entrega e encerrar o transporte, para efetivar a baixa final.

#### AC-061 — Entrega encerra todo o lote
- **Dado** transporte, pedido, alocação, itens e bolsas IN_TRANSIT
- **Quando** o hospital de destino envia POST /api/v1/transports/{id}/complete
- **Então** recebe 200 com status DELIVERED e completedAt do relógio; todos os recursos passam juntos para DELIVERED, preservando rota e startedAt, e o veículo fica disponível sem repor bolsas no estoque.

#### AC-062 — Entrega exige viagem ativa e coerente
- **Dado** transporte PLANNED, CANCELLED, DELIVERED ou lote incompatível
- **Quando** tenta concluir, inclusive em duas confirmações simultâneas
- **Então** a operação inválida retorna 422, transporte inexistente retorna 404, e somente uma confirmação registra a baixa sem reescrever o instante da entrega.

#### AC-063 — Falha no commit preserva o estado anterior
- **Dado** falha tardia de persistência durante início ou conclusão
- **Quando** a transação falha ao confirmar
- **Então** nenhuma alteração parcial persiste em transporte, pedido, alocação, itens ou bolsas e uma tentativa posterior válida consegue concluir.

#### AC-064 — Acesso por papel, vínculo e CSRF
- **Dado** sessões ausentes, papéis incompatíveis, outro centro/hospital ou escrita sem CSRF
- **Quando** consomem início, conclusão ou cadastro de veículo
- **Então** sessão ausente retorna 401, papel/CSRF inválido 403, recurso de outro centro/hospital 404 sem alteração e UUID/payload inválido 400; somente HEMOCENTRO inicia e HOSPITAL do destino conclui.

### US-025 — Preparar veículos para distribuição
Como administrador, quero cadastrar veículos com código e capacidade em bolsas, para disponibilizar a frota no transporte.

#### AC-065 — Cadastro e consulta da frota
- **Dado** administrador e código ainda não cadastrado
- **Quando** envia POST /api/v1/vehicles com code não vazio e capacity positiva e consulta GET /api/v1/vehicles
- **Então** recebe 201 com id, code e capacity e a listagem autenticada inclui o veículo; duplicidade retorna 409, dados inválidos 400 e outros papéis não podem cadastrar.

#### AC-066 — Contrato documentado e migração compatível
- **Dado** aplicação e banco vazio ou migrado até V15
- **Quando** inicia e consulta /v3/api-docs
- **Então** a nova migração preserva dados e impede duas viagens IN_TRANSIT no mesmo veículo, e OpenAPI documenta os quatro endpoints novos, campos, unidades, respostas, autenticação e CSRF.

## Fora de escopo
- GPS, temperatura e alertas seguem para RSDUS03, SOUS03 e AEDUS10; estatísticas agregadas e dashboards seguem histórias próprias.
- Planejamento prévio ou cancelamento de viagem, múltiplas alocações por viagem, edição/exclusão de veículos e frota por centro.
- Recalcular rota no início, consultar OSRM sob lock ou ampliar a alocação além de hemácias.

## Suposições
| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-012 | Frota global, capacity em bolsas, uma alocação completa por viagem. | confirmada | Preservado o modelo existente e o fluxo allocationId/vehicleId confirmado pelo usuário. |

## Perguntas em aberto
| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-016 | Quem inicia e conclui, e a telemetria entra agora? | respondida | Usuário confirmou HEMOCENTRO de origem, HOSPITAL do destino e telemetria posterior. |
| Q-017 | Como preparar veículos? | respondida | Usuário confirmou cadastro/listagem no Swagger, escrita ADMIN. |
