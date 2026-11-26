# Hemovia · Rota Vital

Infraestrutura de Software · CESAR School · ADS · 2026.2  
Trabalho U2 · Parte 2 · Planejamento da remodelagem

**Squad Hemovia**  
Alice Sena, Abraão Santos, Cecília Lopes, Matheus, Emily Raquel e Vinicius Wagner.

**Repositório do PI**  
[HemovIA/hemovia-backend](https://github.com/HemovIA/hemovia-backend)

## Ponto de partida da Parte 1

A demonstração roda em **Amazon Lightsail, Ohio (us-east-2)**. A CI do backend testa com Maven e PostgreSQL e publica a imagem por SHA no GHCR. A CI do frontend verifica o build Vite. O workflow manual de publicação exige CI verde para os dois commits da main. Ele empacota imagem, SPA e configuração, assume uma função AWS por OIDC, grava o pacote no S3 privado e aciona o Systems Manager. A VM aplica a versão com Docker Compose.

Caddy serve a SPA e encaminha `/api/*` ao Spring Boot na mesma origem HTTPS. API, PostgreSQL, observabilidade e Mailpit rodam na mesma VM. A ativação confere a prontidão da API e o conteúdo da SPA. O rollback de aplicação preserva os volumes. Migrações de schema têm ciclo próprio.

Em 26/11/2026, os quatro runs vinculados abaixo estavam concluídos com sucesso. A demo retornou HTTPS 200, a API sem sessão retornou 401 e o Grafana chegou à tela de login com 200. O rollback documentado retornou à versão de referência. A configuração da AWS foi conferida na main publicada. O checkout develop local ainda contém a configuração anterior.

**Decisão proposta**  
Preservar os cinco pacotes e organizar os quatro contextos dentro deles, mantendo o monólito modular na Parte 3.

[CI backend](https://github.com/HemovIA/hemovia-backend/actions/runs/45883071364) · [CI frontend](https://github.com/HemovIA/hemovia-frontend/actions/runs/45880544591) · [Deploy](https://github.com/HemovIA/hemovia-backend/actions/runs/45883467828) · [Rollback](https://github.com/HemovIA/hemovia-backend/actions/runs/45883714627) · [Demo HTTPS](https://18.216.26.95/) · [Diagrama](diagramas/deploy-atual.md)

<!-- página -->

## A. Remodelagem do domínio com DDD

### Subdomínios e contextos propostos

| Subdomínio e classe | Justificativa | Contexto e responsabilidade |
| --- | --- | --- |
| Gestão de hemocomponentes · core | Validade, compatibilidade e reserva segura determinam quais pedidos podem ser atendidos. | **Estoque** controla entrada, validade, disponibilidade e reserva das bolsas por hemocentro. |
| Atendimento hospitalar · core | O ciclo do pedido liga a demanda hospitalar ao atendimento rastreável. | **Requisições** registra a intenção do hospital e governa o estado do pedido até a entrega ou o cancelamento. |
| Logística de entrega · suporte | Roteamento e transporte operacionalizam o atendimento, e a malha viária pode vir de fornecedor externo. | **Expedição** calcula a rota, prepara a carga, acompanha o transporte e registra a entrega. |
| Autenticação e autorização · genérico | Sessão, credenciais e papéis usam padrões aplicáveis a outros sistemas. | **Identidade e Acesso** autentica pessoas e informa seu papel e vínculo institucional. |

### Mapa de contextos

<!-- diagrama:contextos -->

Na relação cliente–fornecedor, o contexto que precisa do contrato é o cliente. Requisições solicita a reserva a Estoque e a preparação de carga a Expedição. Identidade fornece a identidade, o papel e o vínculo aos três contextos pelo contrato de sessão. Expedição traduz o contrato viário externo por uma camada anticorrupção que converte identificadores, geometria e unidades para seu próprio modelo.

[Mapa Mermaid](diagramas/contextos.md)

<!-- página -->

## A. Glossário da linguagem ubíqua

| Termo | Contexto | Significado |
| --- | --- | --- |
| Bolsa | Estoque | Unidade de hemocomponente com identificação, validade e estado de disponibilidade. |
| Bolsa | Expedição | Unidade identificada na carga do transporte, usada para conferência no recebimento. |
| Entrada | Estoque | Inclusão de uma bolsa no inventário de um hemocentro. |
| Validade | Estoque | Data-limite usada para decidir se uma bolsa pode ser alocada. |
| Reserva | Estoque | Compromisso ativo que vincula uma bolsa a um pedido. |
| FEFO | Estoque | Prioridade de retirada para as bolsas elegíveis que vencem primeiro. |
| Requisição | Requisições | Pedido emitido por um hospital, com quantidade, hemocomponente, prioridade e prazo. |
| Alocação | Estoque | Seleção confirmada de todas as bolsas elegíveis que atendem ao pedido. |
| Cancelamento | Requisições | Encerramento do pedido antes do transporte, com liberação eventual da reserva quando os serviços forem separados. |
| Carga | Expedição | Conjunto de bolsas identificado para um transporte. |
| Rota | Expedição | Trajeto calculado entre hemocentro e hospital, com distância e duração. |
| Recebimento | Expedição | Confirmação pelo hospital de que a carga chegou. |
| Vínculo | Identidade e Acesso | Associação de um usuário à instituição ou ao veículo em nome do qual ele opera. |

Os contextos trocam identificadores e contratos explícitos. Estoque preserva a autoridade sobre disponibilidade. Expedição usa a lista de identificadores reservados como manifesto da carga. Requisições mantém a autoridade sobre o estado do pedido.

**Eventos propostos para a projeção distribuída**  
Estoque publica `ReservaConfirmada` e `ReservaLiberada`, consumidos por Requisições. Expedição publica `CargaPreparada`, `PreparacaoDeCargaFalhou` e `EntregaConfirmada`, também consumidos por Requisições.

<!-- página -->

## B. Agregado Alocação no contexto Estoque

**Raiz e conteúdo.** A raiz **Alocação** guarda a identidade da alocação, a referência da requisição e os itens reservados. Cada **Item de Reserva** é uma entidade interna com identidade e referência de bolsa. Os objetos de valor propostos são **Quantidade Solicitada**, inteiro positivo, e **Critérios de Seleção**, composto por hemocomponente, grupo ABO, fator Rh e data de referência. Bolsa e Requisição mantêm identidade própria e aparecem por ID.

**Invariantes da raiz.** Ao confirmar, a listagem deve conter exatamente a quantidade solicitada e cada ID de bolsa deve aparecer uma vez. Toda bolsa deve pertencer ao hemocentro da alocação, ser compatível com os critérios, estar disponível e vencer depois da data de referência. A política de seleção ordena as bolsas elegíveis por FEFO. O caso de uso persiste a alocação completa e as reservas em uma transação. Bloqueios e uma restrição única de reserva ativa protegem a exclusividade de uma bolsa entre agregados concorrentes.

**História âncora · POOUS06 / US-017**  
Como operador do hemocentro, quero alocar as bolsas de hemácias compatíveis que vencem primeiro para atender integralmente a uma requisição pendente.

1. Com estoque suficiente, a API confirma a reserva das bolsas elegíveis por ordem FEFO, devolve HTTP 201 e deixa a requisição em `ALLOCATED`. Cada bolsa selecionada fica em `RESERVED`.
2. Com estoque elegível abaixo da quantidade solicitada, a API devolve HTTP 409 com `INSUFFICIENT_INVENTORY`. A requisição continua em `PENDING`, os estados das bolsas são preservados e nenhuma alocação parcial é persistida.
3. Ao alocar um pedido já processado, a API devolve HTTP 409 com `REQUEST_ALREADY_PROCESSED` e preserva as reservas existentes.

### Cenário Gherkin · AC-031

```gherkin
Cenário: Manter o pedido pendente quando faltam bolsas elegíveis
  Dado que uma operadora do hemocentro está autenticada
  E há uma requisição pendente de 5 bolsas de hemácias AB negativo
  E o hemocentro tem apenas 2 bolsas elegíveis para essa requisição
  Quando a operadora solicita a alocação com CSRF válido
  Então a API responde 409 com o código "INSUFFICIENT_INVENTORY"
  E a requisição continua em "PENDING"
  E as 2 bolsas continuam em "AVAILABLE"
  E nenhuma alocação ou reserva é gravada para a requisição
```

A história e os testes existentes de AC-030, AC-031 e AC-033 sustentam o recorte. O arquivo Gherkin foi preparado para a Parte 3.

[Especificação](../../.spec/features/poous06-poous12/spec.md) · [Verificação existente](../../.spec/verification/poous06-poous12.json) · [Cenário](aceite/alocacao.feature)

<!-- página -->

## C. Cinco pacotes e três camadas lógicas

| Camada | Responsabilidade | Pacotes e execução |
| --- | --- | --- |
| Apresentação | Renderiza telas e traduz HTTP em operações da aplicação. | React no navegador. Caddy entrega a SPA. Na JVM, `controller` recebe e devolve `dto.request` e `dto.response`. |
| Negócio | Casos de uso orquestram operações. O domínio protege as invariantes. | `service.usecase` e tipos de domínio puro em `model`, na JVM. `dto.internal` pode carregar contratos internos. |
| Dados | Implementa persistência e integrações externas. | `repository` e `model.entity` (JPA) executam na JVM. PostgreSQL roda em contêiner próprio na mesma VM. |

**Dependência proposta.** Apresentação usa casos de uso. Casos de uso usam o domínio e os contratos de dados. O domínio usa tipos próprios e Java, sem importar controller, JPA ou framework web. `model.entity` continua no pacote atual com responsabilidade de persistência. A aplicação e os adaptadores mapeiam dados persistidos para objetos de domínio. `dto` organiza contratos presentes em diferentes camadas lógicas.

**Código atual.** ArchUnit impede que controllers acessem diretamente repositórios. O teste também veda dependência de service em controller e de JPA nos DTOs. A regra do modelo veda Spring, e entidades anotadas com JPA ficam em `model.entity`. O caso de uso de alocação usa entidades JPA e retorna DTOs de resposta. A separação do agregado puro e do mapeamento fica indicada para a Parte 3.

### Árvore proposta a partir dos cinco pacotes

```text
com.hemovia.api
├─ controller/{inventory, request, transport, auth}
├─ dto/{request, response, internal}/
│  └─ {inventory, request, transport, auth}
├─ service/usecase/{inventory, request, transport, auth}
├─ model
│  ├─ domain/{inventory, request, transport, auth}
│  └─ entity                                  (JPA existente)
└─ repository/{port, adapter, jpa, mapper}/
   └─ {inventory, request, transport, auth}
```

Os quatro contextos atravessam os pacotes pela organização em subpacotes e contratos. Uma referência entre contextos carrega o identificador, e o contexto proprietário governa os respectivos dados.

**Tiers no deploy atual.** React executa no navegador. Na VM Lightsail, Caddy serve o build estático. O processo Spring Boot executa controllers, negócio e adaptadores. PostgreSQL é outro contêiner da mesma VM e usa volume persistente. Os cenários regional e estadual projetam um banco separado. BDD continua descrevendo comportamentos observáveis da API, como o cenário de estoque insuficiente.

[ArchUnit publicado](https://github.com/HemovIA/hemovia-backend/blob/95bc4978c7fcd76c357c2e21dd8385317114df04/api/src/test/java/com/hemovia/api/architecture/RegrasDeArquitetura.java) · [Modelo atual](https://github.com/HemovIA/hemovia-backend/blob/95bc4978c7fcd76c357c2e21dd8385317114df04/api/src/main/java/com/hemovia/api/model/entity/Allocation.java)

<!-- página -->

## D. Projeção com microsserviços

| Serviço candidato | Motivo para extrair | Dados próprios |
| --- | --- | --- |
| Estoque | Criar uma fronteira transacional clara para o inventário e a disputa por bolsas. | PostgreSQL Estoque com bolsas, alocações, reservas e outbox. |
| Requisições | Dar autonomia ao ciclo do pedido e à coordenação de falhas. | PostgreSQL Requisições com pedidos, estado da saga e outbox. |
| Expedição | Escalar o cálculo viário e a ingestão logística de forma independente. | PostgreSQL Expedição com rotas, cargas, entregas e outbox. |
| Identidade e Acesso | Centralizar a emissão de credenciais e a consulta de vínculos quando houver vários consumidores. | PostgreSQL Identidade com usuários, vínculos e sessões. |

Consultas e criação do pedido entram por REST na borda. Entre serviços, comandos e eventos da saga passam por filas duráveis. Identidade emite credenciais verificáveis localmente e disponibiliza consulta REST de vínculo quando necessário. Banco por serviço significa que cada serviço possui seu próprio banco e migrações. Referências cruzadas usam IDs e contratos versionados.

### Saga de atendimento orquestrada por Requisições

1. Requisições grava o pedido e a saga, depois publica `ReservarBolsas`. Estoque confirma a reserva completa e publica `ReservaConfirmada`. Se faltar estoque, a saga encerra a tentativa e o pedido volta a aguardar atendimento.
2. Requisições envia `PrepararCarga`. Expedição calcula a rota, salva a carga e publica `CargaPreparada`. Requisições marca o pedido como `ALLOCATED`. O transporte só fica autorizado após confirmação dessa etapa.
3. Se a preparação falhar ou vencer o prazo, Requisições invalida a preparação daquela tentativa, solicita `CancelarPreparacao` a Expedição e aguarda confirmar que a carga não pode sair. Depois envia `LiberarReserva` a Estoque. Após `ReservaLiberada`, o pedido retorna a `PENDING` para nova tentativa.
4. Cada mensagem inclui ID da saga, da tentativa e da mensagem. Um outbox transacional evita perda entre gravação e publicação. Consumidores deduplicam e rejeitam transições fora de ordem. Falhas persistentes de compensação ficam em fila de mensagens não processadas com alerta e estado de compensação pendente.

[Diagrama da saga](diagramas/saga-atendimento.md)

<!-- página -->

## D. Comparação e decisões

| Dimensão | Monólito modular | Microsserviços |
| --- | --- | --- |
| Unidade de deploy | Uma imagem e uma esteira. | Uma unidade e um ciclo por serviço. |
| Escala | Replica a aplicação inteira. | Replica o serviço que concentra a carga. |
| Consistência | Transação local no banco compartilhado por módulos. | Transações locais por serviço e consistência eventual entre eles. |
| Custo | Menos processos, um banco operacional e uma esteira. | Vários bancos, mensageria e observabilidade distribuída. |
| Operação | Diagnóstico centrado na API e no banco. | Exige rastreamento, alertas e recuperação por serviço. |

**ADR 0001.** Adotar monólito modular na Parte 3 e projetar a extração por contexto. **ADR 0002.** Confirmar a reserva integral em uma transação local e introduzir saga com outbox se houver separação física.

A primeira decisão preserva uma esteira, um banco e um modelo transacional durante a remodelagem. O preço dessa escolha é escalar toda a API quando apenas um contexto concentra a carga. A equipe pode rever a extração após observar saturação recorrente de Expedição ou precisar de deploys independentes.

A segunda decisão preserva a reserva completa no banco atual. Se houver extração, a compensação torna visível a diferença entre uma falha em preparação e uma reversão já concluída. O squad precisa verificar idempotência e retomada da saga antes de aceitar a operação distribuída.

[ADR 0001](adr/0001-decomposicao.md) · [ADR 0002](adr/0002-consistencia-da-reserva.md)

<!-- página -->

## E. Premissas e Lei de Little

Carga sintética. Usuários representam sessões simultâneas no pico. Pedidos são requisições hospitalares criadas por dia. O pico HTTP inclui consultas, operações de estoque e transporte. O cenário alto usa os 10.000 req/min de SOUS05. As metas de disponibilidade são objetivos sobre o sucesso das operações durante 30 dias, a medir com sondas externas e métricas HTTP.

| Premissa | Baixo · piloto | Moderado · regional | Alto · estadual |
| --- | ---: | ---: | ---: |
| Usuários simultâneos no pico | 25 | 180 | 750 |
| Pedidos por dia | 120 | 600 | 2.400 |
| Pico HTTP por segundo | 10 | 60 | 166,67 |
| Disponibilidade desejada | 99,5% | 99,9% | 99,95% |
| Tempo médio HTTP assumido | 0,25 s | 0,25 s | 0,30 s |
| Tempo médio de ocupação JDBC | 0,08 s | 0,08 s | 0,10 s |
| Tráfego total nas VMs/mês | 20 GB | 100 GB | 500 GB |
| Dados PostgreSQL estimados | 10 GB | 20 GB | 100 GB |

**API.** Com `L = λ × W`, a concorrência média no pico é 2,50, 15,00 e 50,00. Assumindo 12 requisições simultâneas sustentáveis por réplica, em VM de 2 vCPUs com 4 GB ou 8 GB, o mínimo é `arredondar para cima(L ÷ 12)`. No alto, uma réplica adicional preserva a capacidade em caso de perda de uma instância.

**Banco.** Assume-se que 60% do tráfego HTTP usa uma conexão JDBC, uma por operação. A conta é `Lbd = λ × 0,60 × Wbd`. Para operar até 70% de ocupação média, arredonda-se para cima `Lbd ÷ 0,70`. Cada réplica pode abrir até quatro conexões HikariCP. Reservam-se outras dez para migrações e administração.

| Resultado no pico | Baixo | Moderado | Alto |
| --- | ---: | ---: | ---: |
| API · concorrência média | 2,50 | 15,00 | 50,00 |
| API · réplicas mínimas / provisionadas | 1 / 1 | 2 / 2 | 5 / 6 |
| Banco · conexões ativas médias | 0,48 | 2,88 | 10,00 |
| Banco · mínimo com folga | 1 | 5 | 15 |
| Banco · pool previsto da aplicação | 4 | 8 | 24 |
| Banco · conexões previstas com reserva | 14 | 18 | 34 |

No alto, com uma réplica indisponível, restam 60 vagas concorrentes da API e 20 conexões de pool. São estimativas para patamares sustentados. Os tempos são médias supostas, e as métricas locais ajudam a planejar a medição. A Parte 3 deve validar a capacidade de 12 por réplica, a ocupação JDBC, o p95, os 5xx e a recuperação sob carga sintética. Latência observada do Brasil até Ohio e saturação do banco precisam de medição. Conferir via SHOW max_connections se o banco comporta o pool, a reserva e as conexões internas.

[SOUS05 e SOUS06](../../diagramas/README.md#sous05) · [Evidências de observabilidade](../observabilidade/relatorio.pdf)

<!-- página -->

## E. Topologias por cenário

### Baixo · implantação piloto

<!-- diagrama:baixo -->

Uma VM de 8 GB hospeda Caddy, API, banco e observabilidade, seguindo a topologia da demo. O tamanho é uma premissa de orçamento. Uma falha na VM interrompe o serviço.

### Moderado · operação regional

<!-- diagrama:moderado -->

Duas VMs Web atrás do Lightsail Load Balancer, banco gerenciado Standard e uma VM para observabilidade. Uma única réplica oferece capacidade inferior ao pico.

### Alto · pico estadual

<!-- diagrama:alto -->

Seis VMs Web em duas zonas, banco gerenciado HA com standby em outra zona e uma VM para observabilidade. Cinco réplicas sustentam o pico estimado após perder uma VM. Perder uma zona inteira reduz a capacidade.

Nos cenários com balanceador, TLS exige domínio validável. O caminho de saúde do balanceador encaminha a `/readyz` da API. As VMs Web usam a mesma origem HTTPS, o mesmo banco e a mesma chave de assinatura de sessão. A distribuição do deploy para vários alvos e a separação do Compose são propostas.

[Baixo](diagramas/cenario-baixo.md) · [Moderado](diagramas/cenario-moderado.md) · [Alto](diagramas/cenario-alto.md) · [TLS do balanceador](https://docs.aws.amazon.com/lightsail/latest/userguide/create-tls-ssl-certificate-and-attach-to-lightsail-load-balancer-https.html)

<!-- página -->

## E. Orçamento mensal · piloto e regional

AWS **Ohio (us-east-2)**. Tarifas sob demanda consultadas em **26/11/2026**, sem créditos ou compromisso de compra. L = AWS Lightsail, S = catálogo regional S3 e M = AWS Systems Manager. Os totais usam as tarifas completas.

| Cenário | Item e unidade | Qtd. | USD/unid. | USD/mês | Região · fonte/data |
| --- | --- | ---: | ---: | ---: | --- |
| Baixo | VM Web Linux IPv4, 8 GB | 1 | 44,00 | 44,00 | Ohio · [L] · 26/11/26 |
| Baixo | Snapshots Lightsail, GB-mês | 30 | 0,05 | 1,50 | Ohio · [L] · 26/11/26 |
| Baixo | S3 Standard, GB-mês | 5 | 0,023 | 0,115 | Ohio · [S] · 26/11/26 |
| Baixo | S3 PUT/LIST, mil chamadas | 0,1 | 0,005 | 0,0005 | Ohio · [S] · 26/11/26 |
| Baixo | S3 GET/HEAD, mil chamadas | 0,1 | 0,0004 | 0,00004 | Ohio · [S] · 26/11/26 |
| Baixo | SSM Run Command, invocações | 8 | 0,002 | 0,016 | Ohio · [M] · 26/11/26 |
| Moderado | VM Web Linux IPv4, 4 GB | 2 | 24,00 | 48,00 | Ohio · [L] · 26/11/26 |
| Moderado | VM de observabilidade, 4 GB | 1 | 24,00 | 24,00 | Ohio · [L] · 26/11/26 |
| Moderado | PostgreSQL Standard, 2 GB | 1 | 30,00 | 30,00 | Ohio · [L] · 26/11/26 |
| Moderado | Lightsail Load Balancer | 1 | 18,00 | 18,00 | Ohio · [L] · 26/11/26 |
| Moderado | Snapshots Lightsail, GB-mês | 50 | 0,05 | 2,50 | Ohio · [L] · 26/11/26 |
| Moderado | S3 Standard, GB-mês | 10 | 0,023 | 0,23 | Ohio · [S] · 26/11/26 |
| Moderado | S3 PUT/LIST, mil chamadas | 0,2 | 0,005 | 0,001 | Ohio · [S] · 26/11/26 |
| Moderado | S3 GET/HEAD, mil chamadas | 0,2 | 0,0004 | 0,00008 | Ohio · [S] · 26/11/26 |
| Moderado | SSM Run Command, invocações | 24 | 0,002 | 0,048 | Ohio · [M] · 26/11/26 |

SSD e franquia de transferência fazem parte dos planos Lightsail. Os snapshots são estimados em GB-mês faturáveis. O Run Command foi orçado por invocação em cada nó híbrido. S3 considera o armazenamento e as operações dos pacotes de release.

[L]: https://aws.amazon.com/lightsail/pricing/
[S]: https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/AmazonS3/current/us-east-2/index.json
[M]: https://aws.amazon.com/systems-manager/pricing/

[Lightsail](https://aws.amazon.com/lightsail/pricing/) · [S3 Ohio](https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/AmazonS3/current/us-east-2/index.json) · [Systems Manager](https://aws.amazon.com/systems-manager/pricing/)

<!-- página -->

## E. Orçamento mensal · estadual e totais

| Cenário | Item e unidade | Qtd. | USD/unid. | USD/mês | Região · fonte/data |
| --- | --- | ---: | ---: | ---: | --- |
| Alto | VM Web Linux IPv4, 4 GB | 6 | 24,00 | 144,00 | Ohio · [L] · 26/11/26 |
| Alto | VM de observabilidade, 8 GB | 1 | 44,00 | 44,00 | Ohio · [L] · 26/11/26 |
| Alto | PostgreSQL HA, 4 GB | 1 | 120,00 | 120,00 | Ohio · [L] · 26/11/26 |
| Alto | Lightsail Load Balancer | 1 | 18,00 | 18,00 | Ohio · [L] · 26/11/26 |
| Alto | Snapshots Lightsail, GB-mês | 80 | 0,05 | 4,00 | Ohio · [L] · 26/11/26 |
| Alto | S3 Standard, GB-mês | 20 | 0,023 | 0,46 | Ohio · [S] · 26/11/26 |
| Alto | S3 PUT/LIST, mil chamadas | 0,4 | 0,005 | 0,002 | Ohio · [S] · 26/11/26 |
| Alto | S3 GET/HEAD, mil chamadas | 0,4 | 0,0004 | 0,00016 | Ohio · [S] · 26/11/26 |
| Alto | SSM Run Command, invocações | 144 | 0,002 | 0,288 | Ohio · [M] · 26/11/26 |

O plano HA já inclui o standby. A estimativa de tráfego total das VMs usa 20, 100 e 500 GB/mês, valores abaixo das franquias. Para o banco gerenciado, estima-se 40 GB/mês no regional e 80 GB no estadual ante uma franquia de 100 GB/mês. O orçamento exclui aquisição de domínio, hospedagem DNS, impostos, SMTP transacional e fornecedor de rotas.

| Cenário | Pedidos/mês | USD/mês | USD/pedido |
| --- | ---: | ---: | ---: |
| Baixo | 3.600 | 45,63 | 0,01268 |
| Moderado | 18.000 | 122,78 | 0,00682 |
| Alto | 72.000 | 330,75 | 0,00459 |

O custo por pedido divide o custo mensal pelo volume de pedidos hospitalares em 30 dias. Os planos Lightsail usam o teto de um mês completo. São estimativas de infraestrutura com dados sintéticos.

[Planilha editável](orcamento.xlsx) · [Franquias de transferência](https://docs.aws.amazon.com/lightsail/latest/userguide/amazon-lightsail-faq-data-transfer-allowance.html)

<!-- página -->

## Uso de IA

Ferramenta usada: OpenAI Codex (ChatGPT). Finalidade: comparar o enunciado com o checkout, redigir a proposta e os ADRs, diagramar os fluxos e preparar a planilha, o PDF e os slides. Os exemplos e as premissas usam dados sintéticos.

| Parte | Validação realizada |
| --- | --- |
| A · DDD | As responsabilidades e os termos foram confrontados com os fluxos e entidades já documentados. A classificação dos subdomínios e o mapa são decisões propostas. |
| B · Modelo tático | A história e o cenário foram cruzados com a POOUS06, AC-030, AC-031, AC-033 e os testes de aceite existentes. O cenário Gherkin fica para execução na Parte 3. |
| C · Camadas | O desenho foi comparado com os pacotes Java, as verificações ArchUnit e a configuração atual do frontend. A árvore é uma proposta para a Parte 3. |
| D · Microsserviços | A saga foi revisada contra o fluxo atual de reserva, rota, transporte e cancelamento. As mensagens e compensações descrevem o comportamento proposto. |
| E · Orçamento | As fórmulas foram conferidas com cálculo independente. Tarifas, franquias, standby e região foram conferidos nas fontes oficiais AWS. O deploy foi cruzado com o código da main, o histórico do GitHub Actions e consultas públicas HTTPS em 26/11/2026. |

### Entrega e conferência

- [PDF para o Classroom](hemovia-u2-parte-2.pdf)
- [Planilha de dimensionamento e orçamento](orcamento.xlsx)
- [Deploy AWS](diagramas/deploy-atual.md), [mapa de contextos](diagramas/contextos.md), [saga](diagramas/saga-atendimento.md) e os diagramas [baixo](diagramas/cenario-baixo.md), [moderado](diagramas/cenario-moderado.md) e [alto](diagramas/cenario-alto.md)
- [ADRs](adr/) e [cenário Gherkin](aceite/alocacao.feature)

A apresentação segue quatro blocos em 10 minutos. Parte 1 em 1 minuto, domínio e mapa em 3, arquitetura e ADRs em 3, cenários e orçamento em 3. Depois, 5 minutos para perguntas.

### Referências do trabalho

Evans, E. *Domain-Driven Design* (2003).  
Vernon, V. *Implementing Domain-Driven Design* (2013).  
Newman, S. *Building Microservices*, 2ª ed. (2021).  
Nygard, M. *Documenting Architecture Decisions* (2011).
