# Consultas operacionais Specification

## Problem Statement

A API oferece operações de escrita de requisições e transportes, mas não permite recuperar seu histórico. Hospitais possuem apenas criação. As consultas devem permitir ao frontend recuperar dados persistidos sem depender dos comprovantes de uma sessão.

## Goals

- Disponibilizar listagem e detalhe de requisições, transportes e hospitais com isolamento por perfil.
- Documentar os contratos no OpenAPI e verificar consultas e permissões com testes automatizados.

## Out of Scope

| Feature | Reason |
| --- | --- |
| Alterações no frontend | Esta entrega completa as consultas da API. |
| Telemetria em tempo real | Exige contrato e fluxo próprios. |
| Alterar operações de escrita | Preservar os contratos atuais. |
| Publicação e teste manual conjunto | O usuário testará a integração depois. |

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Visibilidade | Regras detalhadas em context.md | Usuário respondeu "Sim, seguir essas regras" | y |
| Ocultação de registros alheios | 404 para detalhe fora do escopo | Convenção de CancelBloodRequest e CompleteTransport | y - código existente |
| Paginação | page inicia em 0; size padrão 20, entre 1 e 100; resposta content, page, size, totalElements, totalPages | Contrato explícito para novas rotas, sem modificar rotas existentes | Proposta para aprovação |
| Ordenação | Pedidos por createdAt decrescente, transportes por startedAt decrescente e hospitais por name crescente; desempate por id crescente | Ordenação determinística para paginação | Proposta para aprovação |

**Open questions:** none - decisões de acesso resolvidas; contrato proposto abaixo aguarda aprovação da especificação.

## User Stories

### P1: Consultar requisições

**User Story**: Como operador autorizado, quero recuperar pedidos persistidos e seus detalhes para continuar o atendimento entre sessões.

**Acceptance Criteria**:

1. WHEN GET /api/v1/requests é chamado THEN a API SHALL retornar 200 com página de pedidos contendo id, hospitalId, hospitalName, bloodType, rhFactor, component, quantity, priority, deliveryDeadline, status, createdAt e updatedAt.
2. WHEN GET /api/v1/requests/{id} é chamado para pedido visível THEN a API SHALL retornar 200 com os dados do pedido e suas alocações, incluindo identificadores, hemocentro, estado, bolsas, rota e transporte associado quando existentes.
3. WHEN um usuário consulta pedidos THEN a API SHALL limitar registros e totais a ADMIN: todos; HOSPITAL: seu hospital; HEMOCENTRO: pendentes ou com alocação de seu hemocentro; TRANSPORTADOR: com transporte do seu veículo vinculado.
4. WHEN filtros status, hospitalId, bloodType, rhFactor, component ou priority são informados THEN a API SHALL retornar apenas pedidos visíveis que satisfaçam todos os filtros informados.

**Independent Test**: Persistir pedidos de hospitais distintos e alocações/transportes com vínculos distintos; consultar lista e detalhe com cada perfil e verificar payload e isolamento.

### P1: Consultar transportes

**User Story**: Como operador autorizado, quero consultar viagens persistidas para acompanhar entregas.

**Acceptance Criteria**:

1. WHEN GET /api/v1/transports é chamado THEN a API SHALL retornar 200 com página contendo os campos atuais de TransportResponse e identificadores de hospital e hemocentro.
2. WHEN GET /api/v1/transports/{id} é chamado para transporte visível THEN a API SHALL retornar 200 com os dados da viagem, rota e identificadores das bolsas da alocação.
3. WHEN um usuário consulta transportes THEN a API SHALL limitar registros e totais a ADMIN: todos; HOSPITAL: hospital de destino vinculado; HEMOCENTRO: hemocentro da alocação vinculado; TRANSPORTADOR: veículo vinculado.
4. WHEN filtros status, requestId ou vehicleId são informados THEN a API SHALL retornar apenas transportes visíveis que satisfaçam todos os filtros informados.

**Independent Test**: Persistir viagens para unidades e veículos distintos e verificar filtros, listagem e detalhe por perfil.

### P1: Consultar hospitais

**User Story**: Como operador autorizado, quero consultar os hospitais cadastrados para identificar destinos e cadastros existentes.

**Acceptance Criteria**:

1. WHEN GET /api/v1/hospitals ou GET /api/v1/hospitals/{id} é chamado por perfil autorizado THEN a API SHALL retornar 200 com página ou detalhe usando os campos de HospitalResponse.
2. WHEN ADMIN ou HEMOCENTRO consulta hospitais THEN a API SHALL permitir todos os hospitais; HOSPITAL SHALL consultar apenas o hospital vinculado.
3. IF TRANSPORTADOR tenta consultar hospitais THEN a API SHALL retornar 403.
4. WHEN filtros name ou city são informados THEN a API SHALL aplicar busca por trecho sem distinção de maiúsculas, combinada com o escopo de acesso.

**Independent Test**: Consultar hospitais com nomes e cidades distintos por perfil e verificar resultados e totais.

### P1: Contrato e segurança comuns

**Acceptance Criteria**:

1. IF uma consulta não tem sessão válida THEN a API SHALL retornar 401.
2. IF um detalhe não existe ou está fora do escopo do usuário THEN a API SHALL retornar 404.
3. IF UUID, enum, page ou size é inválido, page é negativo ou size não está entre 1 e 100 THEN a API SHALL retornar 400 no formato ApiError.
4. WHEN uma lista é consultada sem parâmetros de paginação THEN a API SHALL usar page=0 e size=20 com content, page, size, totalElements e totalPages.
5. WHEN uma lista é paginada THEN a API SHALL ordenar pedidos por createdAt DESC e id ASC, transportes por startedAt DESC e id ASC, e hospitais por name ASC e id ASC.
6. WHEN não existem registros visíveis para os filtros THEN a API SHALL retornar 200 com content vazio e totalElements=0.
7. The API SHALL documentar as seis novas consultas, filtros, paginação, autenticação e respostas de erro no OpenAPI.
8. The API SHALL executar as consultas sem alterar estados de pedidos, alocações, bolsas, rotas ou transportes.

**Independent Test**: Testes HTTP com sessão válida/ausente, UUIDs alheios/inexistentes, parâmetros inválidos, páginas vazias e inspeção do OpenAPI e do estado persistido.

## Edge Cases

- Usuário com perfil de unidade mas vínculo ausente não recebe registros de outras unidades.
- Filtros nunca ampliam o escopo de acesso; totais respeitam os mesmos predicados das listas.
- Alocações canceladas permanecem disponíveis no histórico do pedido autorizado.
- Consulta não chama provedor externo de rotas; utiliza a rota persistida.
- Retries, transições, expiração, concorrência de escrita e falhas externas: N/A porque o escopo adiciona somente leituras transacionais, sem novas integrações ou regras de escrita.

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| READ-01 | Consultar requisições | Specify | Pending |
| READ-02 | Consultar transportes | Specify | Pending |
| READ-03 | Consultar hospitais | Specify | Pending |
| READ-04 | Contrato e segurança comuns | Specify | Pending |

## Sources

- context.md: pedido e decisão de visibilidade do usuário.
- api/src/main/java/com/hemovia/api/controller: contratos HTTP existentes.
- api/src/main/java/com/hemovia/api/model/AuthenticatedUser.java: vínculos por papel.
- CancelBloodRequest e CompleteTransport: ocultação de recursos alheios com 404.

## Success Criteria

- As seis consultas estão documentadas e cobertas por testes de resultado e isolamento por perfil.
- Os contratos de escrita existentes continuam funcionando.
