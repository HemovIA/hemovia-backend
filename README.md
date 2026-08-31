<div align="center">

# 🩸 HemovIA: Sistema Inteligente de Gestão e Roteirização da Hemorrede
### Projeto Integrador — Rota Vital | CESAR School (2026.2 — 3º Semestre ADS)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](#)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk)](#)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=springboot)](#)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-blue?logo=postgresql)](#)
[![CESAR School](https://img.shields.io/badge/CESAR_School-3º_Semestre_ADS-blue)](#)

<br>

[![Figma](https://img.shields.io/badge/Figma-Design_System-%23F24E1E?style=for-the-badge&logo=figma&logoColor=white)](#)
[![Trello](https://img.shields.io/badge/Trello-Quadro_do_Projeto-%23026AA7?style=for-the-badge&logo=trello&logoColor=white)](#)
[![Diagramas UML](https://img.shields.io/badge/Diagramas-Galeria_UML-ff69b4?style=for-the-badge&logo=mermaid)](#diagramas-uml-das-funcionalidades)

</div>

---

O **HemovIA (Rota Vital)** é uma plataforma integrada desenvolvida para otimizar e automatizar os processos críticos da cadeia de suprimentos de sangue (Hemorrede). O sistema conecta centros de coleta, hemocentros processadores, frotas de transporte em tempo real e unidades hospitalares requisitantes.

A proposta do projeto é eliminar gargalos logísticos, prevenir descartes de hemocomponentes por vencimento através do método FEFO (*First Expired, First Out*), garantir compatibilidade biológica ABO/Rh com resposta instantânea $O(1)$, calcular rotas ótimas por Grafos (Dijkstra) respeitando a janela térmica da cadeia fria, e monitorar a telemetria dos veículos com alta concorrência e resiliência.

---

<div align="center">
  <h2>Visão do Padrão de Backlogs (3Cs & BDD)</h2>
</div>

As funcionalidades do projeto foram definidas com base em **histórias de usuário estruturadas no padrão 3Cs (Card, Conversation, Confirmation)**, garantindo rastreabilidade, clareza de requisitos e validação objetiva das entregas.

<div align="center">

| **Elemento** | **Descrição** |
|:---|:---|
| **Card (Cartão)** | Define a persona, intenção e valor da entrega (*Como... Quero... Para...*). |
| **Conversation (Conversa)** | Detalha as regras de negócio, LGPD, integrações técnicas e restrições de arquitetura. |
| **Confirmation (Confirmação)** | Critérios de aceite em BDD (*Dado / Quando / Então*) testando cenários positivos e negativos. |

</div>

---

<div align="center">
  <h2>Features</h2>
</div>

---

### 1. Programação Orientada a Objetos (POO)

#### POOUS01 — Cadastro de Doação e Entrada de Bolsa no Estoque

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como operador do hemocentro, quero registrar uma doação de sangue e a entrada da bolsa correspondente no estoque para que o sistema mantenha rastreabilidade entre a doação recebida e o hemocomponente disponível para uso. |
| **Conversation** | • Registro sintético da doação (ID, data de coleta, centro de coleta) em conformidade com a LGPD <br> • Cada doação origina uma ou mais bolsas com status inicial AVAILABLE <br> • Validação estrita impedindo campos obrigatórios vazios |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Doação válida no "Centro Recife" gera Doação com UUID único e insere bolsa AVAILABLE no estoque <br> • [ ] **Cenário 2 (Negativo):** Doação com centro de coleta vazio é rejeitada com HTTP 400 Bad Request |

#### POOUS02 — Cadastro e Controle de Validade das Bolsas de Sangue

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como gestor do hemocentro, quero cadastrar novas bolsas de sangue e consultar o estoque disponível para garantir o controle rigoroso da validade dos hemocomponentes. |
| **Conversation** | • Registro de tipo (A, B, AB, O), Rh (+/-), componente (Hemácias, Plasma, Plaquetas) e validade <br> • Sistema impede cadastro de bolsas com validade expirada |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Bolsa válida com vencimento em 35 dias é salva com status AVAILABLE <br> • [ ] **Cenário 2 (Negativo):** Bolsa com data vencida dispara BloodBagExpiredException e aborta |

#### POOUS03 — Gestão de Cadastro de Hospitais

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como operador de distribuição, quero cadastrar e gerenciar unidades hospitalares na rede para que o sistema saiba de onde partem as solicitações e suas localizações. |
| **Conversation** | • Cadastro com ID, nome, cidade, coordenadas (latitude/longitude) e prioridade <br> • Validação estrita de formato e persistência no PostgreSQL via JPA |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Cadastro com dados válidos atribui UUID único e retorna HTTP 201 Created <br> • [ ] **Cenário 2 (Negativo):** Latitude/longitude fora da faixa (-90..90 / -180..180) retorna HTTP 400 Bad Request |

#### POOUS04 — Emissão de Requisições Hospitalares

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como profissional do hospital, quero criar solicitações de hemocomponentes informando tipo, quantidade e prioridade para suprir demandas médicas urgentes. |
| **Conversation** | • Requisição registra hospital, tipo sanguíneo, Rh, componente, quantidade e janela de entrega <br> • Status inicial obrigatoriamente PENDING |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Solicitação de 4 bolsas com prazo de 6h salva com status PENDING e retorna ID <br> • [ ] **Cenário 2 (Negativo):** Quantidade nula ou negativa (<= 0) retorna HTTP 400 Bad Request |

#### POOUS05 — Tela Inicial da Aplicação (Visão Geral do Sistema)

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como usuário do sistema, quero acessar uma tela inicial ao entrar na aplicação para ter uma visão geral rápida do estado da rede antes de navegar para funções específicas. |
| **Conversation** | • Resumo leve com total de bolsas, requisições pendentes e status da aplicação <br> • Tratamento de indisponibilidade de banco sem quebrar a interface |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Sistema operacional exibe resumo numérico atualizado e status online <br> • [ ] **Cenário 2 (Negativo):** Falha de conexão com o banco exibe mensagem amigável sem erro não tratado |

#### POOUS06 — Processamento e Matching de Requisições (Compatibilidade + FEFO)

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como sistema Hemovia, quero orquestrar a alocação de bolsas para uma requisição utilizando compatibilidade ABO/Rh e regra FEFO para evitar descartes e garantir atendimento clínico. |
| **Conversation** | • Integra regras de POO à matriz ABO/Rh de AED <br> • Ordena bolsas compatíveis por menor validade e altera status para RESERVED e requisição para ALLOCATED |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Alocação seleciona bolsas mais próximas do vencimento e reserva com sucesso <br> • [ ] **Cenário 2 (Negativo):** Estoque insuficiente lança InsufficientInventoryException e mantém saldo |

#### POOUS07 — Integração com Módulo de Roteirização e Exibição de Rotas

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como despachante de distribuição, quero visualizar a rota calculada entre o hemocentro e o hospital para acompanhar o tempo estimado e os nós do percurso. |
| **Conversation** | • Chama serviço de roteirização Dijkstra de AED <br> • Acopla ID da rota ao objeto Allocation e Transport |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Rota calculada com vértices e tempo estimado é anexada à alocação <br> • [ ] **Cenário 2 (Negativo):** Hospital isolado dispara InvalidRouteException e reverte reserva de bolsas |

#### POOUS08 — Integração dos Painéis de Monitoramento e Estatística

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como gestor da Hemorrede, quero consultar os endpoints de painéis para acompanhar percentual de estoque, solicitações e dados da cadeia fria. |
| **Conversation** | • Agrega dados reais dos módulos de Estatística (EST) e Redes (RSD) <br> • Sem mocks estáticos, refletindo banco real |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** GET /dashboard/summary retorna 200 OK com métricas consolidadas <br> • [ ] **Cenário 2 (Negativo):** Banco recém-instanciado retorna JSON zerado sem NullPointerException |

#### POOUS09 — Início de Transporte e Monitoramento de Cadeia Fria

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como despachante de distribuição, quero transicionar uma alocação para transporte para que o sistema inicie o rastreamento e telemetria de temperatura em tempo real. |
| **Conversation** | • Veículo sai do hemocentro: status das bolsas e da requisição vira IN_TRANSIT <br> • Cria entidade Transport vinculando rota, veículo e lote |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Requisição ALLOCATED inicia transporte, cria Transport e retorna HTTP 201 Created <br> • [ ] **Cenário 2 (Negativo):** Iniciar transporte de requisição PENDING/CANCELLED lança IllegalStateException |

#### POOUS10 — Cancelamento de Requisição com Liberação Automática de Estoque

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como hospital solicitante, quero cancelar uma requisição pendente ou alocada para que as bolsas reservadas voltem imediatamente a ficar disponíveis no estoque. |
| **Conversation** | • Cancelamento atômico: bolsas RESERVED retornam para AVAILABLE <br> • Proibido cancelar transportes já em trânsito |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Requisição ALLOCATED cancelada vira CANCELLED e bolsas voltam a AVAILABLE <br> • [ ] **Cenário 2 (Negativo):** Cancelar requisição IN_TRANSIT retorna HTTP 422 Unprocessable Entity |

#### POOUS11 — Confirmação do Recebimento e Baixa de Entrega

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como recebedor do hospital, quero confirmar a entrega das bolsas e o encerramento do transporte para efetivar a baixa final da solicitação. |
| **Conversation** | • Encerramento do ciclo: status do transporte, requisição e bolsas vira DELIVERED <br> • Gravação de timestamp exato para auditoria e métricas |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Confirmação de transporte IN_TRANSIT atualiza tudo para DELIVERED com timestamp <br> • [ ] **Cenário 2 (Negativo):** Tentar concluir transporte não iniciado retorna erro de regra de negócio |

#### POOUS12 — Bloqueio de Concorrência na Reserva Simultânea de Bolsas

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como arquiteto do sistema, quero garantir o isolamento e o bloqueio transacional no estoque para que duas requisições simultâneas não reservem a mesma bolsa. |
| **Conversation** | • Aplicação de Lock Pessimista (@Lock(LockModeType.PESSIMISTIC_WRITE)) na consulta de estoque <br> • Transações atômicas com rollback automático |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Req A obtém lock e reserva; Req B falha graciosamente com InsufficientInventoryException <br> • [ ] **Cenário 2 (Negativo):** Falha no meio da transação aciona rollback automático sem travar bolsas |

---

### 2. Algoritmos e Estruturas de Dados (AED)

#### AEDUS01 — Fila de Processamento de Requisições Hospitalares

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como sistema de distribuição, quero gerenciar as solicitações dos hospitais em uma estrutura de Fila (FIFO) para garantir atendimento na ordem exata de chegada. |
| **Conversation** | • Fila encadeada/customizada com nós em memória Heap <br> • Primeiro a entrar (enqueue) é o primeiro a ser retirado (dequeue) |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Enfileiramento de Req#101 e Req#102 retorna Req#101 no primeiro dequeue mantendo Req#102 <br> • [ ] **Cenário 2 (Negativo):** Dequeue em fila vazia dispara EmptyQueueException sem quebrar ponteiros |

#### AEDUS02 — Estrutura de Lista para Armazenamento Geral de Bolsas no Estoque

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como módulo de estoque, quero armazenar e manipular a coleção de bolsas em Lista Encadeada para permitir inserções, buscas dinâmicas e remoções na U1. |
| **Conversation** | • Implementação de lista simplesmente encadeada <br> • Inserção dinâmica e busca sequencial O(n) por ID |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Inserção da bolsa BAG-999 permite busca sequencial com retorno em O(n) <br> • [ ] **Cenário 2 (Negativo):** Remoção de bolsa inexistente (BAG-000) retorna false sem corromper ponteiros |

#### AEDUS03 — Pilha de Histórico e Reversão de Operações de Estoque (Undo Log)

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como operador do sistema, quero armazenar as movimentações em Pilha (LIFO) para permitir consulta aos últimos eventos e possibilitar estorno imediato (Undo). |
| **Conversation** | • Toda transição de estado empilha evento no topo (push) <br> • Comando de estorno desempilha (pop) e restaura o status anterior da bolsa |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Bolsa transicionada para IN_TRANSIT é revertida para RESERVED com pop do evento <br> • [ ] **Cenário 2 (Negativo):** Estorno com pilha de eventos vazia dispara EmptyStackException |

#### AEDUS04 — Roteirização e Caminho Mínimo por Grafo (Dijkstra)

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como módulo logístico, quero calcular a menor rota entre o hemocentro e os hospitais utilizando Grafos e caminho mínimo para respeitar janelas da cadeia fria. |
| **Conversation** | • Hospitais/hemocentros representam vértices; vias são arestas ponderadas por tempo em minutos <br> • Dijkstra calcula o menor caminho acumulado |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Grafo calcula sequência de vértices que minimiza tempo total de deslocamento <br> • [ ] **Cenário 2 (Negativo):** Caminho mínimo que excede tempo limite térmico (ex: 85m > 60m) é rejeitado |

#### AEDUS05 — Priorização de Validade por FEFO (Heap / Fila de Prioridade)

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como gerenciador de estoque, quero organizar as bolsas compatíveis por FEFO utilizando Heap/Fila de Prioridade para alocar primeiro as bolsas mais próximas do vencimento. |
| **Conversation** | • Recebe coleção de bolsas e ordena por expirationDate ASC <br> • Menor validade fica na cabeça do conjunto |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Bolsas com vencimento para 3, 15 e 1 dias alocam primeiro a bolsa de 1 dia <br> • [ ] **Cenário 2 (Negativo):** Bolsas já vencidas (<= dataAtual) são descartadas automaticamente do fluxo |

#### AEDUS06 — Indexação de Estoque por Tabela Hash

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como motor de busca, quero indexar o estoque de bolsas utilizando uma Tabela Hash pela chave combinada (Tipo_Rh_Componente) para consultas em tempo constante O(1). |
| **Conversation** | • Tabela Hash personalizada onde a chave é a combinação e o valor é a sublista <br> • Resolução de colisão por encadeamento (separate chaining) |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Busca por RED_BLOOD_CELLS_O_POSITIVE em estoque de 5.000 bolsas retorna em O(1) <br> • [ ] **Cenário 2 (Negativo):** Duas chaves com mesmo hashcode são tratadas por encadeamento mantendo integridade |

#### AEDUS07 — Matriz de Compatibilidade Sanguínea ABO/Rh

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como motor de matching, quero validar a compatibilidade biológica entre a bolsa solicitada e as bolsas disponíveis através de uma Matriz de Compatibilidade ABO/Rh em O(1). |
| **Conversation** | • Matriz bidimensional estática em Java cruzando receptor (linha) com doador (coluna) <br> • Retorna valor booleano instantâneo |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Requisição A+ valida doadores A+, A-, O+, O- com retorno true para todos <br> • [ ] **Cenário 2 (Negativo):** Requisição O- com doadores A+, B-, AB- retorna false e impede alocação |

#### AEDUS08 — Ordenação por MergeSort/QuickSort para Relatório de Validades

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como auditor do hemocentro, quero ordenar todo o lote de bolsas usando divisão e conquista (MergeSort) para gerar relatórios consolidados em tempo O(n log n). |
| **Conversation** | • Reimplementado em Java puro sem depender de Collections.sort() <br> • Preservação da ordem relativa de elementos com chaves iguais (estabilidade) |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Vetor de 1.000 bolsas sintéticas é ordenado da menor para maior validade em O(n log n) <br> • [ ] **Cenário 2 (Negativo):** Lote de 50 bolsas com datas idênticas preserva a ordem relativa original |

#### AEDUS09 — Busca Binária em Vetor Estático de Hospitais Indexados

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como componente logístico, quero pesquisar hospitais em tabela estática ordenada por ID utilizando Busca Binária para reduzir o tempo de consulta a O(log n). |
| **Conversation** | • Nós dos hospitais mantidos em array fixo pré-ordenado por ID numérico <br> • Reduz drasticamente consumo de CPU em comparação com varredura linear |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Busca em 500 hospitais localiza ID 342 em no máximo ~9 comparações (log2 500) <br> • [ ] **Cenário 2 (Negativo):** Busca por ID inexistente (999) encerra intervalo (início > fim) e retorna -1 |

#### AEDUS10 — Buffer Circular em Memória para Leitura de Telemetria

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como módulo de ingestão, quero utilizar um Buffer Circular (Fila Circular estática) para armazenar telemetria dos veículos e evitar alocações excessivas de memória e GC. |
| **Conversation** | • Fila circular de tamanho fixo em memória (head/tail) <br> • Ponteiros realizam wrap-around sem novas alocações |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Gravação e leitura contínua executam wrap-around sem IndexOutOfBoundsException <br> • [ ] **Cenário 2 (Negativo):** Buffer 100% cheio bloqueia ou dispara BufferOverflowException preservando pendentes |

---

### 3. Estatística e Probabilidade (EST)

#### ESTUS01 — Análise Descritiva do Estoque por Tipo Sanguíneo e Componente

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como gestor do hemocentro, quero visualizar o quantitativo do estoque categorizado por tipo e componente para avaliar o nível de suprimento da rede. |
| **Conversation** | • Calcula contagem total, proporção relativa (%) e sintetiza dados operacionais de POO <br> • Previne divisões por zero |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Base com 200 bolsas retorna distribuição percentual exata (ex: 40% O+, 10% AB-) <br> • [ ] **Cenário 2 (Negativo):** Estoque zerado retorna contagens e percentuais 0.0% sem ArithmeticException |

#### ESTUS02 — Métricas de Tendência Central e Dispersão para Demanda

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como analista de operações, quero calcular a média, mediana, desvio padrão e variância das requisições por hospital para identificar discrepâncias de consumo. |
| **Conversation** | • Mede tendência central e dispersão dos volumes solicitados <br> • Trata amostras unitárias evitando erro de divisão por n-1 |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Solicitações dos hospitais A (10), B (20) e C (30) geram Média=20, Mediana=20 e DP≈8.16 <br> • [ ] **Cenário 2 (Negativo):** Amostra de 1 requisição trata n-1=0, define DP=0 e emite aviso |

#### ESTUS03 — Análise de Tempos de Atendimento e Entrega Logística

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como coordenador de logística, quero analisar os tempos de atendimento das requisições (da emissão à entrega) para mensurar o desempenho das rotas operacionais. |
| **Conversation** | • Integra dados de POO/AED calculando tempo mínimo, máximo, médio e desvio padrão <br> • Ignora registros com datas incompletas/nulas |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** 5 entregas (20, 30, 25, 40, 35 min) exibem Mín=20m, Máx=40m, Média=30m e DP <br> • [ ] **Cenário 2 (Negativo):** Registros com timestamp de término nulo são ignorados preservando a média |

#### ESTUS04 — Análise Probabilística de Desabastecimento por Tipo Sanguíneo

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como gestor da Hemorrede, quero calcular a probabilidade de desabastecimento por tipo para antecipar falta de sangue e priorizar campanhas de doação. |
| **Conversation** | • Modela taxa de demanda média vs estoque atual via Distribuição de Poisson: P(Demanda > Estoque) <br> • Sinaliza risco percentual de ruptura |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Estoque de 2 bolsas O- e demanda de 5/dia calcula risco crítico (>80%) com alerta <br> • [ ] **Cenário 2 (Negativo):** Estoque com 500 bolsas AB+ e demanda de 1/sem retorna probabilidade ≈ 0% |

#### ESTUS05 — Cálculo da Taxa de Descarte por Vencimento de Validade

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como auditor de qualidade, quero mensurar a taxa de descarte de hemocomponentes por vencimento para monitorar perdas e avaliar a eficiência do FEFO. |
| **Conversation** | • Indicador calcula razão entre bolsas vencidas/descartadas e total de entradas: (Vencidas / Entradas) * 100 |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Registro de 200 bolsas processadas e 10 vencidas indica taxa de 5.0% <br> • [ ] **Cenário 2 (Negativo):** Período com 100% de uso dentro da validade exibe taxa de 0.0% sem erro |

#### ESTUS06 — Previsão Simples de Demanda por Média Móvel / Regressão

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como analista de planejamento, quero visualizar uma estimativa de demanda futura baseada no histórico de consumo para projetar a necessidade de captação. |
| **Conversation** | • Modelos simples de projeção (Média Móvel ou Regressão Linear Simples) para os próximos dias <br> • Sem bibliotecas externas complexas |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Consumo dos últimos 3 dias (10, 12, 14) projeta demanda estimada de 12 bolsas para o dia seguinte <br> • [ ] **Cenário 2 (Negativo):** Menos de 2 dias de histórico emite "Dados insuficientes para projeção" |

#### ESTUS07 — Painel Consolidado de Indicadores e Integração REST

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como tomador de decisão, quero acessar um endpoint consolidado que reúna todos os indicadores para visualizar o status de saúde da rede em uma única tela. |
| **Conversation** | • Endpoint GET /api/v1/dashboard/summary unificando tendência, dispersão, descarte e risco <br> • Tratamento de indisponibilidade com cache |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** GET /dashboard/summary retorna 200 OK com JSON consolidado estruturado <br> • [ ] **Cenário 2 (Negativo):** Falha temporária no banco utiliza cache ou retorna HTTP 503 Service Unavailable |

---

### 4. Infraestrutura de Software (SO)

#### SOUS01 — Esteira de Integração e Entrega Contínua (Pipeline CI/CD)

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como engenheiro de DevOps, quero configurar um pipeline automatizado no GitHub Actions para compilar, testar e empacotar a aplicação Spring Boot a cada push. |
| **Conversation** | • Pipeline disparado em main e develop executando compilação Java e testes unitários/integração <br> • Geração do artefato JAR sem falhas |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Commits válidos passam por build e testes gerando o JAR com sucesso <br> • [ ] **Cenário 2 (Negativo):** Teste quebrado interrompe a esteira antes do deploy e notifica no repositório |

#### SOUS02 — Deploy Contínuo e Publicação da Aplicação em Nuvem

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como arquiteto de infraestrutura, quero realizar o deploy da aplicação em provedor PaaS/Cloud integrado ao pipeline para manter o sistema acessível externamente. |
| **Conversation** | • Deploy automatizado do Spring Boot e banco PostgreSQL em nuvem <br> • Validação através da rota de saúde /actuator/health |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Deploy automático sobe com sucesso respondendo HTTP 200 OK na rota de saúde <br> • [ ] **Cenário 2 (Negativo):** URL de banco incorreta aborta implantação e preserva versão anterior ativa |

#### SOUS03 — Processamento Concorrente Multi-Thread para Telemetria

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como desenvolvedor de software, quero implementar recebimento de telemetria usando Múltiplas Threads (@Async/ExecutorService) para processar picos sem travar a API. |
| **Conversation** | • Delegação para pool de threads assíncronas para dados simulados de GPS e temperatura <br> • Evita gargalos na thread principal do Tomcat |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Rajada de 100 pacotes simultâneos é processada em paralelo retornando HTTP 202 <br> • [ ] **Cenário 2 (Negativo):** Esgotamento do Thread Pool aplica CallerRunsPolicy sem travar o servidor |

#### SOUS04 — Sincronização e Prevenção de Condições de Corrida

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como arquiteto de software, quero implementar sincronização atômica (ReentrantLock/synchronized) na atualização de estoque para garantir consistência sob alta concorrência. |
| **Conversation** | • Múltiplas threads alterando a mesma bolsa no estoque são serializadas na seção crítica <br> • Apenas uma thread por vez transiciona o estado da bolsa |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** 2 threads disputam bolsa: Thread 1 reserva; Thread 2 vê novo status e recusa <br> • [ ] **Cenário 2 (Negativo):** Teste de estresse com 50 threads reserva bolsa exatamente 1 vez sem inconsistência |

#### SOUS05 — Dimensionamento e Modelagem para Três Cenários de Carga

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como engenheiro de infraestrutura, quero projetar diagramas de arquitetura e capacidade para 3 cenários operacionais (Baixo, Moderado e Alto) detalhando recursos computacionais. |
| **Conversation** | • Especifica CPU, RAM, contêineres, banco e Load Balancer para: Cenário 1 (Baixo/local), Cenário 2 (Moderado/regional) e Cenário 3 (Alto/estadual sob emergência) |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Cenário de alto uso (10k req/min) define auto-scaling e réplicas sem SPOF <br> • [ ] **Cenário 2 (Negativo):** Análise de baixo uso rejeita over-engineering e dimensiona recursos mínimos |

#### SOUS06 — Estimativa e Planilha de Orçamento da Infraestrutura em Nuvem

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como gestor de infraestrutura, quero elaborar orçamento detalhado de custos mensais em dólares/reais para os 3 cenários simulados em nuvem (AWS/Azure/GCP). |
| **Conversation** | • Quantifica custo de instâncias (vCPU/RAM), banco gerenciado, tráfego e armazenamento <br> • Justifica escolhas de custo-benefício financeiro |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Estimativa discriminada por componente mensal e anual calculada de forma realista <br> • [ ] **Cenário 2 (Negativo):** Identifica alto custo On-Demand e sugere instâncias reservadas reduzindo em até 40% |

#### SOUS07 — Rollback Automatizado no Pipeline de Deploy

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como responsável pela operação, quero garantir que a esteira de CI/CD reverta automaticamente a implantação em nuvem caso a nova versão apresente falhas pós-deploy. |
| **Conversation** | • Se o contêiner recém-implantado falhar no Health Check, o pipeline restaura a versão estável anterior <br> • Resiliência de produção sem intervenção manual |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Falha consecutiva no Health Check aciona reversão de tráfego e notifica incidente <br> • [ ] **Cenário 2 (Negativo):** Ambiente estabilizado na versão estável atende normalmente sem indisponibilidade |

---

### 5. Infraestrutura de Comunicação / Redes (RSD)

#### RSDUS01 — Projeto da Topologia de Rede da Hemorrede

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como engenheiro de redes, quero projetar a topologia conectando centros de coleta, hemocentro, hospitais e veículos para garantir interligação segura. |
| **Conversation** | • Mapeia arquitetura nas camadas OSI/TCP-IP com canais VPN/HTTPS fixos e links móveis (4G/5G/Broker) <br> • Elimina pontos únicos de falha |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Mapeamento completo define endpoints, protocolos de transporte e links de comunicação <br> • [ ] **Cenário 2 (Negativo):** Topologia sem link de redundância no canal central é reprovada exigindo failover |

#### RSDUS02 — Mapeamento de Requisitos de Negócio em Requisitos Técnicos

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como engenheiro de redes, quero traduzir as necessidades operacionais da rede de sangue em requisitos técnicos de comunicação para atender ao negócio. |
| **Conversation** | • Mapeia telemetria contínua, disponibilidade em emergências e comunicação segura em métricas técnicas de latência, vazão e TLS <br> • Base para topologia e protocolos |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Necessidade de telemetria contínua especifica frequência de envio e limite de latência <br> • [ ] **Cenário 2 (Negativo):** Área rural sem cobertura definida é sinalizada como pendente bloqueando avanço |

#### RSDUS03 — Especificação dos Protocolos e Contratos de Telemetria

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como arquiteto de redes, quero definir protocolos de comunicação (REST/HTTPS ou MQTT) e schemas de payloads JSON para a transmissão da telemetria da cadeia fria. |
| **Conversation** | • Define contrato entre backend Spring Boot e emuladores de veículos <br> • Payload especifica ID, latitude, longitude, temperatura em °C e timestamp |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Veículo em trânsito enviando JSON formatado é decodificado e responde HTTP 202 Accepted <br> • [ ] **Cenário 2 (Negativo):** Transmissão com campos ausentes ou tipos incompatíveis retorna HTTP 400 Bad Request |

#### RSDUS04 — Benchmarking de Métricas de Rede (Latência, Vazão e Perda)

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como especialista de redes, quero realizar testes de estresse e benchmarking para medir latência, vazão (throughput), jitter e perda de pacotes durante ingestão de dados. |
| **Conversation** | • Teste de carga (k6/JMeter) simulando requisições simultâneas contínuas <br> • Quantifica tempos de RTT (Round Trip Time) e capacidade máxima do canal |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Simulação de 50 req/s consolida latência média (<150ms) e perda de pacotes ≈ 0% <br> • [ ] **Cenário 2 (Negativo):** Disparo massivo excedendo banda registra elevação de latência e identifica gargalo |

#### RSDUS05 — Monitoramento das Taxas de Erro HTTP (4xx e 5xx)

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como analista de suporte de rede, quero coletar e categorizar falhas da API (erros 4xx e 5xx) para identificar inconsistências de clientes ou instabilidades de servidor. |
| **Conversation** | • Gateway intercepta chamadas classificando erros 4xx (contrato/cliente) e 5xx (servidor/banco) <br> • Disparo de alertas automáticos em picos anômalos |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Registro de 10 chamadas 404 e 2 falhas 500 é contabilizado separadamente no log <br> • [ ] **Cenário 2 (Negativo):** Erros HTTP 503 consecutivos em mais de 5% das requisições em 1 min disparam alerta crítico |

#### RSDUS06 — Painel de Monitoramento da Qualidade de Rede e Telemetria

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como administrador de redes, quero visualizar um painel gráfico em tempo real com métricas de latência, status das conexões e dados da cadeia fria para acompanhar a saúde da rede. |
| **Conversation** | • Painel consome indicadores de rede tratados exibindo gráficos dinâmicos de vazão e latência <br> • Detecção visual de perda de sinal com timeout |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Serviços transmitindo renderizam em tempo real gráficos e status ONLINE dos nós <br> • [ ] **Cenário 2 (Negativo):** Veículo que deixa de enviar dados além do limite passa para status OFFLINE/TIMEOUT com alerta |

#### RSDUS07 — Plano de Migração e Implantação do Ambiente de Rede

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como gerente de infraestrutura de redes, quero elaborar plano detalhado de migração e implantação documentando as etapas de transição do ambiente local para produção em nuvem. |
| **Conversation** | • Mapeia sequência de DNS, portas de firewall, certificados SSL/TLS, variáveis e contingência <br> • Rejeição obrigatória de protocolos sem criptografia |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Checklist cobrindo roteamento, portas, HTTPS/TLS e rollback é aprovado pela equipe <br> • [ ] **Cenário 2 (Negativo):** Plano prevendo telemetria via HTTP puro em porta aberta é bloqueado exigindo TLS |

#### RSDUS08 — Implantação do Ambiente de Comunicação em Produção

| **Elemento** | **Descrição** |
|:---|:---|
| **Card** | Como engenheiro de infraestrutura de redes, quero implantar de fato o ambiente de comunicação (portas, certificados, DNS, telemetria) em produção para tornar a rede operacional. |
| **Conversation** | • Configuração real de portas de firewall, certificados SSL/TLS ativos, DNS resolvido e validação de recepção de telemetria |
| **Confirmation** | • [ ] **Cenário 1 (Positivo):** Ambiente entra em produção com HTTPS ativo, DNS resolvendo e telemetria recebendo dados <br> • [ ] **Cenário 2 (Negativo):** Certificado SSL/TLS inválido ou ausente bloqueia a subida do sistema notificando erro |

---

<div align="center">
  <h2>Diagramas UML das Funcionalidades</h2>
  <p><i>Clique no título da funcionalidade para acessar o diagrama correspondente completo</i></p>
</div>

### ☕ Programação Orientada a Objetos (POO)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**POOUS01. Cadastro de Doação**](diagramas/README.md#poous01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous01) | [**POOUS02. Controle de Validade**](diagramas/README.md#poous02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous02) | [**POOUS03. Cadastro de Hospitais**](diagramas/README.md#poous03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous03) |
| [**POOUS04. Emissão de Requisições**](diagramas/README.md#poous04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous04) | [**POOUS05. Tela Inicial / Visão Geral**](diagramas/README.md#poous05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous05) | [**POOUS06. Matching ABO/Rh + FEFO**](diagramas/README.md#poous06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous06) |
| [**POOUS07. Roteirização Integrada**](diagramas/README.md#poous07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous07) | [**POOUS08. Painel de Monitoramento**](diagramas/README.md#poous08) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous08) | [**POOUS09. Início do Transporte**](diagramas/README.md#poous09) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous09) |
| [**POOUS10. Cancelamento de Requisição**](diagramas/README.md#poous10) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous10) | [**POOUS11. Baixa Final de Entrega**](diagramas/README.md#poous11) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous11) | [**POOUS12. Bloqueio de Concorrência**](diagramas/README.md#poous12) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous12) |

---

### ⚡ Algoritmos e Estruturas de Dados (AED)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**AEDUS01. Fila FIFO de Requisições**](diagramas/README.md#aedus01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus01) | [**AEDUS02. Lista Encadeada Estoque**](diagramas/README.md#aedus02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus02) | [**AEDUS03. Pilha Undo Log**](diagramas/README.md#aedus03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus03) |
| [**AEDUS04. Grafo & Dijkstra**](diagramas/README.md#aedus04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus04) | [**AEDUS05. Min-Heap / FEFO**](diagramas/README.md#aedus05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus05) | [**AEDUS06. Indexação Tabela Hash**](diagramas/README.md#aedus06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus06) |
| [**AEDUS07. Matriz ABO/Rh**](diagramas/README.md#aedus07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus07) | [**AEDUS08. MergeSort Estável**](diagramas/README.md#aedus08) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus08) | [**AEDUS09. Busca Binária**](diagramas/README.md#aedus09) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus09) |
| [**AEDUS10. Buffer Circular**](diagramas/README.md#aedus10) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus10) | | | | |

---

### 📊 Estatística e Probabilidade (EST)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**ESTUS01. Análise Descritiva**](diagramas/README.md#estus01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus01) | [**ESTUS02. Tendência e Dispersão**](diagramas/README.md#estus02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus02) | [**ESTUS03. Tempos de Entrega**](diagramas/README.md#estus03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus03) |
| [**ESTUS04. Risco Desabastecimento**](diagramas/README.md#estus04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus04) | [**ESTUS05. Taxa de Descarte**](diagramas/README.md#estus05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus05) | [**ESTUS06. Previsão de Demanda**](diagramas/README.md#estus06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus06) |
| [**ESTUS07. Dashboard REST**](diagramas/README.md#estus07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus07) | | | | |

---

### ⚙️ Infraestrutura de Software (SO)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**SOUS01. Pipeline CI/CD**](diagramas/README.md#sous01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous01) | [**SOUS02. Deploy Contínuo Cloud**](diagramas/README.md#sous02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous02) | [**SOUS03. Ingestão Multi-Thread**](diagramas/README.md#sous03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous03) |
| [**SOUS04. Trava Anti-Race Condition**](diagramas/README.md#sous04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous04) | [**SOUS05. Três Cenários de Carga**](diagramas/README.md#sous05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous05) | [**SOUS06. Orçamento em Nuvem**](diagramas/README.md#sous06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous06) |
| [**SOUS07. Rollback Automatizado**](diagramas/README.md#sous07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous07) | | | | |

---

### 🌐 Infraestrutura de Comunicação / Redes (RSD)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**RSDUS01. Topologia da Hemorrede**](diagramas/README.md#rsdus01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus01) | [**RSDUS02. Mapeamento Técnico**](diagramas/README.md#rsdus02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus02) | [**RSDUS03. Contratos de Telemetria**](diagramas/README.md#rsdus03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus03) |
| [**RSDUS04. Benchmarking de Rede**](diagramas/README.md#rsdus04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus04) | [**RSDUS05. Monitoramento Erros HTTP**](diagramas/README.md#rsdus05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus05) | [**RSDUS06. Painel Qualidade de Rede**](diagramas/README.md#rsdus06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus06) |
| [**RSDUS07. Plano Migração & TLS**](diagramas/README.md#rsdus07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus07) | [**RSDUS08. Implantação em Produção**](diagramas/README.md#rsdus08) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus08) | | |

---

## 👥 Equipe do Projeto

Projeto Integrador desenvolvido para o curso de Análise e Desenvolvimento de Sistemas — **CESAR School (2026.2 — 3º Semestre)**.

---

## 📄 Licença

Este projeto está sob a licença [MIT](https://opensource.org/licenses/MIT).
