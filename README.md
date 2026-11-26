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

### Especificações

As especificações e tarefas de cada funcionalidade ficam em [`.spec/features`](.spec/features/). As provas dos critérios de aceite ficam em [`.spec/verification`](.spec/verification/).

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

### Cadastro público de doadores

Na tela de login, selecione **Criar conta** e informe um e-mail. O link recebido fica válido por 30 minutos. Após abri-lo, informe nome, CPF e senha. A conta é criada com o papel `DOADOR` e pode entrar pelo mesmo formulário de login.

Quando CPF e e-mail confirmado correspondem a um cadastro de doador, a nova conta é vinculada a esse cadastro. Se o CPF já existir e o e-mail estiver ausente ou divergente, a pessoa precisa atualizar seus dados com o hemocentro. Para CPF sem cadastro de doador, a conta é criada sem vínculo.

Para enviar por um provedor SMTP, configure `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` e `REGISTRATION_EMAIL_FROM` no ambiente da API. A autenticação SMTP e o STARTTLS vêm habilitados por padrão. Em produção, defina `REGISTRATION_FRONTEND_URL` com a URL HTTPS do frontend. A configuração local está nos arquivos `.env.example`.

A interface obtém a proteção CSRF em `GET /api/v1/auth/csrf` e envia `X-XSRF-TOKEN` ao solicitar `POST /api/v1/auth/register` e concluir `POST /api/v1/auth/register/confirm`. Os endpoints respondem com `202` e `201`, respectivamente.

#### Testar o cadastro com Mailpit

O Compose de desenvolvimento em `api/compose.yaml` captura os e-mails na [caixa de entrada local](http://127.0.0.1:8025). Abra o [frontend local](http://localhost:5173/), selecione **Criar conta** e use um endereço de teste. O link de confirmação aparece no Mailpit.

- **API executada na máquina**. Inicie o banco e o Mailpit com `docker compose --env-file api/.env -f api/compose.yaml up -d postgres mailpit`. Use em `api/.env` os valores de Mailpit definidos em `api/.env.example`. O SMTP local é `127.0.0.1:1025`.
- **API executada no Compose**. Suba os serviços com `docker compose --env-file api/.env -f api/compose.yaml up -d --build`. A API usa `mailpit:1025` na rede do Compose.

Se o frontend estiver em outra origem, ajuste `REGISTRATION_FRONTEND_URL` para gerar o link correto e inclua essa origem em `CORS_ALLOWED_ORIGINS`. A caixa de entrada e a porta SMTP são acessíveis pela máquina local.

### Documentação interativa da API

Com a aplicação iniciada, a interface Swagger UI fica em [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) e o documento OpenAPI em [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs). A documentação é pública; isso não altera a autenticação das operações de negócio. O endpoint `/auth/me` exige JWT e as outras rotas mantêm a política atual descrita no próprio documento.

#### Pré-requisitos e inicialização local

- JDK 21 e Docker com Docker Compose.
- PostgreSQL 16, iniciado pelo Compose deste projeto, e variáveis de conexão preenchidas em `api/.env` a partir de `api/.env.example`.
- Flyway habilitado; a aplicação aplica as migrações no startup. Dados legados incompatíveis podem interromper o startup com diagnóstico; Swagger não corrige nem transforma esses dados.

No PowerShell, a partir da raiz do repositório:

```powershell
if (-not (Test-Path api/.env)) { Copy-Item api/.env.example api/.env }
# Preencha as variáveis POSTGRES_*, JWT_SECRET (mínimo 32 caracteres) e ADMIN_* em api/.env.
# Para HTTP local, defina COOKIE_SECURE=false.
docker compose --env-file api/.env -f api/compose.yaml up -d postgres mailpit
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

## Deploy da demo na AWS

A demonstração roda em uma instância Lightsail em `us-east-2`. O GitHub Actions publica o frontend e a API em uma mesma versão, identificada pelos dois commits. Para publicar, os dois commits da `main` precisam ter passado na CI.

1. No repositório do backend, abra **Actions → Deploy AWS Demo → Run workflow**, selecione `main` e inicie a publicação.
2. Confira os commits no resumo do workflow e o sucesso da verificação de saúde. Abra a [demo do Hemovia](https://18.216.26.95/) e o [Grafana](https://18.216.26.95/grafana/).
3. Para voltar à versão anterior, abra **Actions → Rollback AWS Demo → Run workflow** e selecione `main`. A recuperação usa a versão anterior guardada na VM e preserva os volumes do Compose.

O workflow usa credenciais temporárias do IAM por OIDC. A stack [AWS CI/CD](deploy/lightsail/aws-cicd.json) define a função, o bucket privado e o documento Systems Manager. As variáveis do repositório do backend são `HEMOVIA_AWS_ROLE_ARN`, `HEMOVIA_RELEASE_BUCKET`, `HEMOVIA_SSM_DOCUMENT` e `HEMOVIA_SSM_NODE`. A VM executa o script [release.sh](deploy/lightsail/release.sh) e usa o `.env` do ambiente.

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

### Consultas operacionais

Com sessão autenticada, `GET /api/v1/requests`, `/api/v1/transports` e `/api/v1/hospitals` retornam `content`, `page`, `size`, `totalElements` e `totalPages`. Os detalhes usam `GET` no mesmo caminho com `/{id}`. A paginação começa em zero, usa 20 itens por padrão e aceita tamanhos de 1 a 100. A ordenação é data de criação decrescente para requisições, início decrescente para transportes e nome crescente para hospitais; empates usam identificador crescente.

Requisições aceitam `status`, `hospitalId`, `bloodType`, `rhFactor`, `component` e `priority`; transportes aceitam `status`, `requestId` e `vehicleId`; hospitais aceitam trechos `name` e `city` sem distinção entre maiúsculas e minúsculas. Os filtros são combinados entre si e com o vínculo da sessão antes da contagem e paginação. ADMIN vê todos os recursos; HOSPITAL vê seu hospital e os pedidos e viagens vinculados; HEMOCENTRO vê hospitais, pedidos pendentes ou que alocou e viagens de suas alocações; TRANSPORTADOR vê viagens de seu veículo e pedidos associados, sem acesso ao cadastro de hospitais. Detalhes fora do vínculo retornam 404. As consultas usam rota persistida, sem calcular nova rota.

## Observabilidade da API e infraestrutura

A demonstração usa a API Java com dados sintéticos. O Compose provisiona Prometheus, Grafana, Loki, Tempo, Alloy, Node Exporter e cAdvisor. O diagrama e as provas datadas estão no [relatório de observabilidade](docs/observabilidade/relatorio.pdf) e em [evidências](docs/observabilidade/evidencias/).

Na raiz do backend, copie `.env.example` para `.env`, configure PostgreSQL, `JWT_SECRET`, os campos `ADMIN_*` e credenciais administrativas do Grafana. Defina também `GRAFANA_VIEWER_USER` e `GRAFANA_VIEWER_PASSWORD` no ambiente local e crie a conta com papel Viewer em **Administration → Users and access → Users**. Guarde as credenciais no arquivo local ignorado pelo Git.

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
# Edite .env com valores próprios antes de iniciar.
docker compose up -d --build
node --env-file=.env scripts/observabilidade/carga.mjs --loop
```

A carga cria dados sintéticos e permanece em execução até Ctrl+C. Confira `docker compose ps`, o probe [http://localhost:18080/readyz](http://localhost:18080/readyz), os alvos [http://localhost:19090/targets](http://localhost:19090/targets) e o Grafana em [http://localhost:13000](http://localhost:13000). Os serviços publicados pelo Compose escutam apenas em localhost. A porta de gerenciamento 8081 é interna; Prometheus consulta `/actuator/prometheus` na rede Docker. Consulte `http_server_requests_seconds_count`, `http_server_requests_seconds_bucket`, `jvm_memory_used_bytes`, `hikaricp_connections_active`, `hemovia_doacoes_registradas_total` e `hemovia_requisicoes_emitidas_total` no Prometheus. Os dashboards provisionados são **Hemovia — Aplicação** e **Hemovia — Infraestrutura**.

Para conferir o endpoint interno a partir do contêiner da API, execute:

```powershell
docker compose exec -T api wget -qO- http://127.0.0.1:8081/actuator/prometheus
```

O health check da API no Docker Compose usa `/readyz`. Na demo da AWS, o script de publicação verifica esse endpoint localmente na VM antes de confirmar a versão.

No Grafana, entre com a conta Viewer e abra Explore com Loki. A consulta `{application="hemovia-api"} |= "Requisicao de sangue processada"` encontra eventos da carga; abra o campo derivado `traceId` para consultar o mesmo trace no Tempo. Para diagnóstico operacional, use `docker compose logs api`, `docker compose logs alloy` e `docker compose logs prometheus`. Não inclua credenciais nas evidências.

A publicação para a correção depende de definir a URL HTTPS, a janela de disponibilidade e a entrega da credencial Viewer em canal privado. Encaminhe apenas o Grafana e valide o acesso de outra rede durante a janela combinada.

Para verificar a entrega local após configurar a conta Viewer:

```powershell
$env:HEMOVIA_OBS_VERIFY='true'
node --env-file=.env .agents/skills/onp-spec-driven/scripts/onp-spec.mjs verify observabilidade
```

### Pré-requisitos e portas da demonstração

Use Docker Engine Linux rootful ou Docker Desktop com contêineres Linux, Docker Compose, Node.js 22 e Python com `pypdf` para a verificação documental. Caso o Python com `pypdf` não esteja no PATH, configure `$env:HEMOVIA_PDF_PYTHON='C:\caminho\para\python.exe'` antes do comando de verificação.

| Serviço | Porta interna | Acesso publicado local |
|---|---:|---|
| API / gerenciamento | 8080 / 8081 | 18080 / não publicado |
| PostgreSQL | 5432 | não publicado |
| Prometheus | 9090 | 19090 |
| Grafana | 3000 | 13000 |
| Loki | 3100 | não publicado |
| Tempo HTTP / OTLP | 3200 / 4318 | não publicado |
| Node Exporter | 9100 | rede do host Docker |
| cAdvisor | 8080 | não publicado |
| Alloy | 12345 | não publicado |
