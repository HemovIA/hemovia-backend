# Spec: Documentação da API com Swagger

> feature: documentacao-swagger
> status: em-implementacao

## Contexto

Disponibilizar documentação interativa da API Hemovia, gerada a partir dos controllers e DTOs reais, para consultar contratos e experimentar chamadas pelo Swagger UI. A documentação será acessível sem login e poderá ser desativada por configuração, conforme escolha explícita do usuário em 22/10/2026. A autenticação das operações existentes continua a mesma.

O inventário atual contém 14 operações: POST /auth/register, POST /auth/login, GET /auth/me, POST /hospitals, GET/POST /donations, GET/PUT/DELETE /donations/{id}, GET/POST /blood-bags e GET/PUT/DELETE /blood-bags/{id}. Entidades sem controller não ganham endpoints nesta feature.

## Histórias

### US-010 — Consultar a documentação interativa

Como desenvolvedor consumidor da API, quero abrir a documentação no navegador e obter seu documento OpenAPI para conhecer e testar os serviços existentes.

#### AC-016 — Swagger UI e OpenAPI disponíveis quando habilitados

- **Dado** o backend iniciado com documentação habilitada
- **Quando** um consumidor sem token acessa /swagger-ui.html, seus recursos estáticos e /v3/api-docs
- **Então** a interface e sua configuração são carregadas, o JSON OpenAPI é gerado com sucesso e informa título Hemovia API, versão da API e descrição em português
- **E** o Swagger usa o documento da aplicação, sem exemplo Petstore, e sua configuração permite experimentar operações com dados informados pelo consumidor.

#### AC-017 — Documentação pode ser desativada

- **Dado** a opção HEMOVIA_API_DOCS_ENABLED=false
- **Quando** a aplicação inicia e são consultados /v3/api-docs, /v3/api-docs/swagger-config e /swagger-ui.html
- **Então** esses acessos não disponibilizam o documento nem a interface da aplicação
- **E** a mesma aplicação mantém seus endpoints de negócio e regras de autenticação operantes; o README explica como habilitar e desabilitar a documentação.

### US-011 — Entender os contratos implementados

Como desenvolvedor de integração, quero consultar operações, modelos, exemplos e respostas para montar requisições compatíveis com o backend.

#### AC-018 — Todas as operações atuais têm descrição e respostas coerentes

- **Dado** os quatro controllers atuais de autenticação, hospitais, doações e bolsas
- **Quando** o documento OpenAPI é gerado pela aplicação real
- **Então** contém as 14 operações inventariadas, agrupadas por domínio, com resumo em português, identificador único, parâmetros, corpo quando aplicável e respostas de sucesso correspondentes ao controller (200, 201 ou 204)
- **E** respostas de erro documentadas correspondem aos handlers/comportamentos efetivos; respostas 204 não anunciam corpo; /actuator/**, /error e endpoints inexistentes de entidades não aparecem como operações de negócio.

#### AC-019 — Modelos e exemplos representam os contratos atuais

- **Dado** os DTOs atuais, suas validações e a compatibilidade de doações com várias bolsas
- **Quando** são inspecionados os schemas e exemplos do OpenAPI
- **Então** tipos, campos obrigatórios, UUIDs, enums, datas e unidades correspondem ao contrato JSON; exemplos usam dados sintéticos e códigos aceitos como POSITIVE e WHOLE_BLOOD
- **E** donation.bloodBags é uma coleção, bloodBag singular é descrito como nulo para zero ou várias bolsas, vínculos desconhecidos continuam opcionais, e os cenários alternativos de criar doação com bloodBagId ou criar uma nova bolsa são explicados sem impor novos campos obrigatórios ao payload legado
- **E** as descrições diferenciam volume em mL, coordenadas e prioridade; exemplos com validade futura são testados com relógio/datas adequados e não viram dados de produção.

### US-012 — Usar autenticação pelo Swagger sem mudar a API

Como consumidor autenticado, quero fornecer meu JWT no Swagger para chamar operações protegidas e entender quais endpoints precisam de token.

#### AC-020 — JWT e credenciais documentados conforme a segurança real

- **Dado** a documentação habilitada e a política de acesso atual
- **Quando** o consumidor consulta a especificação e usa autenticação Bearer
- **Então** há um esquema HTTP Bearer JWT para o botão Authorize, /auth/me exige esse esquema, registro/login permanecem públicos e os demais endpoints não são marcados como protegidos se hoje são públicos
- **E** o parâmetro interno @AuthenticationPrincipal não vira corpo/parâmetro do usuário; entidades User e campos internos de credenciais não são expostos como schemas de resposta; password em registro/login é documentado somente como entrada com formato password, sem token real ou senha de ambiente nos exemplos
- **E** chamadas reais preservam o comportamento atual com token válido, ausente e inválido; a documentação não altera a autorização dos endpoints.

### US-013 — Manter documentação e implementação verificáveis

Como mantenedor, quero testar o documento gerado e ter instruções reproduzíveis para impedir que a documentação se afaste da API.

#### AC-021 — Testes reais, instruções de uso e auditoria reproduzível

- **Dado** a suíte atual e os novos testes de documentação
- **Quando** o gate completo é executado com PostgreSQL isolado
- **Então** testes HTTP exercitam geração real do OpenAPI, recursos da UI, habilitação/desabilitação, inventário, schemas e segurança; cada critério tem métodos JUnit obrigatórios no manifesto e prova recente por caso, sem skips ou aprovação só por presença de anotações
- **E** todos os testes anteriores continuam passando; o README traz URLs, configuração, fluxo login/Authorize e pré-requisitos reais do backend, incluindo Flyway sem prometer que Swagger resolve dados legados incompatíveis
- **E** verify das três features e audit --ci encerram com código 0; o relatório mecânico é salvo em .spec/features/documentacao-swagger/auditoria.md e resultados/limites são registrados sem inventar aprovação.

## Desenho técnico

Adicionar org.springdoc:springdoc-openapi-starter-webmvc-ui 3.1.1, compatível proposto para Spring Boot 4.1.1, sem downgrade do Spring Boot. Uma OpenApiConfig define informações, esquema Bearer e schemas reutilizáveis necessários aos erros já retornados. Controllers e DTOs recebem anotações de documentação; a geração continua baseada nos mappings reais. Nenhuma resposta, validação de negócio ou migração é reescrita para caber na documentação.

Aplicar HEMOVIA_API_DOCS_ENABLED às duas propriedades springdoc.api-docs.enabled e springdoc.swagger-ui.enabled, habilitadas por padrão. Não persistir autorização do Swagger no navegador por padrão. Restringir o inventário aos quatro controllers atuais e não habilitar exposição de repositórios, actuator ou integrações MCP. A versão OpenAPI da aplicação não deve ser confundida com a versão da biblioteca.

Testes de contexto usam a configuração isolada já existente e PostgreSQL descartável. Usar geração OpenAPI real com o contexto web e filtros de segurança, incluindo um contexto separado com documentação desabilitada. Mockito pode simular serviços em testes de contrato específicos, sem substituir o documento por JSON estático. Adaptar testes da UI aos redirects válidos e verificar também recursos/configuração efetivamente utilizados.

A escolha técnica parte da documentação oficial https://springdoc.org/ consultada em 22/10/2026. Compatibilidade de dependências e comportamento do artefato fixado serão comprovados na implementação; se houver incompatibilidade, registrar a causa sem inventar PASS nem baixar a versão do backend silenciosamente.

## Definição de pronto

Todos os critérios possuem testes anotados @spec:AC-xxx e provas PASS provenientes de JUnit/Surefire recente. Os testes não são enfraquecidos e a suíte completa continua no gate. A documentação é utilizável por HTTP, explica os contratos atuais e não altera os endpoints. Arquivos de produção alterados/criados são mapeados às tarefas e ao escopo de fontes. Tarefas só são concluídas após prova; a feature só recebe status auditada com audit --ci exit 0. Esta etapa cria a especificação e o plano; ainda não executa a implementação.

## Fora de escopo

- Novos endpoints para as entidades de requisição, estoque ou logística; CRUDs inexistentes.
- Alterar autenticação/autorização de negócio, login OAuth ou gestão de usuários.
- Documentar arquitetura inteira, C/AED e telas dentro do Swagger.
- Corrigir dados de bancos pessoais, alterar V1–V7 ou rodar migrações no banco do usuário.
- Reescrever contratos de erro/validação, inserir dados de exemplo reais ou publicar a aplicação.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-005 | A documentação Swagger cobre a API HTTP implementada, seus DTOs e instruções de uso; não cria endpoints a partir das entidades JPA. | confirmada | Usuário confirmou o plano dos 14 endpoints atuais em 22/10/2026. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-004 | Como disponibilizar Swagger UI e OpenAPI? | respondida | Usuário escolheu acesso sem login e opção de desativar por configuração em 22/10/2026. |
| Q-005 | Quais tarefas paralelizar e qual modelo/esforço executar? | respondida | Usuário escolheu executar as três tarefas em sequência, sem paralelismo, com gpt-5.6-terra/medium em todas, em 22/10/2026. |
