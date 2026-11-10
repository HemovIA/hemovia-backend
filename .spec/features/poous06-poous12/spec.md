# POOUS06 e POOUS12 — Alocação de hemácias e reserva concorrente

> feature: poous06-poous12
> status: auditada

## Histórias

### US-017 — Atender uma requisição com as bolsas compatíveis que vencem primeiro

Como operador do hemocentro, quero alocar bolsas compatíveis para uma requisição pendente usando FEFO e receber a identificação da alocação. [POOUS06, issue #8](https://github.com/HemovIA/hemovia-backend/issues/8)

#### AC-030 — Alocação usa ABO/Rh e FEFO e grava a reserva completa

- **Dado** uma requisição pendente de duas bolsas de hemácias O+ e três bolsas elegíveis no estoque do hemocentro, O+ com validade de 2 dias, O− de 5 dias e O+ de 10 dias
- **Quando** o operador envia `POST /api/v1/requests/{id}/allocate`
- **Então** a API responde 201 com o ID da alocação, o ID da requisição, o estado `ALLOCATED` e os IDs das bolsas de 2 e 5 dias nessa ordem
- **E** o banco contém a alocação e os dois itens de reserva, com as bolsas em `RESERVED` e a requisição em `ALLOCATED`
- **E** bolsas incompatíveis, vencidas, que vencem na data de referência, de outro componente, de outro hemocentro, sem hemocentro ou com reserva ativa ficam fora da seleção

#### AC-031 — Estoque insuficiente preserva a requisição e o estoque

- **Dado** uma requisição pendente de cinco bolsas de hemácias AB− e estoque elegível com apenas duas
- **Quando** o operador solicita a alocação
- **Então** a API responde 409 com `INSUFFICIENT_INVENTORY` e uma mensagem explicativa
- **E** a requisição permanece `PENDING`, as bolsas disponíveis preservam seus estados e nenhuma reserva parcial é gravada

#### AC-032 — O comando exige sessão de hemocentro e CSRF

- **Dado** chamadas sem sessão válida, com um papel diferente de `HEMOCENTRO` ou sem o token CSRF obrigatório
- **Quando** tentam alocar a requisição
- **Então** a API devolve 401 para sessão ausente com CSRF válido ou 403 para papel ou CSRF inválido
- **E** o OpenAPI descreve a operação, o cookie de sessão, o cabeçalho CSRF e as respostas

#### AC-033 — A API informa se o pedido não pode ser alocado

- **Dado** um operador autenticado e uma requisição inexistente, já processada ou de um componente fora do recorte de hemácias
- **Quando** ele solicita a alocação
- **Então** a API devolve respectivamente 404, 409 com `REQUEST_ALREADY_PROCESSED` ou 422 com `INVALID_STATE_TRANSITION`
- **E** o estoque e as alocações preservam seu estado anterior

### US-018 — Impedir reserva duplicada e preservar a consistência em caso de falha

Como responsável pela integridade do estoque, quero que a reserva sobreviva a disputas simultâneas e que as alterações sejam desfeitas se a transação falhar. [POOUS12, issue #14](https://github.com/HemovIA/hemovia-backend/issues/14)

#### AC-034 — Duas requisições concorrentes reservam a última bolsa uma única vez

- **Dado** duas requisições pendentes de uma bolsa O+ e uma única bolsa elegível no hemocentro
- **Quando** duas chamadas executam a alocação em transações concorrentes
- **Então** uma responde 201 e a outra responde 409 com `INSUFFICIENT_INVENTORY`
- **E** só existe uma reserva ativa para a bolsa, a requisição vencedora fica `ALLOCATED` e a outra permanece `PENDING`

#### AC-035 — Falha de persistência desfaz toda a alocação

- **Dado** uma requisição pendente com estoque suficiente e uma falha de banco provocada na transação depois de aplicar as escritas
- **Quando** o operador tenta concluir a alocação
- **Então** a API devolve erro interno e a requisição permanece `PENDING`
- **E** as bolsas continuam `AVAILABLE`, nenhuma alocação ou item parcial fica gravado e uma nova tentativa pode usar as mesmas bolsas

## Desenho

- O comando retorna `AllocationResponse` com `id`, `requestId`, `status` e `bloodBagIds`. O hemocentro é derivado da sessão e o corpo da chamada fica vazio.
- O caso de uso roda em uma transação PostgreSQL. Bloqueia a requisição por ID antes de ler seu estado. Bloqueia as bolsas elegíveis do hemocentro em ordem de UUID para coordenar as transações que disputam o mesmo estoque. O índice único de reservas ativas continua protegendo a integridade.
- Usa a data UTC do relógio injetado, consistente com o cadastro de bolsas. A matriz de hemácias filtra ABO/Rh e a fila FEFO ordena as bolsas elegíveis. As entidades `Allocation` e `AllocationItem` preservam o registro da reserva.
- Os testes de aceite atravessam o endpoint e usam PostgreSQL real. A prova de rollback provoca uma falha de banco depois das escritas na transação do pedido.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-007 | O recorte cobre hemácias e o operador de hemocentro dispara a alocação pelo endpoint, usando o estoque do seu vínculo. | confirmada | Confirmada na revisão do plano. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-009 | Confirmar o recorte de hemácias e o acionamento manual autenticado? | respondida | Hemácias com endpoint testável no Swagger. |
| Q-010 | Quais tarefas devem rodar em paralelo e quais modelos e esforços serão usados? | respondida | T-021 e T-022 em paralelo, seguidas da T-023. GPT-6.1 Terra com esforço médio nas três. |
