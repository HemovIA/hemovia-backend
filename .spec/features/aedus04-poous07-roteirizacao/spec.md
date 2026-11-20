# AEDUS04 e POOUS07 — Rotas reais na rede cadastrada

> feature: aedus04-poous07-roteirizacao
> status: auditada

## Contexto

Implementar [AEDUS04, issue #18](https://github.com/HemovIA/hemovia-backend/issues/18) e [POOUS07, issue #9](https://github.com/HemovIA/hemovia-backend/issues/9), partindo da develop a48cb94 com a POOUS10 integrada. O usuário escolheu ruas reais: OSRM/OpenStreetMap fornece trajeto, distância e duração de cada ligação; um Dijkstra próprio escolhe o menor tempo no grafo de ligações cadastradas. Não se promete o ótimo global de todas as ruas fora desse grafo.

## Histórias

### US-021 — Encontrar o percurso de menor tempo com trechos reais

Como módulo logístico, quero calcular o menor caminho entre os pontos cadastrados da hemorrede usando os tempos dos trechos de rua, para respeitar o prazo de entrega.

#### AC-048 — Dijkstra devolve o menor caminho acumulado

- **Dado** um grafo com caminhos alternativos e pesos positivos, incluindo um caminho mais curto em distância porém mais demorado
- **Quando** o algoritmo próprio procura o percurso entre origem e destino
- **Então** devolve os vértices e os trechos do caminho de menor duração, somando distância e tempo das arestas selecionadas
- **E** respeita o sentido dos trechos, trata ciclos sem repetir indefinidamente e rejeita origem, destino ou conexões inexistentes com INVALID_ROUTE

#### AC-049 — Rejeitar trajetos que ultrapassam a janela de entrega

- **Dado** um trajeto de plaquetas de 85 minutos e o limite didático de 60 minutos previsto no backlog, ou um trajeto cuja chegada estimada ultrapassa deliveryDeadline
- **Quando** o serviço valida a rota com o relógio injetado
- **Então** rejeita a rota com INVALID_ROUTE; uma rota exatamente no limite é aceita
- **E** o limite de plaquetas é configurável e o prazo é respeitado para todos os componentes quando informado

#### AC-050 — Obter os dados reais da ligação e tratar falhas do provedor

- **Dado** dois pontos geográficos válidos e uma resposta OSRM com geometria GeoJSON LineString, distância em metros e duração em segundos
- **Quando** o administrador cadastra uma ligação
- **Então** a aplicação consulta o serviço no sentido origem-destino, converte distância para quilômetros e preserva a geometria e a duração sem pesos inventados
- **E** NoRoute ou NoSegment resulta em 422 com INVALID_ROUTE; timeout, resposta inválida ou falha do provedor resulta em 503 com ROUTING_PROVIDER_UNAVAILABLE, sem gravar uma ligação parcial
- **E** as chamadas ao servidor público respeitam no máximo uma consulta por segundo e os testes automatizados usam um servidor HTTP local controlado

### US-022 — Associar e exibir a rota de uma alocação sem perder a integridade do estoque

Como despachante do hemocentro, quero receber a rota da requisição alocada, para acompanhar seus pontos, distância e tempo estimado.

#### AC-051 — Preparar os pontos e as ligações pelo Swagger com acesso protegido

- **Dado** um administrador autenticado com CSRF válido e um hemocentro, hospital ou cruzamento
- **Quando** cadastra e lista pontos e ligações em /api/v1/route-graph/nodes e /api/v1/route-graph/edges
- **Então** recebe IDs persistidos e os dados dos registros, com 201 na criação e 200 na consulta
- **E** pontos vinculados usam as coordenadas do hospital ou hemocentro; cruzamentos exigem coordenadas próprias; vínculos inexistentes ou hemocentros excluídos retornam 404
- **E** dados inválidos retornam 400, duplicidades retornam 409, sessão ausente com CSRF válido retorna 401 e escrita por outro papel ou sem CSRF válido retorna 403
- **E** cada ligação é direcional, e o cliente não fornece seu tempo ou distância manualmente

#### AC-052 — Alocar com rota persistida e exibida na resposta

- **Dado** uma requisição pendente de hemácias, estoque compatível suficiente e um caminho viável entre o hemocentro da sessão e o hospital solicitante
- **Quando** o operador envia POST /api/v1/requests/{id}/allocate
- **Então** recebe 201 com os campos anteriores da alocação e o novo objeto route, contendo ID da rota, IDs dos vértices, geometria GeoJSON, distância em quilômetros e duração estimada em segundos
- **E** a alocação referencia a rota persistida, com a ordem dos pontos e a geometria preservadas, as bolsas ficam RESERVED e a requisição fica ALLOCATED
- **E** a rota é um registro histórico e não depende de consultar o OSRM novamente para devolver a resposta

#### AC-053 — Falha de rota desfaz toda a reserva

- **Dado** estoque suficiente e um hospital sem conexão no grafo ou uma rota que ultrapassa o prazo da requisição
- **Quando** o operador tenta alocar
- **Então** recebe 422 com INVALID_ROUTE, a requisição continua PENDING, as bolsas permanecem AVAILABLE e não ficam alocação, itens ou rota parcial no banco

#### AC-054 — Consultar uma prévia e documentar as operações no Swagger

- **Dado** um operador de hemocentro autenticado com CSRF e um pedido de prévia com hospital de destino, componente e prazo
- **Quando** envia POST /api/v1/routes/calculate
- **Então** recebe 200 com vértices, geometria, distância e duração, sem criar rota persistida, alocação ou reserva
- **E** consegue testar a regra de plaquetas pela prévia, enquanto a alocação continua restrita a hemácias
- **E** o OpenAPI documenta os novos endpoints, papéis, cookieAuth, X-XSRF-TOKEN, unidades e códigos de erro, preservando emissão, alocação e cancelamento

#### AC-055 — Falha de persistência da rota causa rollback completo

- **Dado** uma alocação com percurso viável e uma falha de banco provocada no commit após gravar a rota e as reservas
- **Quando** a transação é encerrada
- **Então** retorna erro interno e preserva o estado anterior do pedido e das bolsas, sem deixar rota, alocação ou itens órfãos
- **E** a mesma requisição pode ser atendida em uma nova tentativa sem a falha

#### AC-056 — Preservar concorrência e cancelamento com histórico de rota

- **Dado** a reserva concorrente da última bolsa ou o cancelamento de uma requisição alocada com rota
- **Quando** os comandos são executados com PostgreSQL real
- **Então** somente uma reserva ativa pode existir para a mesma bolsa, e o cancelamento libera o estoque sem apagar a rota histórica
- **E** os testes existentes de rollback e de disputa entre cancelamento e alocação continuam aprovados, agora com grafos válidos nas suas fixtures

## Desenho

O contrato técnico e a divisão dos arquivos estão em design.md e tasks.md. São mantidas as cinco camadas e as migrações V1 a V14. A V15 adiciona metadados e geometria às ligações, vínculo opcional de rota à alocação e os IDs ordenados dos vértices do percurso histórico, sem inventar rotas para registros antigos.

## Fora de escopo

- Criar ou iniciar Transport, selecionar veículo, confirmar entrega e acompanhar temperatura. O vínculo da rota com Transport será implementado na POOUS09, conforme recorte aprovado pelo usuário; esta parte da descrição original da POOUS07 permanece explicitamente adiada.
- Mapa visual de frontend, navegação curva a curva, trânsito em tempo real, cadastro de endereço por geocodificação, importação automática de toda a malha viária e otimização de toda a rede de ruas.
- Alocar plaquetas ou outros componentes além de hemácias; a regra didática de plaquetas é demonstrada na prévia e nos testes.
- CRUD completo e atualização automática da malha: esta entrega cadastra e lista pontos e ligações. Os trechos são registros dos dados consultados no provedor no momento do cadastro.

## Verificação

- A suíte completa executou 415 testes, com zero falhas, erros ou testes ignorados após atualizar o inventário de migrações para incluir exatamente V1 a V15. As verificações de integridade das migrações publicadas foram preservadas.
- Os nove critérios novos foram comprovados pelo runner em .spec/verification/aedus04-poous07-roteirizacao.json. As oito especificações anteriores também tiveram provas renovadas contra o mesmo código, sem alterações posteriores nos fontes ou testes.
- O cliente Java real consultou o OSRM para as coordenadas de Recife usadas na verificação pontual e recebeu 3.867 km, 348 segundos e 145 pontos de geometria. As suítes automatizadas usam apenas respostas locais controladas, sem depender do servidor público.
- A auditoria em modo CI saiu 0 com 56/56 critérios provados e um único aviso preexistente de P-002, sobre o glob src/**/*.js sem arquivos. O relatório gerado está em auditoria.md.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-010 | OSRM fornece trechos reais e Dijkstra próprio escolhe a sequência das ligações cadastradas, em vez de calcular sobre toda a malha de ruas do mundo. | confirmada | Usuário escolheu OSRM com Dijkstra integrado à rede cadastrada em 20/11/2026. |
| ASM-011 | Vincular a rota à alocação agora; vínculo com Transport fica para POOUS09; manter hemácias na alocação e limite didático de plaquetas na prévia. | confirmada | Usuário confirmou esse recorte em 20/11/2026. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-013 | Qual origem dos dados de ruas e como preservar o Dijkstra próprio? | respondida | OSRM para trechos reais; grafo cadastrado e seleção por Dijkstra próprio. |
| Q-014 | Quando associar a rota ao transporte e qual recorte dos componentes? | respondida | Associação ao transporte na POOUS09; alocação de hemácias; prévia respeita prazo e limite didático de 60 minutos para plaquetas. |
| Q-015 | Quais tarefas paralelizar e quais modelos e esforços usar? | respondida | Usuário confirmou T-028 e T-029 em paralelo; T-030, T-031 e T-032 em sequência; GPT-6.1 Terra, esforço médio nas cinco, em 20/11/2026. |
