# Spec: Revisão do projeto

> feature: revisao-projeto
> status: em-implementacao

## Contexto

Revisar todo o Hemovia para avaliar se a implementação segue os objetivos documentados e apontar inconsistências com evidências. A entrega será um diagnóstico técnico priorizado. Esta especificação prepara a revisão; não representa uma revisão já executada.

## Escopo

Inventariar o repositório, incluindo arquivos ocultos versionados. Revisar README e diagramas, API Java/Spring, estruturas Java e módulo C em aed-u1, entidades e migrações PostgreSQL/Flyway, regras de doações/bolsas/hospitais/estoque, autenticação e autorização, contratos REST, testes, build, Docker, automação, observabilidade e instruções de execução. Comparar arquitetura declarada e realizada, distinguindo funcionalidades futuras de defeitos. Registrar áreas ausentes ou não verificadas.

## Histórias

### US-001 — Entender a direção do projeto

Como responsável pelo projeto, quero comparar objetivos e implementação para saber o que está alinhado, incompleto ou divergente.

#### AC-001 — Inventário e matriz de alinhamento

- **Dado** os componentes e objetivos encontrados na revisão analisada
- **Quando** o levantamento de alinhamento for concluído
- **Então** o relatório lista todos os componentes encontrados e relaciona cada funcionalidade documentada a evidências de implementação e testes, classificando-a como implementada, parcial, planejada ou sem evidência
- **E** cada área recebe uma conclusão justificada, sem tratar funcionalidade futura como defeito automaticamente.

### US-002 — Conhecer inconsistências com evidências

Como mantenedor, quero achados reproduzíveis para distinguir defeitos confirmados, riscos e dúvidas.

#### AC-002 — Revisão da API, dados e segurança

- **Dado** o código, configurações, contratos e migrações da API
- **Quando** a revisão técnica for realizada
- **Então** cada área do escopo recebe resultado com evidência ou justificativa de não aplicabilidade, e cada achado contém identificador, tipo, severidade, arquivo e linha, comportamento esperado e observado, impacto, reprodução ou justificativa estática e recomendação
- **E** riscos sem reprodução ficam identificados como riscos, segredos são omitidos e ausência de achados exige registrar o que foi inspecionado.

#### AC-003 — Revisão de AED, testes e operação

- **Dado** os módulos Java/C, testes e configurações disponíveis
- **Quando** a revisão de qualidade e operação for realizada
- **Então** o relatório cobre estruturas de dados, invariantes e casos extremos, testes, build, banco, Docker, automação e documentação, registrando para cada comando o diretório, resultado e limitações
- **E** verificações impedidas por ferramentas, dependências ou ambiente são marcadas como não verificadas, sem contar como aprovadas.

### US-003 — Decidir os próximos passos

Como responsável pelo projeto, quero uma síntese priorizada para decidir quais correções realizar primeiro.

#### AC-004 — Diagnóstico consolidado e plano de ação

- **Dado** os três levantamentos concluídos e seus achados
- **Quando** a revisão for consolidada
- **Então** o diagnóstico identifica o commit Git e alterações locais analisadas, inclui pontos positivos comprovados, inconsistências sem duplicação, limitações e uma conclusão fundamentada sobre a direção do projeto
- **E** cada achado aparece no plano de ação com prioridade, justificativa, dependências e forma de validar a correção; nenhum achado dos levantamentos é descartado sem justificativa.

## Definição de pronto e provas

Cada critério terá teste anotado com @spec:AC-xxx para verificar completude, referências locais e coerência dos relatórios. Esses testes não substituem revisão técnica nem testes funcionais Java/C: conclusões precisam citar código, testes ou execuções reais. Registrar comandos e impedimentos honestamente.

A configuração atual do motor usa node --test e src/**, embora a API esteja em api/. Avaliar essa lacuna e propor adequação sem enfraquecer a constituição. Um gate limpo de documentação não comprova aprovação funcional da API.

A revisão somente será encerrada com provas por critério via verify revisao-projeto e audit --ci com saída 0. Nesta fase as tarefas permanecem pendentes.

## Fora de escopo

- Correções automáticas, refatorações e implementação de funcionalidades futuras durante o diagnóstico.
- Deploy, alterações em produção e testes de carga externos.
- Atestado jurídico ou clínico; o README descreve uma simulação com dados sintéticos.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-001 | A entrega será diagnóstico e recomendações; correções serão planejadas separadamente. | confirmada | Usuário autorizou executar o plano em 22/10/2026; entrega de diagnóstico, sem correções automáticas. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-001 | Confirmar diagnóstico sem correções automáticas, tarefas a paralelizar e modelos/esforços do plano? | respondida | Usuário autorizou o plano recomendado: T-001/T-002/T-003 paralelas, T-004 ao final, gpt-5.6-terra e esforço medium. |

