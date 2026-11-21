# Hemovia | Especificação do frontend para o Figma Make

## 1. Entrega

Crie uma aplicação web responsiva e navegável em português do Brasil para a operação entre hospital e hemocentro. O percurso principal começa quando o hospital solicita hemácias e termina quando confirma o recebimento. Faça esse percurso funcionar primeiro. Depois complete estoque, doações, estatísticas e cadastros administrativos. A interface deve abrir em modo demonstração sem depender de credenciais, servidor ou provedor de mapas.

Use React e TypeScript, componentes reutilizáveis e rotas com URLs próprias. Se o projeto inicial já usar TanStack Router, Tailwind e shadcn/ui, preserve essa estrutura. Concentre consultas e comandos em uma camada de serviços com duas implementações explícitas: demonstração e HTTP. Os nomes das rotas de interface abaixo são decisões de design. Os caminhos da seção 7 são contratos do backend Hemovia, revisão de referência `c74c062`.

## 2. Identidade visual e navegação

- Interface de operação com fundo claro quente, superfícies brancas, bordas discretas, fonte Inter e ícones Lucide. Usar vermelho na ação principal, azul em transporte, âmbar em pendências e verde em conclusões. Uma ação primária por contexto. Identificar prioridade e status com texto.
- Tokens claros: fundo #FDFCFB, texto #2A2020, superfície #FFFFFF, borda #EAE6E3, ação #D32F2F, seleção #FDE8E6, texto da seleção #A8191A. Escuro: fundo #1C1415, superfície #281D1E, texto #F7F5F2, seleção #4A2224. Ajustar pares de cores de botões e badges para leitura em ambos os temas. Usar tokens nos componentes.
- Base de espaçamento 4 px, campos com rótulo permanente, título de página 22–28 px, texto 14–16 px, cards com raio 18 px. Gráficos com legenda, unidade e alternativa em tabela. Datas no formato brasileiro. Para prazos e eventos, mostrar o fuso local e enviar instantes ISO 8601. Datas civis de coleta e validade devem preservar o dia escolhido.
- A partir de 1024 px, sidebar esquerda de 256 px com seção ativa, nome da unidade quando conhecido, perfil, tema e saída. Abaixo disso, cabeçalho com botão “Abrir menu” e painel lateral. Referências de teste: 390 × 844 e 1440 × 900. Listas em cards no celular e tabelas no desktop. Formulários em página própria, confirmação em diálogo, filtros móveis em painel.
- Permitir navegação por teclado, foco visível, retorno do foco ao fechar diálogos, alvos de toque de pelo menos 44 × 44 px e mensagens de validação associadas aos campos. Manter acesso à ação principal com teclado virtual aberto.

## 3. Perfis e página inicial

Após o login, resolver o perfil e o vínculo retornados por `/auth/me`. A composição de menus abaixo é uma escolha de produto para o protótipo. As permissões efetivas vêm da API. Aplicar o mesmo controle ao entrar por um link direto.

| Perfil | Primeira tela e navegação | Ação central |
|---|---|---|
| Hospital | Início com seus pedidos por situação, Pedidos e Recebimentos | Solicitar componente, cancelar pedido próprio elegível e confirmar recebimento destinado ao seu hospital |
| Hemocentro | Início com estoque disponível e pedidos para alocação, Estoque, Doações, Pedidos, Logística e Estatísticas | Registrar bolsa, alocar hemácias, calcular prévia de rota e despachar veículo |
| Transportador | Minhas entregas e detalhe do trajeto associado ao veículo do usuário | Consultar origem, destino e andamento no cenário demonstrativo |
| Administrador | Início com indicadores, Estatísticas e Cadastros de usuários, unidades, veículos e rede de rotas | Gerenciar cadastros e consultar indicadores |

A tela do transportador e as filas de pedidos/recebimentos dependem de consultas ainda ausentes. No modo HTTP, aplicar as regras de disponibilidade da seção 7. No modo demonstração, permitir entrar com um dos quatro perfis por “Explorar demonstração” e trocar o perfil preservando o cenário. Na sessão real, obter o papel exclusivamente do usuário autenticado.

## 4. Telas e interações

### Acesso e início — /login e /

Campo único “E-mail ou CPF”, senha com opção de mostrar, botão “Entrar”, estado de envio e mensagem de credenciais recusadas. Em sessão expirada, voltar ao login com explicação. Mostrar o nome da unidade se conhecido. Evitar preencher nomes por suposição a partir de um UUID. No início, apresentar somente os atalhos e indicadores que o perfil consegue abrir. Ao tocar em um contador, abrir a lista já filtrada quando houver uma fonte de dados.

### Pedidos — /pedidos, /pedidos/novo e /pedidos/:id

Lista com código curto pesquisável, hospital, tipo e Rh, componente, quantidade de bolsas, prazo, prioridade e status. No celular, destacar prazo e ação disponível. Permitir copiar o UUID completo no detalhe. O hospital vê seus pedidos. O hemocentro vê a fila demonstrativa de pendentes e as operações vinculadas a ele. Filtros de situação e prioridade independentes.

Em “Novo pedido”, mostrar hospital da sessão como somente leitura. Solicitar uma combinação de tipo sanguíneo, Rh e componente, quantidade inteira positiva, prioridade e prazo futuro com data e hora. Exibir resumo antes do envio. Rótulos e valores:

| Campo | Valores |
|---|---|
| Tipo e Rh | A, B, AB ou O com positivo (`POSITIVE`) ou negativo (`NEGATIVE`) |
| Componente | Sangue total (`WHOLE_BLOOD`), hemácias (`RED_BLOOD_CELLS`), plasma (`PLASMA`), plaquetas (`PLATELETS`), crioprecipitado (`CRYOPRECIPITATE`) |
| Prioridade | Baixa (`LOW`), média (`MEDIUM`), alta (`HIGH`), crítica (`CRITICAL`) |

A criação aceita os cinco componentes. A alocação implementada atende **somente hemácias**. Para os demais, explicar essa limitação no detalhe. Enviar apenas `bloodType, rhFactor, component, quantity, priority, deliveryDeadline`. Usar o `id` e o `status` retornados para a confirmação. Bloquear envio duplicado enquanto aguarda.

### Detalhe do pedido e operação entre perfis

No topo, exibir código, status e prioridade em elementos distintos. Em seguida, resumo da solicitação, bolsas alocadas quando disponíveis, rota, veículo e histórico dos eventos conhecidos. Enquanto uma etapa estiver pendente, mostrar apenas seu nome. Usar horários de eventos disponíveis e indicar quando o horário é desconhecido.

| Estado do pedido | Ação e responsável | Resultado após sucesso |
|---|---|---|
| Pendente / PENDING | Hemocentro seleciona “Alocar bolsas” para hemácias | ALLOCATED. Guardar ID da alocação, IDs das bolsas reservadas e rota retornada |
| Alocado / ALLOCATED | Hemocentro da alocação seleciona veículo e confirma “Iniciar transporte” | IN_TRANSIT. Guardar ID do transporte e horário de saída |
| Em trânsito / IN_TRANSIT | Hospital de destino confirma “Receber entrega” | DELIVERED. Exibir horário de conclusão |
| Pendente ou alocado | Hospital solicitante confirma “Cancelar pedido” antes do transporte | CANCELLED. Uma alocação cancelada libera as bolsas reservadas |
| Entregue ou cancelado | Consulta | Linha do tempo encerrada |

“Alocar bolsas” é um comando ao servidor. A interface apresenta a seleção que ele devolve. No despacho, exibir código e capacidade do veículo em bolsas. Explicar estoque insuficiente, rota inviável, veículo ocupado ou capacidade insuficiente junto à ação. Manter a última situação confirmada em caso de falha. Ao cancelar, explicar o efeito antes da confirmação. Usar o ID específico de cada recurso ao chamar o próximo comando.

### Estoque e doações — /estoque, /estoque/novo e /doacoes

Mostrar bolsas individuais, com filtros locais de tipo/Rh, componente, status e hemocentro. Cada linha abre o detalhe por `bloodBagId`. Rótulos de status: disponível, reservada, em trânsito, entregue e descartada. Calcular “Validade atingida” como informação derivada da data de referência UTC, sem criar um enum EXPIRED. Mostrar contagens agregadas em uma área distinta da lista de bolsas.

Cadastro individual: tipo, Rh, componente, coleta e validade. Enviar `bloodTypeEnum` na escrita e ler `bloodType` na resposta. Enviar `status: AVAILABLE` no cadastro, que cria a bolsa disponível. Enviar as datas de entrada representando o dia escolhido em UTC. Validade posterior ao dia atual UTC e igual ou posterior à coleta. Vínculos `bloodCenterId` e `donorId` e volume `volumeMl` são opcionais. Para volume, rotular em mL. A resposta de leitura da bolsa omite o volume: evitar apresentar zero ou gravar um valor presumido numa edição. Na primeira entrega, priorizar consulta e cadastro individual.

Doações: lista, detalhe e cadastro. Informar centro de coleta (`collectionCenter`) e dia da coleta. Oferecer “Vincular bolsa existente” com seleção por ID ou “Registrar com nova bolsa” com tipo, Rh, componente e validade futura. Datas da doação em YYYY-MM-DD. Não criar uma bolsa em chamada separada quando o cadastro da doação já solicitar sua criação. Exibir os vínculos retornados pelo servidor.

### Logística e rastreio — /logistica e /rastreio/:id

Painel com alocações prontas para envio e transportes em andamento, pesquisáveis nos dados da demonstração. O detalhe de rastreio recebe o ID do transporte. Antes da partida, o detalhe da alocação já pode mostrar o trajeto planejado. Desenhar a linha usando GeoJSON em ordem longitude, latitude, marcar origem e destino e exibir alternativa textual com distância em km e duração estimada em minutos. Usar a distância e os segundos fornecidos pelo backend. Uma prévia de rota recebe hospital, componente e prazo. Calcular a prévia não reserva bolsas nem inicia transporte.

No rastreio, distinguir previsão de chegada de horário real de conclusão. Usar os horários do transporte para saída e entrega. O mapa representa o trajeto planejado. Não inventar localização GPS, temperatura, telefone de motorista ou movimento em tempo real. Na demonstração, garantir legibilidade do trajeto com mapa esquemático local.

### Estatísticas — /estatisticas

Abas “Estoque disponível” e “Demanda”. Na primeira, exibir total de bolsas elegíveis e distribuição por tipo/Rh, componente e combinação, com a data de referência retornada. O total elegível pode diferir do número de registros da lista de bolsas. Na segunda, oferecer filtro de período, total de requisições, total de bolsas, média, mediana e detalhamento por hospital. Em “Mais métricas”, distinguir variância em bolsas² de desvio padrão em bolsas e as versões amostral e populacional. Exibir o aviso retornado quando a amostra for insuficiente. Respeitar início inclusivo e fim exclusivo do intervalo. Usar os resultados da API e diferenciar “zero no período” de “consulta indisponível”.

### Administração — /cadastros

Usar abas com títulos e ações específicas. Mostrar confirmações para desativação e exclusão.

- **Usuários.** Listar, consultar, criar, editar nome/e-mail/telefone, alterar papel/vínculo e desativar. Cadastro com nome, e-mail, CPF, senha de pelo menos 6 caracteres e papel. Telefone opcional. Pedir apenas o vínculo pertinente: hospital para HOSPITAL, hemocentro para HEMOCENTRO, veículo para TRANSPORTADOR e nenhum para ADMIN. Evitar CPF na tabela de visão geral.
- **Hemocentros.** Lista, detalhe, criação, edição e exclusão. Nome, cidade e coordenadas. **Hospitais.** Cadastro com os mesmos campos e prioridade institucional de 1 a 5. Latitude entre −90 e 90, longitude entre −180 e 180. Onde for preciso escolher um hospital, usar o catálogo demonstrativo ou um ID conhecido na integração.
- **Veículos.** Listar e cadastrar código único e capacidade inteira positiva. A resposta traz ID, código e capacidade. Não deduzir disponibilidade do simples fato de o veículo aparecer na lista.
- **Rede de rotas.** Listar e criar nós e ligações direcionais. Nó com nome e uma origem de coordenadas: vínculo com hospital, vínculo com hemocentro ou coordenadas próprias de cruzamento. Ligação por nó de partida e chegada. Distância e duração vêm do servidor.

## 5. Componentes e estados compartilhados

Reutilizar estrutura de navegação, campo com rótulo e erro, seletor de tipo/Rh, indicador de prioridade, badge de status, tabela/card responsivo, painel de filtros, resumo de rota, linha do tempo e confirmação de ação. Usar tema claro por padrão, com preferência de tema persistida no dispositivo.

Para cada consulta, desenhar carregamento, dados, vazio inicial, busca sem resultados e falha com “Tentar novamente”. Para cada mutação, mostrar envio em andamento, sucesso com resultado visível e erro mantendo o formulário. Distinguir “Nenhuma entrega atribuída” de “Consulta de entregas indisponível nesta integração”. Nunca preencher um indicador indisponível com zero. Após sucesso, atualizar os detalhes e reconsultar agregados suportados.

## 6. Cenário de demonstração

Manter um único conjunto de dados fictícios compartilhado por todas as telas e papéis. Inicializar uma vez com um hospital em Recife, um hemocentro, um veículo de capacidade 4, duas bolsas disponíveis O+ de hemácias do centro e usuários fictícios para os quatro papéis. Usar UUIDs estáveis, datas de validade futuras, prazo relativo a agora e rota curta viável. Permitir reiniciar o cenário com confirmação. Persistir apenas o cenário fictício localmente para que sobreviva à atualização da página.

1. Hospital solicita duas bolsas de hemácias O+, prioridade alta, prazo amanhã. O pedido aparece pendente.
2. Hemocentro abre o mesmo pedido, aloca as duas bolsas e vê o trajeto. As duas passam a reservadas e saem do total disponível.
3. Hemocentro seleciona o veículo e inicia o transporte. Hospital e transportador veem a saída. Somente o hospital de destino vê “Receber entrega”.
4. Hospital confirma o recebimento. Pedido, transporte e bolsas aparecem entregues. A atualização da página preserva o resultado.
5. Cenários alternativos após reiniciar: pedir três bolsas com duas disponíveis mantém o pedido pendente após falha na alocação. Cancelar após alocar e antes da partida devolve as duas ao estoque. Solicitar plasma cria um pedido cujo detalhe explica a restrição de alocação.

## 7. Integração HTTP e limites dos dados

Base configurável por ambiente com prefixo `/api/v1`. A tabela usa caminhos relativos a esse prefixo. Nunca apontar automaticamente uma demonstração para a API real. Em modo integrado, usar cookies com credenciais e a origem permitida pelo backend.

| Recurso | Operações existentes utilizadas no frontend |
|---|---|
| Sessão | GET /auth/csrf, POST /auth/login, GET /auth/me, POST /auth/refresh, POST /auth/logout |
| Bolsas e doações | GET e POST /blood-bags e /donations. GET, PUT e DELETE no respectivo /{id} |
| Pedidos | POST /requests, POST /requests/{id}/allocate, POST /requests/{id}/cancel |
| Transporte | POST /transports/start com allocationId e vehicleId. POST /transports/{id}/complete |
| Rotas | POST /routes/calculate com hospitalId, component e deliveryDeadline. GET e POST /route-graph/nodes e /route-graph/edges |
| Estatísticas | GET /statistics/inventory e GET /statistics/demand com from e to opcionais |
| Unidades e frota | GET e POST /blood-centers, GET/PUT/DELETE /blood-centers/{id}, POST /hospitals, GET e POST /vehicles |
| Usuários | GET e POST /users, GET e PUT /users/{id}, PATCH /users/{id}/role e PATCH /users/{id}/deactivate |

Login usa `identifier` e `password`; a sessão é mantida por cookies HttpOnly. Para operações protegidas por CSRF, obter o cookie via GET /auth/csrf e devolver seu valor no header X-XSRF-TOKEN. Login é isento. Em 401 de sessão, tentar uma renovação coordenada com CSRF antes de voltar ao acesso. Não guardar credenciais no armazenamento do navegador nem repetir automaticamente um comando após falha de rede com resultado desconhecido.

**Consultas ausentes nesta revisão:** listagem e detalhe persistente de pedidos, alocações e transportes, histórico completo, listagem de hospitais e posição atual do veículo. No modo demonstração, obter listas, busca e histórico do repositório fictício. No integrado, exibir informações conhecidas de respostas confirmadas da sessão ou um estado de consulta indisponível. Após recarregar uma URL sem fonte de leitura, explicar a limitação. Evitar chamadas a endpoints presumidos ou mistura silenciosa de dados reais e fictícios.

**Erros:** ler `ApiError.message` e associar `fieldErrors` aos campos correspondentes, quando presentes. 400: corrigir dados. 401: recuperar sessão. 403: tratar acesso recusado ou falha de CSRF conforme o erro. 404: recurso indisponível. 409: conflito, por exemplo estoque insuficiente ou pedido já processado. 422: explicar regra de estado, componente ou rota. Manter a ação de recuperação específica ao erro.

## 8. Critérios para considerar o frontend pronto

- O percurso da seção 6 funciona de ponta a ponta no celular e no desktop, inclusive ao trocar papel e atualizar a página. O ID do pedido é o mesmo em todas as etapas.
- Cada ação depende do perfil, do vínculo e da situação do recurso. O menu, os atalhos e a URL direta respeitam a mesma regra. Uma pessoa do transporte não consegue confirmar o recebimento.
- Um prazo vencido ou uma quantidade inválida impede o envio e identifica o campo. Duplo clique não cria duas solicitações. Uma falha na operação mantém a situação anterior.
- Os totais, as bolsas reservadas, a lista e os detalhes refletem a mesma operação. As estatísticas identificam a unidade de medida e lidam com amostra insuficiente.
- Todo controle visível tem um comportamento definido. A prévia de rota, a viagem em andamento e a entrega concluída têm apresentações distintas. A integração sinaliza consultas indisponíveis, e a demonstração permanece utilizável sem rede.

Referência técnica no repositório: api/src/main/java/com/hemovia/api, especialmente controller, controller/security/SecurityConfig.java, dto/request, dto/response, model/enums e service/usecase. Em divergência futura de contrato, atualizar o adaptador e esta especificação a partir do backend.

