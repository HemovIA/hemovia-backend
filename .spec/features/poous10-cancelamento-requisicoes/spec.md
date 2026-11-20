# POOUS10 — Cancelamento de requisição e liberação do estoque

> feature: poous10-cancelamento-requisicoes
> status: auditada

## Contexto

A emissão e a alocação já funcionam, mas o hospital ainda não pode cancelar seu pedido e devolver as bolsas ao estoque. Esta entrega implementa a [POOUS10, issue #12](https://github.com/HemovIA/hemovia-backend/issues/12) sobre a develop c866394, mantendo as cinco camadas existentes.

## Histórias

### US-020 — Cancelar um pedido do hospital sem deixar bolsas presas na reserva

Como hospital solicitante, quero cancelar uma requisição pendente ou alocada, para disponibilizar novamente as bolsas reservadas para outros atendimentos.

#### AC-041 — Cancelar uma requisição alocada libera todas as bolsas

- **Dado** uma requisição do hospital em ALLOCATED com três bolsas RESERVED
- **Quando** o profissional envia POST /api/v1/requests/{id}/cancel com sessão e CSRF válidos
- **Então** recebe 200 com o ID e o status CANCELLED da requisição, e as três bolsas ficam AVAILABLE
- **E** a alocação fica CANCELLED, seus itens ficam RELEASED com released_at preenchido e os registros históricos são preservados
- **E** as bolsas liberadas podem ser reservadas por outra requisição sem violar o índice único de reservas ativas

#### AC-042 — Cancelar um pedido pendente não cria alocação

- **Dado** uma requisição do hospital em PENDING sem alocação
- **Quando** seu profissional solicita o cancelamento
- **Então** recebe 200 com o ID e o status CANCELLED, sem criar alocação nem alterar bolsas de outros pedidos

#### AC-043 — Impedir cancelamento depois de iniciar a viagem ou encerrar o pedido

- **Dado** uma requisição em IN_TRANSIT, DELIVERED ou CANCELLED, ou uma reserva vinculada a transporte em trânsito
- **Quando** seu hospital solicita o cancelamento
- **Então** recebe 422 com INVALID_STATE_TRANSITION e nenhum status ou vínculo é alterado

#### AC-044 — Proteger o cancelamento por sessão, papel, vínculo e CSRF

- **Dado** uma chamada sem sessão, com papel diferente de HOSPITAL, sem CSRF válido, com identificador inválido ou sem requisição pertencente ao hospital da sessão
- **Quando** o cliente tenta cancelar
- **Então** recebe respectivamente 401 com CSRF válido, 403 por papel ou CSRF, 400 para UUID inválido ou 404 para requisição inexistente ou de outro hospital
- **E** nenhuma requisição ou bolsa é alterada

#### AC-045 — Uma falha de gravação desfaz todo o cancelamento

- **Dado** uma requisição alocada e uma falha de banco provocada após as alterações da transação
- **Quando** o hospital tenta cancelar
- **Então** recebe erro interno e a requisição permanece ALLOCATED, a alocação permanece ALLOCATED, as bolsas permanecem RESERVED e os itens continuam RESERVED sem released_at
- **E** uma nova tentativa sem a falha consegue concluir o cancelamento

#### AC-046 — Coordenar o cancelamento com a alocação e com cancelamentos simultâneos

- **Dado** chamadas concorrentes para cancelar e alocar a mesma requisição, ou para cancelar duas vezes uma requisição alocada
- **Quando** as transações disputam os bloqueios no PostgreSQL
- **Então** o estado final é consistente: o cancelamento concluído deixa a requisição CANCELLED e nenhuma reserva ativa, sem devolver bolsas de outra requisição nem gravar alterações parciais
- **E** uma segunda tentativa de cancelar uma requisição já cancelada recebe 422

#### AC-047 — Documentar o cancelamento no Swagger

- **Dado** o documento OpenAPI gerado pela aplicação
- **Quando** o profissional consulta POST /requests/{id}/cancel
- **Então** encontra o identificador UUID, a resposta 200 com BloodRequestResponse, os erros 400, 401, 403, 404 e 422, a sessão cookieAuth e o cabeçalho obrigatório X-XSRF-TOKEN
- **E** a operação não exige corpo e as operações de emissão e alocação continuam documentadas

## Desenho

- Contrato público: POST /api/v1/requests/{id}/cancel sem corpo; 200 com o DTO existente BloodRequestResponse, contendo id e status. Acesso exclusivo ao papel HOSPITAL e à requisição do seu próprio hospital.
- CancelBloodRequest.execute(UUID) fica em service/usecase/request. Usa CurrentUserProvider e ClockProvider e uma porta RequestCancellationStore na camada repository. O controller apenas delega.
- Uma única transação READ_COMMITTED bloqueia primeiro a requisição, como o fluxo de alocação. O adaptador carrega as alocações e bloqueia as bolsas vinculadas em ordem de UUID antes de alterar estados. Valida as reservas e rejeita dados em trânsito, preservando a integridade histórica.
- As reservas são liberadas, nunca apagadas. Reutiliza CANCELLED e RELEASED já existentes e o campo released_at. Não altera migrações já aplicadas.
- Os testes HTTP usam PostgreSQL real e conferem os estados persistidos. Falha de commit provocada comprova rollback; chamadas concorrentes comprovam coordenação com os bloqueios da alocação.
- Execução manual do plano com agentes nativos: T-025 e T-026 em contextos separados e arquivos disjuntos no checkout existente; T-027 após a integração. A branch efetiva é codex/poous10-cancelamento-requisicoes. Os arquivos gerados do executor headless permanecem como referência do plano e não foram executados.

## Fora de escopo

- Cancelamento por administradores ou operadores de hemocentro, viagens em trânsito, cálculo de rotas, novos endpoints de transporte e Undo genérico de estoque.
- Exclusão de requisições ou reservas históricas e alterações no cadastro de bolsas sem relação com o cancelamento.

## Verificação

- A suíte completa executou 397 testes com zero falhas, erros ou skips. Os sete testes HTTP novos cobrem os seis critérios funcionais; o teste de contrato OpenAPI comprova o sétimo critério.
- O verificador registrou os sete critérios novos como PASS em .spec/verification/poous10-cancelamento-requisicoes.json. As sete especificações anteriores também tiveram suas provas renovadas contra o mesmo código, sem alterações posteriores nos fontes ou testes.
- A auditoria em modo CI terminou com exit 0, 47/47 critérios provados e o aviso preexistente de P-002 sobre o glob src/**/*.js sem arquivos. O relatório gerado está em auditoria.md.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-009 | Apenas o hospital solicitante cancela; sucesso retorna 200 com ID/status; pedidos já cancelados retornam 422. | confirmada | Usuário confirmou o contrato proposto em 20/11/2026. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-011 | Confirmar o contrato de acesso, sucesso e repetição do cancelamento? | respondida | Somente o hospital solicitante; 200 com ID/status; 422 em trânsito, entregue ou já cancelada. |
| Q-012 | Quais tarefas executar em paralelo e quais modelos e esforços usar? | respondida | T-025 e T-026 em paralelo, seguidas da T-027. GPT-6.1 Terra com esforço médio nas três. |
