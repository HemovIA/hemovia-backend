# Desenho de transporte e entrega

## Contratos
- StartTransport.execute(StartTransportRequest) e CompleteTransport.execute(UUID), em service/usecase/transport, retornam TransportResponse. Usar CurrentUserProvider e ClockProvider existentes.
- StartTransportRequest(UUID allocationId, UUID vehicleId), ambos NotNull. TransportResponse(UUID id, UUID allocationId, UUID requestId, UUID routeId, UUID vehicleId, TransportStatus status, Instant startedAt, Instant completedAt).
- TransportLifecycleStore em repository/port e adaptador encapsulam consultas e persistência; regras e mutações são do serviço. Repositórios não importam DTO HTTP. Compartilhar validação interna em TransportLifecycle se necessário, sem interfaces vazias.
- ManageVehicles.create(VehicleRequest) e list(), em service/usecase/vehicle, usam VehicleRepository existente. VehicleRequest(String code, Integer capacity), NotBlank, limite da coluna e Positive/NotNull. VehicleResponse(UUID id, String code, Integer capacity). Aparar code antes de verificar/gravar. Listagem ordenada por code/id.
- Manter controller, service, dto, repository e model; DTOs records, sem entidades em controllers, sem novo Javadoc e Java legível.

## Consistência
- Transações READ_COMMITTED. Descobrir requestId por projeção escalar de allocation/transport; bloquear pedido com o mesmo PESSIMISTIC_WRITE de alocação/cancelamento antes de carregar entidades. Evitar snapshots obsoletos do Hibernate.
- Ordem: pedido, centro ativo (só início, lock compartilhado de novos vínculos), veículo PESSIMISTIC_WRITE, bolsas por UUID ordenado. Start e complete bloqueiam veículo antes de alterar disponibilidade. Cancelamento segura pedido e bolsas, sem ciclo com veículo.
- Exigir uma única alocação ativa do pedido, lote completo (quantidade do pedido), sem duplicidade de bolsas nem reservas ativas de outro pedido, releasedAt nulo, centro/componente e estados coerentes. Não iniciar fragmento legado com várias alocações ativas.
- Início: pedido/alocação ALLOCATED, itens/bolsas RESERVED, rota do centro e hospital corretos, duração positiva e janela válida no instante real de saída (DeliveryWindow e configuração atuais). Reusar snapshot sem OSRM. Impedir qualquer transporte anterior da alocação, inclusive PLANNED legado.
- Validar capacidade em bolsas e consultar viagem IN_TRANSIT do veículo após seu lock. Criar Transport IN_TRANSIT com startedAt do relógio e alterar todos os estados juntos.
- Conclusão: transporte/pedido/alocação/itens/bolsas IN_TRANSIT, início existente, fim nulo e rota coerente. Não exigir centro ativo para receber viagem existente. Preservar início/rota e registrar fim uma vez; releasedAt continua nulo, pois entrega não libera estoque.
- V16 cria índice único parcial transports(vehicle_id) WHERE status='IN_TRANSIT'; manter unicidade allocation_id. Não alterar V1–V15 nem inventar backfill. Duplicidades legadas impedem migração com erro visível.
- Sem escritor de StockMovement adicional nesta entrega; os estados e vínculos persistidos provam o ciclo solicitado.

## HTTP
- POST /transports/start HEMOCENTRO 201; POST /transports/{id}/complete HOSPITAL 200. Recurso fora do vínculo 404. Escritas com cookie e X-XSRF-TOKEN.
- POST /vehicles ADMIN 201; GET /vehicles papéis de negócio autenticados 200. Quatro operações novas, inventário OpenAPI de 35 para 39.
- Erros 400 payload/UUID, 401 sessão, 403 papel/CSRF, 404 recurso/vínculo, 409 ocupado/duplicado, 422 estado/capacidade/janela. VehicleUnavailableException código VEHICLE_UNAVAILABLE já previsto no handler; InvalidRouteException mantém INVALID_ROUTE.
- Swagger explícito application/json, unidades, erros e limites reais; sem prometer telemetria.

## Execução e verificação
Usuário confirmou T-033 e T-034 paralelas em arquivos disjuntos; T-035 e T-036 em sequência depois. Todas usam GPT-6.1 Terra com esforço médio. Agentes nativos no checkout existente; script headless gerado serve apenas como referência. Branch real feat/poous09-poous11-transporte-entrega prevalece sobre nomes padrão do gerador.

PostgreSQL isolado, concorrência determinística saída/saída, saída/cancelamento e entrega/entrega, falha tardia de commit e recuperação, capacidade, veículo ocupado, janela vencida, estados legados e autorização. Atualizar inventários finais para V16 sem remover checks históricos. Registrar testes no manifesto/fontes na configuração, confirmar sucesso da suíte inteira e renovar provas anteriores após congelar o código.

## Verificação executada

Em 20/11/2026, a suíte final executou 436 testes com zero falhas, erros ou skips. Os 21 casos adicionais incluem 12 testes de transporte, dois de veículos por HTTP, cinco unitários de veículos, um da V16 e um do contrato OpenAPI. O ciclo positivo verifica quatro bolsas nas duas transições; a concorrência verifica mesma alocação, disputa de veículo, início/cancelamento e duas entregas. Falhas tardias no commit são provocadas em início e conclusão, com recuperação posterior.

Com JDK 26.0.2, MAVEN_OPTS=-Daether.syncContext.named.factory=noop e PostgreSQL 17 temporário, foi executado scripts/spec-java/test-postgres.ps1 -Verify para esta feature e para as nove especificações anteriores. Todas as provas têm exitCode 0, e os resultados globais das rodadas completas foram conferidos nos relatórios Maven. Nenhum banco da aplicação foi usado. A auditoria CI terminou com código 0, 66/66 critérios provados e zero avisos. A verificação de P-002 agora examina api/src/main/java/**/*.java (224 arquivos), preservando o padrão proibido e o nível RECOMENDADO. Um controle temporário confirmou a detecção de literal sintético e a aceitação de leitura por variável de ambiente, sem incluir arquivos de teste no escopo de produção.

| Tarefa | Commit local | Resultado |
|---|---|---|
| T-033 | ce5b765 | Ciclo transacional, bloqueios e V16 |
| T-034 | 5c07d20 | Cadastro/listagem de veículos e testes unitários |
| T-035 | c77ba77 | Endpoints, permissões e Swagger |
| T-036 | d00ca95 | Testes PostgreSQL, inventários, rastreabilidade e guia |

Os artefatos gerados do plano representam a proposta aprovada antes da execução. O trabalho foi realizado pelos agentes nativos e revisado no checkout compartilhado, sem executar o script headless nem criar as branches padrão spec/ nele descritas. A branch efetivamente criada e usada é feat/poous09-poous11-transporte-entrega, conforme a preferência do usuário.
