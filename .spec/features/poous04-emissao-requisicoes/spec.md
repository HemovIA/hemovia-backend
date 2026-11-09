# POOUS04 — Emissão de Requisições Hospitalares

> feature: poous04-emissao-requisicoes
> status: auditada

## Histórias

### US-014 — Solicitar hemocomponentes

Como profissional de um hospital, quero solicitar hemocomponentes informando tipo sanguíneo, fator Rh, componente, quantidade, prioridade e prazo de entrega para suprir demandas médicas urgentes.

#### AC-022 — Solicitação válida recebe um ID e fica pendente

- **Dado** um profissional autorizado de um hospital cadastrado que precisa de quatro bolsas de plaquetas A+ com prioridade alta e prazo de seis horas
- **Quando** ele envia a solicitação a `POST /api/v1/requests`
- **Então** recebe HTTP 201 com o ID da requisição e o status `PENDING`
- **E** o registro identificado pela resposta conserva o hospital solicitante, ABO/Rh, componente, quantidade, prioridade, prazo e status `PENDING`

#### AC-023 — Quantidade inválida recebe erro de validação

- **Dado** um profissional autorizado com os demais dados válidos e quantidade omitida, nula, zero ou negativa
- **Quando** ele envia a solicitação a `POST /api/v1/requests`
- **Então** recebe HTTP 400 com o erro no campo de quantidade e nenhuma requisição é gravada

#### AC-024 — A emissão exige o papel de hospital e respeita seu vínculo

- **Dado** uma chamada sem sessão válida ou um usuário que não tem o papel `HOSPITAL`
- **Quando** a chamada tenta emitir uma requisição com a proteção CSRF da API
- **Então** a API devolve HTTP 401 sem sessão ou 403 para papel sem permissão
- **E** uma chamada autorizada só pode gravar para o hospital vinculado à sessão, mesmo se o corpo incluir o ID de outro hospital

#### AC-025 — A documentação da API descreve a emissão

- **Dado** que a documentação OpenAPI está habilitada
- **Quando** o cliente consulta o documento gerado
- **Então** encontra o contrato da emissão com campos de entrada, sessão e CSRF e respostas 201, 400, 401 e 403

## Contrato

- O papel `HOSPITAL` usa a sessão por cookie e o cabeçalho CSRF já definidos na API. O hospital solicitante vem do vínculo autenticado.
- O corpo informa `bloodType`, `rhFactor`, `component`, `quantity`, `priority` e `deliveryDeadline`. O servidor define o estado inicial. O prazo usa data e hora ISO 8601 com fuso e precisa estar no futuro.
- As prioridades seguem o banco (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`). O prazo permanece opcional na persistência de registros antigos e é obrigatório para emissão pela API.

## Fora de escopo

- Consulta por ID, edição, cancelamento, alocação de estoque, despacho e fila de processamento.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-006 | A sessão identifica o hospital solicitante. O cliente envia o instante limite de entrega em `deliveryDeadline`, calculado a partir da janela desejada. | confirmada | O usuário confirmou o contrato na revisão do plano. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-006 | Confirmar que a emissão usa o hospital da sessão e recebe um prazo absoluto em ISO 8601? | respondida | Hospital da sessão e prazo absoluto. |
| Q-007 | Quais tarefas executar em paralelo e quais modelos e esforços usar? | respondida | T-016 e T-017 em paralelo, ambas com GPT-6.1 Terra e esforço médio. |
