# Spec: Finalizando entidades base

> feature: finalizando-entidades-base
> status: auditada

## Contexto

Completar o modelo persistente que sustenta o ciclo do Hemovia: doação, estoque, requisição hospitalar, alocação, rota, transporte e telemetria. O usuário confirmou entidades do ciclo completo, migrações e testes, deixando CRUDs novos e algoritmos para features próprias. Confirmou também que uma doação pode originar várias bolsas, preservando o contrato atual de criação de uma bolsa durante a adaptação.

Base observada: branch develop, commit 5fd3c0c. Existem User, Person, Hospital, Donation e BloodBag, além de V1, V2 de usuários e V3 de hospitais. A versão duplicada apontada na revisão anterior já foi corrigida; não repetir essa correção nem reescrever migrações existentes. Fonte funcional: README e histórias POOUS01–04, POOUS06–12, AEDUS01–03, ESTUS01–05 e RSDUS03 em diagramas/README.md. A implementação permanece uma simulação com dados sintéticos.

## Entrega

14 entidades JPA: manter User, Person, Hospital, Donation e BloodBag; adicionar BloodCenter, Vehicle, BloodRequest, Allocation, AllocationItem, DeliveryRoute, Transport, TelemetryReading e StockMovement. RouteWaypoint será um objeto de valor persistido em coleção ordenada. Estados, fator Rh e hemocomponente terão enums explícitos. Não criar outra entidade de requisição com o nome HospitalRequest, já usado pela estrutura didática em memória.

A existência de classes não basta: incluir relacionamentos, repositórios, validações, integridade no PostgreSQL, migrações e provas funcionais. As associações desconhecidas em registros legados permanecem explicitamente ausentes; não inventar doadores, hemocentros ou dados históricos para preencher campos. Campos novos de rastreabilidade nos contratos antigos são aditivos e seus limites ficam documentados no design.

## Histórias

### US-004 — Identificar participantes e locais da rede

Como responsável pela rede simulada, quero distinguir operadores, doadores, hospitais, hemocentros e veículos para relacionar corretamente os registros.

#### AC-005 — Identidades e cadastros sem duplicação de papéis

- **Dado** um usuário de acesso, uma pessoa doadora e um veículo com identificação válida
- **Quando** os cadastros são persistidos e consultados
- **Então** os identificadores e dados obrigatórios são preservados, User continua separado de Person, um doador pode participar de várias doações/bolsas e a identificação do veículo não pode ser duplicada
- **E** dados pessoais continuam sintéticos e credenciais não aparecem em respostas públicas.

#### AC-006 — Hospitais e hemocentros com localização válida

- **Dado** nome, cidade, coordenadas e prioridade hospitalar
- **Quando** Hospital e BloodCenter são validados e persistidos
- **Então** nomes vazios, coordenadas ausentes ou fora de latitude -90 a 90 e longitude -180 a 180, e prioridade hospitalar fora de 1 a 5 são rejeitados com erro explícito
- **E** valores válidos, inclusive coordenadas zero, são preservados; entradas inválidas não são ignoradas silenciosamente.

### US-005 — Rastrear doações e bolsas de hemocomponentes

Como operador de estoque, quero registrar várias bolsas de uma doação e reconhecer sua origem para manter a rastreabilidade.

#### AC-007 — Uma doação pode originar várias bolsas

- **Dado** uma doação e duas bolsas distintas vinculadas a ela
- **Quando** o conjunto é persistido, recarregado e uma terceira associação inválida é tentada
- **Então** as duas bolsas pertencem à mesma doação, cada bolsa aponta para no máximo uma doação e vínculos conflitantes de doador são rejeitados quando ambas as referências são conhecidas
- **E** remover uma associação não apaga automaticamente pessoa, bolsa, alocação ou histórico; a migração preserva os vínculos antigos.

#### AC-008 — Características das bolsas sem valores ambíguos

- **Dado** tipo ABO, fator Rh, hemocomponente, datas e volume opcional em mililitros
- **Quando** uma bolsa é validada e persistida
- **Então** códigos desconhecidos, datas de validade anteriores à coleta e volume informado menor ou igual a zero são rejeitados; enums são armazenados por nome e datas civis são preservadas como LocalDate
- **E** valores conhecidos dos contratos atuais, incluindo POSITIVE e WHOLE_BLOOD, continuam aceitos; dados legados não mapeáveis interrompem a migração com diagnóstico em vez de sofrer conversão arbitrária.

### US-006 — Representar demanda e reserva de estoque

Como operador da rede, quero persistir requisições e suas bolsas alocadas para que os próximos fluxos operacionais tenham referências consistentes.

#### AC-009 — Requisição hospitalar com quantidade e estado explícitos

- **Dado** um hospital existente, ABO/Rh, hemocomponente e quantidade de bolsas
- **Quando** BloodRequest é criado e recarregado
- **Então** quantidades inteiras positivas são aceitas com estado inicial PENDING e data/hora de criação; quantidade zero/negativa, hospital inexistente e tipos inválidos são rejeitados
- **E** a quantidade representa unidades de bolsa, sem ser confundida com volume em mililitros.

#### AC-010 — Alocações preservam histórico sem reserva duplicada

- **Dado** uma requisição, uma alocação e uma bolsa elegível para associação
- **Quando** duas associações ativas para a mesma bolsa são tentadas, inclusive em transações independentes
- **Então** no máximo uma associação ativa é persistida; liberar uma reserva permite nova associação mantendo a anterior no histórico
- **E** chaves estrangeiras e controle de versão detectam referências inexistentes e atualização concorrente obsoleta, com rollback da tentativa que falhar. A seleção por compatibilidade/FEFO permanece fora desta feature.

### US-007 — Representar logística e monitoramento

Como operador logístico, quero relacionar rota, transporte, veículo e leituras para sustentar o acompanhamento das bolsas.

#### AC-011 — Rota persistida com origem, destino e sequência ordenada

- **Dado** um hemocentro de origem, hospital de destino e pontos de passagem
- **Quando** DeliveryRoute é persistida e recarregada
- **Então** origem, destino, ordem dos pontos, distância em quilômetros e duração estimada em segundos são preservados; referências inexistentes, coordenadas inválidas e medidas negativas são rejeitadas
- **E** a ordem não se perde nem há duas posições iguais na mesma rota. Nenhum algoritmo de menor caminho é implementado aqui.

#### AC-012 — Transporte, telemetria e movimentações rastreáveis

- **Dado** alocação, rota, veículo e bolsas existentes
- **Quando** Transport, TelemetryReading e StockMovement são persistidos e consultados
- **Então** o transporte referencia a alocação, rota e veículo, suas bolsas são obtidas pelos itens alocados, leituras identificam transporte/sensor/instantes/temperatura em Celsius e movimentos identificam bolsa, mudança de estado, instante e ator quando conhecido
- **E** relações inexistentes, temperatura ausente ou não finita e término anterior ao início são rejeitados; histórico não é apagado por exclusão em cascata de cadastros. Não se presume uma faixa térmica clínica universal.

### US-008 — Inicializar e evoluir o banco sem perder dados

Como mantenedor, quero migrações verificadas para instalar e atualizar o modelo com segurança.

#### AC-013 — Migrações funcionam em base nova e base legada

- **Dado** um PostgreSQL vazio e outro contendo V1 a V3 com dados sintéticos representativos
- **Quando** as novas migrações Flyway são aplicadas e o Hibernate valida o esquema
- **Então** ambas as bases chegam ao mesmo esquema válido, IDs e associações legadas são preservados, não há versões duplicadas, e os testes provam chaves estrangeiras, índices de unicidade ativa e restrições de domínio
- **E** dados incompatíveis geram erro identificável antes de transformações destrutivas; migrações existentes e checksums não são reescritos, e repetição do migrate não reaplica versões já concluídas.

#### AC-014 — Provas refletem testes Java e PostgreSQL reais

- **Dado** os testes anotados da feature e as suítes existentes
- **Quando** a verificação completa é executada em ambiente de teste isolado
- **Então** cada critério recebe prova obtida de casos JUnit realmente executados, a integração usa PostgreSQL e relatórios recentes, e falha, teste ausente, relatório antigo ou skip impedem aprovação
- **E** o comando inclui a suíte existente e o teste de contexto, sem usar H2 como prova de comportamento PostgreSQL nem marcar aprovação por mera existência de classes. Os testes documentais da revisão anterior verificam seu commit histórico, sem falsificar o estado atual.

### US-009 — Evoluir o modelo preservando contratos existentes

Como consumidor da API atual, quero continuar criando e consultando uma bolsa por doação enquanto o modelo passa a suportar várias.

#### AC-015 — Contratos atuais continuam utilizáveis

- **Dado** requisições válidas dos contratos atuais de autenticação, hospital, doação e bolsa
- **Quando** os endpoints existentes são exercitados após a evolução do modelo
- **Então** continuam funcionando para o caso de uma bolsa, o donorId informado passa a ser resolvido e associado em vez de descartado, e novos campos de rastreabilidade não inventam valores quando omitidos
- **E** respostas de doação disponibilizam a coleção bloodBags e mantêm bloodBag para o caso de exatamente uma bolsa; com várias, o campo singular fica nulo e documentado, sem escolher uma bolsa arbitrariamente ou produzir ciclos de serialização.

## Definição de pronto

Todos os critérios têm casos positivos e negativos anotados @spec:AC-xxx, com asserções de comportamento e persistência. O gate deve consumir resultados por teste e executar a suíte completa; não alterar testes para esconder regressões. PostgreSQL de teste é descartável e separado dos ambientes do usuário. Dependências/ferramentas ausentes são impedimentos, nunca aprovação ou skip. Atualizar documentação do modelo e dos comandos; finalizar somente com verify da feature e audit --ci exit 0. Criar esta especificação não executa a implementação.

## Fora de escopo

- Novos CRUDs completos, telas e endpoints de requisição/alocação/logística.
- Matching ABO/Rh, FEFO, Dijkstra, roteirização, cancelamento/entrega automáticos e ingestão concorrente de telemetria.
- Reescrever a arquitetura inteira, estatísticas materializadas, tabelas para algoritmos didáticos e alterações no módulo C.
- Unificar User com Person, criar pacientes reais, validação clínica ou afirmar conformidade jurídica.
- Corrigir todos os achados da revisão; apenas os necessários à integridade das entidades e à prova desta entrega entram aqui.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-002 | A entrega compreende o modelo persistente do ciclo completo, migrações e testes; fluxos novos ficam para outras features. | confirmada | Escolha explícita do usuário em 22/10/2026. |
| ASM-003 | Uma doação pode gerar várias bolsas, preservando a criação atual de uma bolsa durante a adaptação. | confirmada | Escolha explícita do usuário em 22/10/2026. |
| ASM-004 | Person representa o doador sintético existente; associações desconhecidas em dados/contratos legados podem permanecer ausentes, sem cadastros fictícios. | confirmada | Usuário autorizou iniciar o plano apresentado em 22/10/2026, preservando vínculos legados desconhecidos sem dados fictícios. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-002 | Executar as tarefas em sequência, como recomendado, ou preparar faixas após os pré-requisitos? Confirmar também modelo e esforço para esta feature. | respondida | Usuário autorizou iniciar o plano apresentado: sequencial, gpt-5.6-terra, esforço medium. |
| Q-003 | Confirmar a política de compatibilidade sem fabricar doadores/hemocentros para registros legados incompletos? | respondida | Plano autorizado em 22/10/2026 com compatibilidade legada proposta, sem fabricar cadastros. |

