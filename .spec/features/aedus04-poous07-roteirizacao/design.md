# Desenho de rotas reais na rede cadastrada

## Responsabilidades

- model: RoutingGraph, GeoPoint e Dijkstra puros, validação da janela, InvalidRouteException, entidades RouteNode e RouteEdge e histórico DeliveryRoute.
- repository: portas e adaptadores para a malha e a persistência. Repositórios não recebem DTOs HTTP.
- service: cadastro de pontos/trechos, cliente OSRM atrás de uma porta, cálculo de rota e orquestração transacional da alocação.
- dto: records de entrada e saída, com coleções copiadas defensivamente. GeoJSON usa a ordem longitude/latitude.
- controller: HTTP, códigos de resposta, Swagger e autorização por rota. Sem acesso a entidades ou persistência.

## Contratos entre tarefas

- GeoPoint(double latitude, double longitude), em model/valueobject, valida coordenadas finitas.
- RoutingGraph em model/algorithm/graph contém listas imutáveis de Node(UUID id, GeoPoint point) e Edge(UUID id, UUID from, UUID to, BigDecimal distanceKm, long durationSeconds, List<GeoPoint> geometry). As arestas são direcionais; geometrias são preservadas na ordem de viagem.
- Dijkstra.find(RoutingGraph graph, UUID origin, UUID destination) é estático e retorna Path(List<UUID> nodeIds, List<RoutingGraph.Edge> edges, BigDecimal distanceKm, long durationSeconds). Retorna o menor tempo no grafo; não usa um simples algoritmo guloso de vizinho mais próximo. Ausência de caminho, IDs inválidos ou pesos inválidos lançam InvalidRouteException, código INVALID_ROUTE.
- DeliveryWindow.validate(BloodComponent component, Instant deadline, Instant departure, long durationSeconds, long plateletsMaxSeconds) valida chegada <= prazo e duração <= limite de plaquetas. Não inventa prazos para pedidos legados sem deliveryDeadline. Novas requisições já exigem prazo na emissão.
- RoadRoutingProvider.route(GeoPoint from, GeoPoint to) retorna RoadSegment(BigDecimal distanceKm, long durationSeconds, List<GeoPoint> geometry). A porta fica em service/port/output; os records de domínio ficam no model. O cliente OSRM não conhece DTOs HTTP de negócio.
- RouteGraphStore, em repository/port, oferece leitura/gravação das entidades da malha, findNodeForBloodCenter(UUID) e findNodeForHospital(UUID), que retornam Optional<RouteNode>, e loadGraph(), que devolve o grafo de trechos com geometria e dados do provedor. Não usa ligações legadas sem geometria como se fossem rotas reais.
- A gravação de uma ligação usa NewRouteEdge(UUID fromNodeId, UUID toNodeId, RoadSegment segment, String provider, Instant calculatedAt), e a leitura devolve StoredRouteEdge(UUID id, UUID fromNodeId, UUID toNodeId, RoadSegment segment, String provider, Instant calculatedAt), records em dto.internal. RoadGeometryCodec e a representação JSON da coluna são detalhes do adaptador de persistência; o serviço não importa nem usa o codec do repositório.
- ManageRouteGraph, em service/usecase/route, expõe createNode(CreateRouteNodeRequest), listNodes(), createEdge(CreateRouteEdgeRequest) e listEdges(), devolvendo os DTOs definidos pela T-030.
- Os DTOs compartilhados têm contratos fixos: RouteNodeResponse(UUID id, String name, double latitude, double longitude, UUID bloodCenterId, UUID hospitalId); GeoJsonLineString(String type, List<List<Double>> coordinates); RouteEdgeResponse(UUID id, UUID fromNodeId, UUID toNodeId, BigDecimal distanceKm, long travelSeconds, GeoJsonLineString geometry, String provider, Instant calculatedAt). GeoJsonLineString pertence à T-030 e é reutilizado pela T-031.
- CalculateDeliveryRoute.execute(UUID centerId, UUID hospitalId, BloodComponent component, Instant deadline) devolve Dijkstra.Path após carregar o grafo e validar a janela. PreviewDeliveryRoute.execute(CalculateRouteRequest) deriva o hemocentro da sessão e devolve RoutePreviewResponse.

## Contratos HTTP

- POST /route-graph/nodes e GET /route-graph/nodes: criação restrita a ADMIN; leitura autenticada com papel de negócio. CreateRouteNodeRequest contém nome, bloodCenterId ou hospitalId opcionais e exclusivos, e latitude/longitude opcionais. Um vínculo usa as coordenadas do recurso cadastrado; um cruzamento sem vínculo exige as duas coordenadas. Coordenadas fornecidas junto de um vínculo são rejeitadas para não deixar duas origens de verdade.
- POST /route-graph/edges e GET /route-graph/edges: criação restrita a ADMIN. CreateRouteEdgeRequest recebe apenas fromNodeId e toNodeId. O serviço consulta o OSRM e grava uma aresta direcional com bidirectional=false; a viagem inversa requer outra consulta e outro registro. A resposta inclui custo, geometria e data de consulta. Não aceita peso manual.
- POST /routes/calculate: HEMOCENTRO com CSRF. CalculateRouteRequest contém hospitalId, component e deliveryDeadline. A resposta é uma prévia sem ID de rota e sem escrita no estoque.
- POST /requests/{id}/allocate: contrato anterior preservado com campo adicional route. RouteResponse contém id, nodeIds, distanceKm, estimatedDurationSeconds e geometry GeoJSON LineString. RoutePreviewResponse possui os mesmos dados exceto o ID. Todos os pontos da geometria são longitude/latitude; unidades aparecem no Swagger.
- Falhas de grafo/rota/janela: 422 INVALID_ROUTE. Falhas do serviço de mapas: 503 ROUTING_PROVIDER_UNAVAILABLE. Duplicidades: 409; payload inválido: 400; sessão/papel/CSRF seguem o padrão atual.

## Persistência e consistência

- Reutilizar route_nodes e route_edges da V12. Nova V15 adiciona colunas nullable para geometria JSON, provedor e instante da consulta às arestas antigas. Novos cadastros exigem esses dados e o carregamento da malha ignora arestas sem dados reais.
- Nova V15 adiciona allocations.route_id nullable com FK a delivery_routes; não faz backfill fictício. DeliveryRoute mantém waypoints como geometria de ruas e ganha nodeIds ordenados em delivery_route_nodes, preservando o histórico sem FK para nós mutáveis da malha.
- Cálculo na alocação usa dados previamente consultados no cadastro do trecho: não faz chamadas externas enquanto segura os bloqueios de estoque. A chamada CalculateDeliveryRoute ocorre após a reserva no mesmo limite transacional; falha desfaz pedido, bolsas, itens, alocação e rota.
- Persistir a rota e seu vínculo antes do commit. O cancelamento preserva as rotas já associadas, assim como preserva a alocação cancelada.
- Coordenadas de nós e custos de trechos são um retrato no momento do cadastro. Mudanças posteriores na localização de um recurso não recalculam automaticamente a malha nesta entrega; o guia deve explicar essa limitação e o preparo da rede antes da alocação.

## OSRM

- URL e timeouts configuráveis por propriedades próprias em service/config, sem modificar o contrato de HemoviaProperties. URL padrão de demonstração: https://router.project-osrm.org.
- GET /route/v1/driving/{lon},{lat};{lon},{lat}?overview=full&geometries=geojson&steps=false. User-Agent identifica o projeto. Respeitar no máximo uma chamada por segundo ao servidor público e usar timeouts finitos; não fazer tentativas ilimitadas nem inventar rota quando o serviço falhar.
- Validar code, distância/duração finitas e positivas e geometria LineString com ao menos dois pontos válidos. Converter metros para quilômetros; arredondar duração fracionária para cima ao armazenar segundos inteiros, sem reduzir a janela de segurança.
- Fontes verificadas: https://project-osrm.org/docs/v26.4.0/http e https://github.com/Project-OSRM/osrm-backend/wiki/Demo-server.
- Consulta de disponibilidade em 20/11/2026: Recife, coordenadas -34.90,-8.06 e -34.88,-8.05, code=Ok, 3866.6 metros, 348 segundos, LineString com 145 coordenadas. Este resultado é apenas uma consulta pontual, não uma expectativa fixa dos testes.
- Consulta pontual posterior pelo próprio cliente Java OsrmRoadRoutingProvider também foi aprovada: 3.867 km após normalização para a precisão do banco, 348 segundos e 145 pontos. Nenhum dado foi gravado no banco da aplicação.

## Verificação

Testes do Dijkstra e da janela são puros. O cliente de mapas usa servidor HTTP local com respostas e falhas controladas. Testes de negócio usam PostgreSQL real; cenários antigos de alocação/cancelamento recebem malhas de teste explícitas e limpeza de rotas, sem desabilitar a integração ou enfraquecer as verificações. O contrato OpenAPI passa a conferir route e os endpoints novos. O uso real do provedor é uma consulta pontual separada dos testes automatizados.

## Execução do plano

O usuário confirmou T-028 e T-029 em paralelo, seguidas de T-030, T-031 e T-032 em sequência, todas com GPT-6.1 Terra e esforço médio. A execução usa agentes nativos em contextos separados, com arquivos disjuntos, no checkout existente codex/aedus04-poous07-roteirizacao. Os arquivos do executor headless são a referência gerada do plano e não foram executados. Cada tarefa terá um commit local próprio após validação, e a auditoria final exige provas atualizadas de todas as especificações.
