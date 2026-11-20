# 🩸 HEMOVIA — Rota Vital

## Descrição do projeto

O **HEMOVIA (Rota Vital)** é um sistema para simulação da gestão e distribuição de hemocomponentes em uma rede de hemocentros e hospitais. O projeto centraliza o cadastro de hospitais e bolsas de sangue, controle de estoque, requisições hospitalares, verificação de compatibilidade ABO/Rh, priorização por validade (FEFO), roteirização logística, telemetria de transporte e indicadores estatísticos da rede. Todos os dados utilizados são **sintéticos** — nenhuma informação real de pacientes ou doadores é armazenada.

Projeto Integrador desenvolvido na **CESAR School**, curso de Análise e Desenvolvimento de Sistemas, 3º semestre (2026.2), integrando as disciplinas de POO, AED, Estatística, Infraestrutura de Software (SO) e Infraestrutura de Comunicação (RSD).

## Tecnologias usadas

- **Backend:** Java + Spring Boot
- **Arquitetura:** backend em cinco camadas Java (`controller`, `service`, `dto`, `repository` e `model`)
- **Banco de dados:** PostgreSQL (Spring Data JPA)
- **Documentação de API:** OpenAPI/Swagger
- **CI/CD:** GitHub Actions
- **Containerização:** Docker / docker-compose
- **Protótipo:** Lovable (Lo-Fi)

---

## Entregas

### Entrega 01 — 31/08

#### Histórias de usuário (BDD)

> Histórias completas, com cenários de validação (positivo e negativo), escritas em formato BDD (Dado/Quando/Então).

- **Cenário 1 (Positivo):** Dado uma bolsa BAG-999 com validade em dia, quando inserida, então é adicionada e localizável com complexidade O(n).
- **Cenário 2 (Negativo):** Dado uma lista populada, quando tenta-se remover BAG-000 (inexistente), então retorna falso/exceção sem corromper a lista.

Documento completo com todas as histórias do backlog (POO, AED, EST, SO e RSD — mínimo 7 aqui apresentadas): **[link para o documento de histórias no GitHub](https://github.com/HemovIA/hemovia-backend/tree/main/diagramas)**

#### Protótipo (Lo-Fi — Figma)

- Protótipo Lo-Fi cobrindo no mínimo 5 histórias: **[Protótipo](https://hemovia-lofiprototype.lovable.app/)**
- Screencast explicando cada história implementada no protótipo: **[YouTube](https://youtu.be/6ujUBhdNyCA?is=7e5ZPCPDCTEUPSc6)**

---

### Entrega 02 — 21/09

Nesta entrega, foram implementadas duas histórias de usuário do backend da plataforma HemovIA:

- **POOUS01 — Cadastro de Doação e Entrada de Bolsa no Estoque**
- **POOUS02 — Cadastro e Controle de Validade das Bolsas de Sangue**

Essas histórias contemplam o cadastro de doações e o registro das bolsas de sangue, incluindo a associação com a doação, o controle de validade e a estrutura inicial do fluxo de estoque do sistema.

#### Funcionalidades entregues

- Cadastro de doação via API REST
- Cadastro de bolsas de sangue com dados de tipo sanguíneo, fator Rh, componente, data de coleta e validade
- Associação de uma doação com uma bolsa de sangue
- Persistência com Spring Data JPA + PostgreSQL
- Estrutura de service/controller/repository para a camada de negócio
- Configuração inicial de segurança para ambiente de desenvolvimento
- Migration inicial do banco para criar as tabelas de `person`, `blood_bags` e `donations`

#### Screencast

A seguir, o screencast com a apresentação do que foi implementado nesta entrega:

**[Vídeo da entrega 02](https://youtu.be/npM3RlHqVcY?si=utX1xd-HdhkL4oeI)**

---

**Entrega 03 — 19/10**

Nesta entrega, foram implementadas mais duas histórias de usuário do backend da plataforma HemovIA:

* **POOUS03v — Gestão de Cadastro de Hospitais**
* **PI3E6-54 — Cadastro de Usuário e Login**

* **Funcionalidades entregues**

- Cadastro de usuário
- Autenticação de Usuário
- Cadastro de Hospital
- Atualização no Docker Compose para comportar imagem do PGAdmin (SGBD)

Screencasts

* Explicação do código das histórias implementadas:
* **[Vídeo da entrega 03](https://youtu.be/qHIdWBhU4DY)**

Issue/Bug tracker

Issues organizadas e atualizadas pela ferramenta de issues do GitHub:
[Issues](https://github.com/HemovIA/hemovia-backend/issues)

![Print do bug tracker](CAMINHO_DA_IMAGEM)

---

### Entrega 04

#### Histórias implementadas

##### [POOUS13 — Gestão do Cadastro de Hemocentros](https://github.com/HemovIA/hemovia-backend/issues/64)

**Cartão:** Como administrador, quero cadastrar, consultar, atualizar e excluir hemocentros com sua localização para manter o cadastro operacional e preservar o histórico.

**Conversa:** O cadastro registra nome, cidade, latitude e longitude. Usuários autenticados com um papel de negócio podem consultar hemocentros ativos. Somente administradores podem criar, atualizar e excluir. A exclusão preenche `deleted_at` e preserva os vínculos históricos.

**Confirmação:**

- **Dado** um administrador autenticado e dados válidos, **quando** ele cadastra e atualiza um hemocentro, **então** a criação retorna `201` com o ID e a atualização retorna `200`, preservando o ID e gravando os novos dados.
- **Dado** um hemocentro com vínculos existentes, **quando** um administrador o exclui, **então** a API retorna `204`, retira o centro das consultas do cadastro e mantém o registro e seus vínculos no banco.

##### [POOUS04 — Emissão de Requisições Hospitalares](https://github.com/HemovIA/hemovia-backend/issues/6)

**Cartão:** Como profissional de um hospital, quero solicitar hemocomponentes informando o tipo sanguíneo, o fator Rh, o componente, a quantidade, a prioridade e o prazo de entrega para suprir demandas médicas urgentes.

**Conversa:** O hospital solicitante é identificado pela sessão do profissional. Toda nova requisição válida é registrada com status inicial `PENDING`.

**Confirmação:**

- **Dado** um profissional autorizado que solicita quatro bolsas de plaquetas A+ com prioridade alta e prazo de seis horas, **quando** ele envia a requisição a `POST /api/v1/requests`, **então** a API retorna `201`, o ID para acompanhamento e o status `PENDING`.
- **Dado** um pedido com os demais dados válidos, **quando** o profissional envia quantidade nula, zero ou negativa, **então** a API retorna `400` e não grava a requisição.

#### Screencasts

-  **[Vídeo da entrega 04](https://www.youtube.com/watch?v=d44n4RsEXDw)**
-  **[Explicação do código](https://www.youtube.com/watch?v=TQqrf7Rikus)**

## Desenvolvimento local

*(seção detalhada a partir da segunda entrega)*

### Organização do backend

As cinco camadas ficam sob o pacote `com.hemovia.api`.

| Camada | Responsabilidade |
|---|---|
| `controller` | Rotas HTTP, tratamento de erros, configuração do Swagger e segurança da sessão HTTP. |
| `service` | Casos de uso, regras de aplicação, transações e serviços de autenticação. |
| `dto` | Dados de entrada, de saída e de troca interna, separados dos modelos persistidos. |
| `repository` | Contratos de persistência, repositórios Spring Data JPA, adaptadores e mapeadores. |
| `model` | Modelos de negócio, entidades JPA em `model.entity`, enums, objetos de valor e algoritmos. |

Um controller recebe e devolve DTOs e chama o serviço. O serviço coordena a operação e usa o modelo e os repositórios. O ArchUnit verifica a distribuição do código e as dependências entre as camadas.

- Agrupe os casos de uso em `service/usecase/<área>`. Use uma classe por operação com regras próprias, como `CreateHospital`. Agrupe as operações de um mesmo cadastro em classes como `ManageBloodCenters`.
- Use `record` para os DTOs. O repositório pode receber dados internos em `dto.internal`. Os DTOs HTTP ficam em `dto.request` e `dto.response`.
- Use as interfaces de persistência, relógio e autenticação para separar os casos de uso de dependências externas.
- Os planos de execução em `.spec` registram o trabalho de cada entrega. Para localizar os arquivos no checkout atual, consulte o `tasks.md` da história.

### Modelo persistente: entidades base

O modelo do ciclo de hemocomponentes é persistido em PostgreSQL com UUIDs, enums gravados por nome, datas civis em `LocalDate` e instantes operacionais em `Instant`. A documentação completa de entidades, associações, unidades e limites está em [modelo-entidades.md](.spec/features/finalizando-entidades-base/modelo-entidades.md).

Compatibilidade dos contratos atuais:

- Uma doação expõe `bloodBags` como coleção completa. O campo legado `bloodBag` só é preenchido quando há exatamente uma bolsa; para mais de uma ele é `null`.
- `donorId` recebido no cadastro legado de bolsa é resolvido para `Person`; um ID inexistente falha e não cria uma bolsa sem rastreabilidade.
- Respostas públicas de autenticação e de domínio usam DTOs e não incluem senha; entidades JPA não são retornadas diretamente.

Para a verificação de aceitação em PostgreSQL isolado, com dependências Maven já em cache, execute:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/spec-java/test-postgres.ps1 -Verify
node <diretorio-da-skill-onp-spec-driven>/scripts/onp-spec.mjs verify finalizando-entidades-base
node <diretorio-da-skill-onp-spec-driven>/scripts/onp-spec.mjs audit --ci
```

Os dois últimos comandos só devem ser considerados aprovados quando terminarem com exit code `0`; veja as limitações e evidências reais em [resultado-validacao.md](.spec/features/finalizando-entidades-base/resultado-validacao.md).

### Documentação interativa da API

Com a aplicação iniciada, a interface Swagger UI fica em [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) e o documento OpenAPI em [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs). A documentação é pública; isso não altera a autenticação das operações de negócio. O endpoint `/auth/me` exige JWT e as outras rotas mantêm a política atual descrita no próprio documento.

#### Pré-requisitos e inicialização local

- JDK 21 e Docker com Docker Compose.
- PostgreSQL 16, iniciado pelo Compose deste projeto, e variáveis de conexão preenchidas em `api/.env` a partir de `api/.env.example`.
- Flyway habilitado; a aplicação aplica as migrações no startup. Dados legados incompatíveis podem interromper o startup com diagnóstico; Swagger não corrige nem transforma esses dados.

No PowerShell, a partir da raiz do repositório:

```powershell
Copy-Item api/.env.example api/.env
# Preencha as variáveis POSTGRES_*, JWT_SECRET (mínimo 32 caracteres) e ADMIN_* em api/.env.
# Para HTTP local, defina COOKIE_SECURE=false.
docker compose --env-file api/.env -f api/compose.yaml up -d postgres
Set-Location api
.\mvnw.cmd spring-boot:run
```

Para habilitar explicitamente a documentação, defina `HEMOVIA_API_DOCS_ENABLED=true`; esse também é o padrão. Para desabilitá-la, defina `HEMOVIA_API_DOCS_ENABLED=false`; a mesma opção desabilita `/v3/api-docs`, sua configuração e a Swagger UI.

#### Autenticação no Swagger

1. Configure `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_CPF` e `ADMIN_PASSWORD` para criar o primeiro administrador na inicialização.
2. Execute `POST /api/v1/auth/login` com `identifier` (e-mail ou CPF) e `password`. O navegador recebe os cookies HttpOnly de sessão.
3. Consulte `/api/v1/auth/me`. As rotas de negócio exigem sessão e papel autorizado; novos usuários são criados pelo administrador em `/api/v1/users`.
4. Antes de escritas, consulte `/api/v1/auth/csrf` e envie o valor do cookie `XSRF-TOKEN` no header `X-XSRF-TOKEN`.

A autenticação usa cookies do navegador; o botão **Authorize** não é usado para colar JWT. Em HTTP local, configure `COOKIE_SECURE=false`. O servidor OpenAPI já contém `/api/v1`.

Não use dados pessoais ou credenciais reais nos exemplos. O documento reflete os controllers e DTOs existentes; ele não cria endpoints para entidades sem controller.

#### Cancelamento de requisições hospitalares — POOUS10

Um profissional com papel `HOSPITAL` pode cancelar uma requisição do próprio hospital em `PENDING` ou `ALLOCATED` com `POST /api/v1/requests/{id}/cancel`, sem corpo. A resposta `200` contém `id` e `status: CANCELLED`. O cancelamento libera as bolsas reservadas e preserva a alocação e seus itens como histórico: a alocação fica `CANCELLED`, os itens ficam `RELEASED` com o horário de liberação e as bolsas voltam a `AVAILABLE`.

Para testar no Swagger, siga a autenticação acima, abra a operação de cancelamento, informe o ID da requisição e preencha o header `X-XSRF-TOKEN` com o valor do cookie `XSRF-TOKEN`. Pedidos em trânsito, entregues ou já cancelados retornam `422`; requisições inexistentes ou de outro hospital retornam `404`.

O cancelamento e a alocação usam o mesmo bloqueio da requisição. A liberação acontece em uma única transação; uma falha de gravação desfaz todas as alterações. [História e critérios de aceite](https://github.com/HemovIA/hemovia-backend/issues/12).

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

Documento completo com todas as histórias do backlog (POO, AED, EST, SO e RSD — mínimo 7 aqui apresentadas): **[link para o documento de histórias no GitHub]**

#### Protótipo (Lo-Fi — Figma)

- Protótipo Lo-Fi cobrindo no mínimo 5 histórias: **[colar link do Figma aqui]**
- Screencast explicando cada história implementada no protótipo: **[YouTube](https://youtu.be/6ujUBhdNyCA?is=7e5ZPCPDCTEUPSc6)**

---

## Deploy no Render

O arquivo [`render.yaml`](render.yaml) configura o serviço Docker do Render para usar `api/Dockerfile` e `api/` como contexto de build. Para criar o serviço, crie um Blueprint no Render apontando para este repositório (selecione a branch que deseja publicar, como `develop`) e informe as variáveis solicitadas:

- `POSTGRES_HOST`, `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD`: dados de conexão de um PostgreSQL acessível pelo serviço.
- `JWT_SECRET`: segredo forte e exclusivo para assinatura dos tokens.

O Render fornece a variável `PORT` automaticamente; a API já a utiliza. Para um serviço Render já criado sem Blueprint, configure **Dockerfile Path** como `api/Dockerfile` e **Docker Context** como `api` nas configurações de build. Não use a raiz do repositório como contexto, pois o Dockerfile copia arquivos relativos à pasta `api`.

---

## Rotas na rede cadastrada — AEDUS04 e POOUS07

O [OSRM](https://project-osrm.org/) fornece trajetos de ruas com dados do [OpenStreetMap](https://www.openstreetmap.org/copyright). Nosso Dijkstra escolhe a sequência de ligações cadastradas com menor tempo total. Defina as coordenadas dos hemocentros e hospitais antes de preparar a malha.

1. Autentique-se como `ADMIN`, obtenha o CSRF e cadastre o ponto de origem em `POST /api/v1/route-graph/nodes`:

```json
{"name":"Origem","bloodCenterId":"<id-do-hemocentro>"}
```

2. Cadastre o destino na mesma operação:

```json
{"name":"Destino","hospitalId":"<id-do-hospital>"}
```

Ao vincular um cadastro, omita `latitude` e `longitude`: o servidor copia as coordenadas do recurso. Para um cruzamento, envie `name`, `latitude` e `longitude`, sem os campos de vínculo. Guarde os IDs dos pontos retornados; são diferentes dos IDs do hospital e do hemocentro.

3. Cadastre a ligação em `POST /api/v1/route-graph/edges`, usando os IDs dos pontos:

```json
{"fromNodeId":"<id-do-ponto-de-origem>","toNodeId":"<id-do-ponto-de-destino>"}
```

O servidor consulta o OSRM para obter tempo, distância e geometria. Não informe pesos manualmente. Cada ligação é direcional; cadastre também a ligação inversa se precisar da viagem de volta. Use os GETs de `/route-graph/nodes` e `/route-graph/edges` para consultar a malha.

4. Autentique-se como `HEMOCENTRO` vinculado à origem e envie uma prévia a `POST /api/v1/routes/calculate`:

```json
{
  "hospitalId":"<id-do-hospital>",
  "component":"RED_BLOOD_CELLS",
  "deliveryDeadline":"2030-12-31T18:00:00Z"
}
```

Substitua o prazo pelo instante futuro do seu teste. A prévia não grava rota nem reserva estoque. Nas operações POST, envie o cookie da sessão e o header `X-XSRF-TOKEN`, conforme as instruções de autenticação no Swagger.

5. Com uma requisição pendente do hospital e estoque compatível, execute `POST /api/v1/requests/{id}/allocate`. A resposta mantém os campos anteriores e acrescenta `route`, com ID da rota, vértices, distância em quilômetros, duração em segundos e geometria GeoJSON. Se não houver caminho ou a chegada ultrapassar o prazo, retorna `422 INVALID_ROUTE` e desfaz a reserva inteira. O cancelamento preserva o histórico da rota.

O GeoJSON usa pares `[longitude, latitude]`; o perfil considerado é o de carro. Os custos e trajetos são snapshots dos trechos consultados no cadastro, sem atualização automática ou trânsito em tempo real. A seleção é ótima dentro da rede cadastrada; não pesquisa todas as ruas fora dela. Esta entrega oferece cadastro e listagem da malha e mantém a alocação restrita a hemácias.

O endereço do serviço é configurável por `ROUTING_BASE_URL`. O servidor público de demonstração é destinado a uso não comercial razoável, limita consultas a uma por segundo e não oferece garantia de disponibilidade. [Política do servidor](https://github.com/Project-OSRM/osrm-backend/wiki/Demo-server). Falha do provedor no cadastro retorna `503 ROUTING_PROVIDER_UNAVAILABLE`, sem ligação parcial.

O limite de plaquetas na prévia é a regra didática do backlog: 60 minutos por padrão, configurável em segundos por `ROUTING_PLATELETS_MAX_SECONDS`. O transporte reutiliza a rota associada à alocação, conforme o fluxo abaixo.

## Transporte e entrega — POOUS09 e POOUS11

Pré-requisito: uma requisição alocada com rota, conforme o roteiro anterior. A viagem transporta o lote inteiro de uma alocação. A frota é compartilhada, e a capacidade de cada veículo é medida em bolsas.

1. Como `ADMIN`, cadastre um veículo em `POST /api/v1/vehicles`:

```json
{"code":"HEMOVIA-01","capacity":20}
```

Guarde o `id` retornado. `GET /api/v1/vehicles` lista a frota por código e está disponível aos quatro papéis de negócio. O código é único e a capacidade precisa ser positiva.

2. Como `HEMOCENTRO` vinculado à origem da alocação, envie `POST /api/v1/transports/start`:

```json
{"allocationId":"<id-da-alocacao>","vehicleId":"<id-do-veiculo>"}
```

A resposta `201` contém o `id` do transporte, `allocationId`, `requestId`, `routeId`, `vehicleId`, `status: IN_TRANSIT`, `startedAt` e `completedAt: null`. O pedido, a alocação, os itens e as bolsas passam juntos para `IN_TRANSIT`. A rota existente é preservada; sua janela de chegada é validada novamente no instante da saída. Não é feita outra consulta ao OSRM.

Veículo ocupado retorna `409 VEHICLE_UNAVAILABLE`. Capacidade insuficiente, pedido sem alocação válida, lote inconsistente ou viagem repetida retornam `422`; rota ausente ou prazo inviável retorna `422 INVALID_ROUTE`. A operação não modifica parcialmente o estoque. Uma requisição em trânsito não pode ser cancelada.

3. Como `HOSPITAL` vinculado ao destino, envie `POST /api/v1/transports/{id}/complete`, usando o ID do transporte, sem corpo.

A resposta `200` mantém os identificadores e `startedAt`, passa o status para `DELIVERED` e registra `completedAt` em UTC. Pedido, alocação, itens e bolsas ficam `DELIVERED`. O veículo pode iniciar outra viagem; as bolsas entregues não voltam ao estoque disponível. Uma segunda confirmação ou uma viagem não iniciada retorna `422`.

Em todos os POSTs, envie o cookie da sessão e o header `X-XSRF-TOKEN` obtido em `GET /api/v1/auth/csrf`. Ao trocar de usuário, use o CSRF da sessão atual. Papel incompatível retorna `403`; alocação de outro hemocentro ou transporte de outro hospital retorna `404`.

Este fluxo registra saída e recebimento. Ingestão de GPS, temperatura e alertas será desenvolvida nas histórias de telemetria. A migração V16 impede duas viagens em trânsito no mesmo veículo, preservando o histórico de viagens encerradas.

## Estatísticas de estoque e demanda — ESTUS01 e ESTUS02

Autentique-se como `ADMIN` ou `HEMOCENTRO` no Swagger. Os dois endpoints abaixo consultam a rede inteira com o cookie da sessão; são GETs e não exigem o header CSRF. `HOSPITAL` e `TRANSPORTADOR` recebem `403`, e sessão ausente recebe `401`.

1. Consulte `GET /api/v1/statistics/inventory`.

O resumo retorna `totalBags`, a data UTC de referência e três distribuições: `byBloodType` (oito tipos ABO/Rh), `byComponent` (cinco componentes) e `byBloodTypeAndComponent` (quarenta combinações). Cada uma contém contagens e `percentage` entre 0 e 100. Categorias sem bolsas também aparecem com zero.

Para um teste simples, cadastre três bolsas disponíveis em um hemocentro ativo: uma O+ de hemácias, uma A+ de plaquetas e uma AB- de plasma, todas com validade posterior ao dia UTC atual. Use `POST /api/v1/blood-bags`, informando `bloodCenterId`, `bloodTypeEnum`, `rhFactor`, `component`, `status`, `collectionDate` e `expirationDate`. Com apenas essas bolsas elegíveis, o total será 3. Os percentuais são rateados para somar 100,00% em cada distribuição: uma divisão em três partes iguais aparece como 33,34%, 33,33% e 33,33%, com desempate pela ordem dos enums.

O estoque considera apenas bolsas `AVAILABLE`, sem reserva ativa, de centros ativos e com validade posterior a hoje UTC. Bolsas que vencem hoje, bolsas sem hemocentro, reservadas, em trânsito, entregues ou descartadas não entram no resumo. A consulta não muda esses estados. Se não houver estoque elegível, as categorias permanecem no JSON com contagens e percentuais zero.

2. Consulte `GET /api/v1/statistics/demand`. Opcionalmente, informe `from` e/ou `to` no Swagger, usando instantes como `2026-11-01T00:00:00Z` e `2026-12-01T00:00:00Z`, ajustados ao seu teste.

O período considera o instante de emissão (`createdAt`): `from` é inclusivo e `to` é exclusivo. Sem limites, consulta todo o histórico. Cada requisição não cancelada é uma observação, inclusive pedidos ainda pendentes; requisições `CANCELLED` são excluídas. `from >= to` e datas malformadas retornam `400`.

Emita três requisições, de 10, 20 e 30 bolsas, pelos profissionais dos hospitais A, B e C conforme o fluxo da POOUS04. Para um período que contenha somente essas emissões, `overall` retorna:

| Campo | Resultado |
|---|---:|
| `requestCount` | 3 |
| `totalBags` | 60 |
| `mean` e `median` | 20,00 |
| `populationVariance` | 66,67 |
| `populationStandardDeviation` | 8,16 |
| `sampleVariance` | 100,00 |
| `sampleStandardDeviation` | 10,00 |

`byHospital` traz as mesmas métricas separadas por hospital. Se um hospital emitir várias requisições, cada uma mantém seu peso no cálculo; o total por hospital não substitui as observações individuais. Hospitais sem requisições no período não aparecem nessa lista.

As métricas populacionais dividem por `n`; as amostrais, por `n - 1`. Médias, medianas e desvios são medidos em bolsas; variâncias, em bolsas ao quadrado. Os decimais têm duas casas. Com menos de duas requisições, as dispersões são zero e `insufficientSample: true` vem acompanhado de `warning`. No histórico vazio, todas as métricas são zero e `byHospital` é uma lista vazia. Essa convenção evita divisões por zero e não indica erro na consulta.

---

## Equipe

### Membros atuais

| Nome | E-mail |
|---|---|
| Alice Sena | [amsp@cesar.school](mailto:amsp@cesar.school) |
| Abraão Santos | [afs6@cesar.school](mailto:afs6@cesar.school) |
| Cecília Lopes | [cvls@cesar.school](mailto:cvls@cesar.school) |
| Matheus | [mngv@cesar.school](mailto:mngv@cesar.school) | 
| Emily Raquel |  [emrs@cesar.school](mailto:emrs@cesar.school) |
| Vinicius Wagner | [vwgg@cesar.school](mailto:vwgg@cesar.school) |
