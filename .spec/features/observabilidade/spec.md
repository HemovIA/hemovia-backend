# Observabilidade do Hemovia

> feature: observabilidade
> status: auditada

## Contexto

A atividade de Infraestrutura de Comunicação avalia a observabilidade da aplicação Java do Hemovia. A API Spring Boot é a única aplicação web Java encontrada. O cliente React e os materiais do Figma fazem parte do projeto, mas a coleta exigida se aplica à API e à infraestrutura do ambiente Docker.

## Histórias

### US-037 — Inspecionar o funcionamento da API

Como integrante da equipe, quero diagnosticar a saúde e o desempenho da API para demonstrar o comportamento do Hemovia ao professor.

#### AC-114 — Métricas da API identificadas no Actuator (E1)

- **Dado** que a API está conectada ao PostgreSQL e recebeu requisições
- **Quando** o coletor consulta a porta de gerenciamento
- **Então** health, info, metrics e prometheus respondem, e o scrape contém séries HTTP (incluindo histograma), heap, GC, threads, CPU do processo, uptime e conexões HikariCP com a tag application=hemovia-api

#### AC-115 — A API pública preserva o health check e restringe o Actuator (R5)

- **Dado** que a API e o gerenciamento estão em portas separadas
- **Quando** um cliente externo à rede de coleta consulta a porta da API
- **Então** nenhum endpoint de /actuator está acessível nessa porta, o probe /readyz responde apenas com o estado e a autenticação das rotas de negócio continua em vigor

#### AC-116 — Volume de operações de negócio visível (adicional)

- **Dado** que usuários autorizados registram doações e emitem requisições de sangue
- **Quando** as transações são confirmadas ou revertidas
- **Então** dois contadores Micrometer com nomes e descrições distintos registram apenas operações confirmadas e aparecem em painéis separados do dashboard da aplicação

### US-038 — Diagnosticar aplicação e infraestrutura no Grafana

Como avaliador, quero consultar métricas, logs e traces no Grafana para investigar uma execução do Hemovia.

#### AC-117 — Coleta de host e contêineres em execução (E2)

- **Dado** que a stack está rodando em ambiente Docker compatível com os exporters
- **Quando** o Prometheus coleta Node Exporter e cAdvisor
- **Então** os dois alvos ficam UP e existem séries de CPU, memória, disco, rede e carga do host e de CPU e memória por contêiner

#### AC-118 — Stack integrada, provisionada e isolada (E3, R5)

- **Dado** o arquivo de ambiente local com os valores necessários
- **Quando** a equipe sobe a stack com Docker Compose
- **Então** a API, o banco e toda a coleta sobem juntos, os alvos configurados ficam UP, o Grafana tem datasources de Prometheus, Loki e Tempo e exige configuração de credencial administrativa fora do Git

#### AC-119 — Dashboards exibem séries reais (E4)

- **Dado** que Prometheus coletou a stack e a carga atingiu a API
- **Quando** um usuário abre os dashboards provisionados
- **Então** há um dashboard de aplicação com tráfego, latência, erros, JVM e conexões e um dashboard de infraestrutura com séries do host e dos contêineres, ambos descritos em JSON versionado

#### AC-120 — Logs filtráveis por aplicação (E5)

- **Dado** que foram geradas requisições válidas e um erro controlado
- **Quando** o avaliador consulta os logs da aplicação no Loki
- **Então** encontra logs pelo nome hemovia-api, com traceId nas entradas geradas durante a requisição e sem payload, cookie ou identificadores de pessoas nas mensagens de demonstração

#### AC-121 — Navegação de log para trace com vários spans (E6)

- **Dado** que o script de carga passa por um fluxo da API com trabalho interno instrumentado
- **Quando** o avaliador abre no Grafana o traceId que aparece em um log
- **Então** o datasource Tempo devolve o mesmo trace, com span HTTP e pelo menos um span filho, e o teste de carga também produz uma resposta de erro

### US-039 — Avaliar a demonstração remota e a documentação

Como professor, quero acessar a demonstração com permissão de leitura e consultar a documentação e as evidências da equipe.

#### AC-122 — Conta de leitura e preparação para acesso remoto (E7)

- **Dado** que a demonstração local usa dados sintéticos
- **Quando** a equipe acessa o Grafana com a conta de avaliação
- **Então** o usuário Viewer consegue consultar os dashboards e os datasources sem permissão de salvar dashboards, e o Compose mantém os serviços de coleta fora das interfaces públicas

A validação HTTPS a partir de outra rede e a janela de correção serão tratadas quando o ambiente remoto for definido, conforme alinhamento com o usuário.

#### AC-123 — Repositório e relatório com provas datadas (E8)

- **Dado** que a demonstração foi executada
- **Quando** a equipe consulta o README e abre o PDF
- **Então** encontra instruções reproduzíveis, portas, as métricas de negócio, o link do repositório, a identificação da equipe, o diagrama, evidências datadas da stack, métricas, dashboards, logs e navegação para traces, além do acesso local, das informações ainda pendentes para publicar o acesso remoto e das lições aprendidas

## Limites da entrega

- O ambiente de avaliação usa dados sintéticos e serviços próprios em Docker Compose.
- A entrega no Classroom, o envio de credenciais ao professor e mudanças na visibilidade do GitHub dependem de autorização própria.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-015 | O escopo é a API Java e inclui dois contadores de negócio. | confirmada | Execução local do plano recomendado autorizada. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-023 | Qual execução, modelo e esforço usar nas tarefas? | respondida | Sequencial, GPT-6.1 Terra com esforço médio. |
| Q-024 | Qual host ou túnel, janela de correção e identificação da equipe devem constar na entrega? | respondida | Preparar localmente com a equipe identificada no README. A URL externa e a janela de correção serão definidas em etapa posterior. |
