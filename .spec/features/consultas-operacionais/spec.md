# Spec: Consultas operacionais

> feature: consultas-operacionais
> status: auditada

## Contexto

As consultas recuperam requisições, transportes e hospitais persistidos para dar continuidade à operação entre sessões. Em 20/11/2026, o usuário pediu para completar as consultas da API antes de testar o conjunto e aprovou as [regras de visibilidade](#decisões-confirmadas).

## Histórias

### US-028 — Consultar requisições

Como operador autorizado, quero consultar requisições para recuperar dados persistidos sem depender dos comprovantes da sessão.

#### AC-075 — Listar pedidos

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** GET /api/v1/requests retorna página com id, hospitalId, hospitalName, bloodType, rhFactor, component, quantity, priority, deliveryDeadline, status, createdAt e updatedAt.

#### AC-076 — Detalhar pedido

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** GET /api/v1/requests/{id} retorna os dados do pedido e o histórico de alocações, inclusive as canceladas, com hemocentro e estado, bolsas, rota persistida e transporte associado quando existentes.

#### AC-077 — Isolar pedidos por vínculo

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** ADMIN vê todos; HOSPITAL vê seu hospital; HEMOCENTRO vê pendentes e os pedidos que alocou; TRANSPORTADOR vê pedidos das viagens do veículo vinculado; totais respeitam esse escopo.

#### AC-078 — Filtrar pedidos

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** status, hospitalId, bloodType, rhFactor, component e priority restringem os resultados com interseção dos filtros informados e do escopo de acesso.

### US-029 — Consultar transportes

Como operador autorizado, quero consultar transportes para recuperar dados persistidos sem depender dos comprovantes da sessão.

#### AC-079 — Listar transportes

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** GET /api/v1/transports retorna página com os campos de TransportResponse e hospitalId e bloodCenterId.

#### AC-080 — Detalhar transporte

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** GET /api/v1/transports/{id} retorna dados da viagem, rota persistida e identificadores das bolsas.

#### AC-081 — Isolar transportes por vínculo

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** ADMIN vê todos; HOSPITAL vê seu destino; HEMOCENTRO vê as viagens de suas alocações; TRANSPORTADOR vê seu veículo; totais respeitam esse escopo.

#### AC-082 — Filtrar transportes

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** status, requestId e vehicleId restringem os resultados pela interseção dos filtros e do escopo.

### US-030 — Consultar hospitais

Como operador autorizado, quero consultar hospitais para recuperar dados persistidos sem depender dos comprovantes da sessão.

#### AC-083 — Consultar hospitais

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** GET /api/v1/hospitals e GET /api/v1/hospitals/{id} retornam página e detalhe com os campos de HospitalResponse.

#### AC-084 — Isolar hospitais por vínculo

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** ADMIN e HEMOCENTRO veem todos os hospitais e HOSPITAL vê apenas seu vínculo, inclusive nos totais.

#### AC-085 — Negar cadastro ao transportador

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** TRANSPORTADOR recebe 403 ao consultar lista ou detalhe de hospitais.

#### AC-086 — Buscar hospitais

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** name e city fazem busca por trecho sem distinção de maiúsculas, combinada com o escopo de acesso.

### US-031 — Consultar com segurança e contrato previsível

Como operador autorizado, quero consultar com segurança e contrato previsível para recuperar dados persistidos sem depender dos comprovantes da sessão.

#### AC-087 — Exigir sessão

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** as seis consultas respondem 401 sem sessão válida.

#### AC-088 — Ocultar recursos alheios

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** detalhes inexistentes ou fora do escopo de acesso respondem 404 para perfis autorizados à rota.

#### AC-089 — Validar parâmetros

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** UUID, enum e paginação malformados, page negativo ou size fora de 1 a 100 resultam em 400 no formato ApiError.

#### AC-090 — Paginar e ordenar

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** page=0 e size=20 são os padrões; respostas contêm content, page, size, totalElements e totalPages; pedidos ordenam por createdAt DESC, transportes por startedAt DESC e hospitais por name ASC, com desempate id ASC.

#### AC-091 — Consultar resultado vazio

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** filtros sem correspondência e usuários de unidade sem vínculo recebem content vazio e totalElements=0 sem exposição de dados alheios.

#### AC-092 — Documentar consultas

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** OpenAPI descreve as seis rotas, filtros, paginação, autenticação, payloads e erros aplicáveis.

#### AC-093 — Preservar estados

- **Dado** registros persistidos e usuários com perfis e vínculos distintos
- **Quando** a consulta correspondente é solicitada com os parâmetros e a sessão do cenário
- **Então** as leituras não alteram pedidos, alocações, bolsas, rotas ou transportes e não acionam provedor externo de rotas.

## Fora de escopo

- Frontend, telemetria em tempo real, publicação e mudanças nas operações de escrita.
- Teste manual integrado: o usuário fará depois; testes automatizados integram esta entrega.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-014 | Paginação padrão 20, limite 100 e ordenação determinística descritos acima | confirmada | Usuário: "Pode iniciar o plano, sequencial e com a paginação padrao recomendada". |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-021 | Quais tarefas executar em paralelo? | respondida | Usuário: "Pode iniciar o plano, sequencial e com a paginação padrao recomendada". Todas as tarefas em sequência. |
| Q-022 | Quais modelos e esforços utilizar? | respondida | Após a indisponibilidade de gpt-6-terra, o usuário respondeu "Sim" à substituição por gpt-6.1-terra com esforço médio. Aprovado em todas as tarefas. |

## Decisões confirmadas

Usuário respondeu "Sim, seguir essas regras": administrador vê tudo; hospital vê seus pedidos, entregas e hospital; hemocentro vê pedidos pendentes e aqueles que alocou, seus transportes e todos os hospitais; transportador vê viagens de seu veículo e pedidos dessas viagens, sem consulta ao cadastro de hospitais. Detalhes fora do escopo recebem 404, seguindo os casos de uso existentes.
