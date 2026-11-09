# POOUS13 — Gestão do cadastro de hemocentros

> feature: poous13-cadastro-hemocentros
> status: auditada

## Contexto

O modelo de hemocentros já registra nome, cidade, latitude e longitude. A API permite administrar esses dados, vincular operadores ao centro e manter a localização que será usada como origem em transportes.

## Histórias

### US-019 — Gerir o cadastro de hemocentros

Como administrador, quero cadastrar, consultar, atualizar e excluir hemocentros com sua localização para manter o cadastro operacional e preservar o histórico.

#### AC-036 — Criar, consultar e atualizar um hemocentro

- **Dado** um administrador autenticado e dados válidos de nome, cidade, latitude e longitude
- **Quando** cria um hemocentro, lista o cadastro, consulta seu ID e atualiza seus dados
- **Então** recebe 201 com o ID na criação, e 200 nas consultas e na atualização, que mantém o mesmo ID e persiste as novas coordenadas

#### AC-037 — Rejeitar dados inválidos e proteger operações de escrita

- **Dado** uma tentativa de criar, atualizar ou excluir um hemocentro
- **Quando** falta autenticação, papel ADMIN ou token CSRF, ou quando a criação ou edição contém campos inválidos
- **Então** a API retorna 401 sem sessão com CSRF válido, 403 sem o papel ou sem CSRF, e 400 para dados inválidos, sem gravar as alterações

#### AC-038 — Excluir logicamente e preservar vínculos históricos

- **Dado** um hemocentro com uma bolsa já vinculada e outro hemocentro ativo
- **Quando** um administrador exclui o primeiro
- **Então** a API retorna 204, grava o instante em `deleted_at` e preserva a linha e o vínculo histórico. O hemocentro excluído some da listagem e retorna 404 em consulta individual, atualização ou nova exclusão. Registros anteriores à migração ficam ativos até a exclusão

#### AC-039 — Impedir novos vínculos e alocações após exclusão

- **Dado** um hemocentro excluído e um operador que já estava vinculado a ele
- **Quando** alguém tenta criar ou alterar o vínculo de um operador, cadastrar uma bolsa ou doação para o centro, ou alocar uma requisição com a sessão desse centro
- **Então** as operações retornam 404 para o centro e não criam vínculos nem alocações

#### AC-040 — Publicar o contrato no Swagger

- **Dado** o OpenAPI ativo
- **Quando** um profissional consulta o contrato do cadastro
- **Então** encontra as cinco operações, autenticação por cookie, requisitos de CSRF nas escritas e os status do contrato

## Fora de escopo

- Exclusão lógica das demais entidades, restauração de hemocentros e cálculo de rotas.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-008 | Aplicar exclusão lógica ao hemocentro. | confirmada | O usuário pediu soft-delete com `deleted_at`. |

## Perguntas em aberto

Nenhuma.

## Contrato HTTP

- Escrita restrita a ADMIN. As leituras seguem os papéis de negócio autenticados da API.
- As operações usam `/api/v1/blood-centers`. A criação e a listagem usam a coleção, e a consulta, atualização completa e exclusão usam `/{id}`.
- O timestamp da exclusão fica na coluna `blood_centers.deleted_at`. Registros com valor nulo são ativos, incluindo os anteriores à migração. Novas associações obtêm um bloqueio compartilhado no centro ativo, enquanto exclusão e edição obtêm bloqueio exclusivo.
