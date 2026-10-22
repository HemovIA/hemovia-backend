# Relatório final — revisão do projeto

> Consolidação T-004 / critério de aceite AC-004. Este documento consolida os
> três levantamentos, sem corrigir o produto e sem inspecionar arquivos `.env`.

## Referência, escopo e limites da conclusão

- **Commit do produto analisado:** `91d1f981e8860df0cb53fa9472edd8f1bcd91e89`.
- **Commit de bootstrap da revisão:** `33c3420` (referência completa registrada
  em [execucao.md](execucao.md)).
- **Estado considerado:** o código do produto não mudou em relação ao commit de
  produto; há alterações locais em andamento, restritas aos artefatos desta
  revisão. Elas não são evidência de evolução funcional do produto.
- **Fontes consolidadas:** [alinhamento](relatorios/alinhamento.md),
  [API, dados e segurança](relatorios/api-dados-seguranca.md) e
  [qualidade e operação](relatorios/qualidade-operacao.md).

O inventário contém 44 histórias: 4 implementadas estaticamente, 2 parciais,
37 planejadas e 1 sem evidência. As 37 planejadas são backlog/documentação de
um projeto incremental, não defeitos atuais. A história sem evidência é CI/CD; Swagger também está sem evidência na tabela de componentes. Os desencontros de arquitetura declarada, Docker apenas do banco e
instruções operacionais são pendências documentais: precisam de evidência ou de
ajuste da declaração, não provam uma falha de runtime por si sós.

## Diagnóstico

O caminho é adequado para um projeto acadêmico incremental: há um núcleo
coerente de API Spring, persistência, autenticação e exemplos de AED, enquanto
os diagramas deixam explícito um conjunto maior de evoluções planejadas. A
direção deixa de ser adequada apenas se as afirmações atuais de entrega forem
tratadas como se já cobrissem o backlog, a execução integrada ou a operação em
produção. A prioridade é tornar o núcleo executável e verificável antes de
ampliar o escopo.

### Pontos positivos comprovados

- A API possui bootstrap, controladores para autenticação, doações, bolsas e
  hospitais, entidades JPA e serviços; há testes reais de autenticação/JWT e
  de parte da regra de bolsas ([alinhamento](relatorios/alinhamento.md)).
- Há JWT com sessão sem estado, BCrypt, normalização de e-mail/CPF, DTOs com
  `@Valid` e credenciais de banco por variáveis de ambiente, sem valores de
  conexão versionados ([API, dados e segurança](relatorios/api-dados-seguranca.md)).
- As estruturas de lista, fila, pilha/undo e ABB existem em Java e no exemplo
  C, que compila e executa com exit 0 ([qualidade e operação](relatorios/qualidade-operacao.md)).
- A prova Java é parcial porém real: Maven offline com JDK 22 compilou para
  release 21 e executou 17 testes — 16 passaram, 1 errou em `contextLoads` por
  conexão PostgreSQL recusada; suíte **FAIL**, 0 skip. Isso não aprova a API.

### Limitações e pendências documentais

- Docker não estava disponível: Compose, healthcheck, PostgreSQL e migrações
  não foram executados. A duplicidade Flyway é confirmada estaticamente, mas
  não foi reproduzida em banco ativo.
- Os testes Node desta feature verificam artefatos de diagnóstico; não provam
  comportamento Java/C. O motor também aponta para `src/**`, enquanto a API
  está em `api/` (QUA-004).
- A demonstração C executou, mas o undo imprime a cópia local como `RESERVED`,
  em vez de consultar o estado restaurado no estoque; por isso a execução não
  é aprovação semântica completa (QUA-006).
- Swagger/OpenAPI é declarado no README sem dependência/configuração ou teste
  versionado; CI/CD é declarado sem workflow versionado; Docker contém somente
  PostgreSQL; e a arquitetura em camadas observada não corresponde literalmente
  à Clean Architecture declarada. Essas pendências são tratadas no plano como
  documentação/evidência, não como backlog futuro defeituoso.
- `show-sql` habilitado não prova registro de parâmetros pessoais; é risco que
  exige revisão do logger e do perfil. Similarmente, `permitAll` para actuator
  não prova que endpoints sensíveis estejam expostos: a superfície efetiva não
  foi executada nem configurada por perfil. A política DEV declarada no README
  é contexto de desenvolvimento, não evidência de incidente de segurança.

## Plano de ação rastreável

Cada linha contém prioridade, justificativa, dependências e validação. IDs na
mesma célula são aliases do mesmo problema; assim API-001 e QUA-001 não criam
trabalho duplicado.

| Achado(s) | Prioridade | Justificativa | Dependências | Como validar |
| --- | --- | --- | --- | --- |
| API-001, QUA-001 | P0 | Duas migrações `V2` bloqueiam uma sequência Flyway inequívoca. | Definir ordem/migração compatível; PostgreSQL efêmero. | Aplicar Flyway em banco vazio e rodar `contextLoads` com banco acessível. |
| API-002 | P0 | CRUD operacional fica público pela regra final abrangente. | Papéis e política de acesso definidos. | Testes de integração anônimos e autenticados esperam 401/403 e permissões corretas. |
| API-003 | P0 | Fallback de segredo JWT pode ser conhecido em implantação sem configuração. | Cofre/variável obrigatória e rotação coordenada. | Inicialização falha sem segredo; teste confirma segredo externo e rotação planejada. |
| API-004 | P0 | Contrato permite chegar a erro técnico ao criar bolsa incompleta. | Regra de criação de bolsa e DTO/validador. | Testes MVC retornam 400 para ausência condicional e persistem entrada válida. |
| API-006 | P0 | IDs inválidos e recursos ausentes podem retornar 500. | Formato padrão de erro. | Testes MVC verificam 400 para ID inválido e 404 para inexistente. |
| API-005 | P1 | `donorId` obrigatório é descartado, quebrando rastreabilidade contratada. | Decidir associação de doador e repositório/regra. | Teste de serviço confirma associação ou contrato removido e documentado. |
| API-007 | P1 | SQL global é risco de privacidade, sem prova de parâmetros pessoais em log. | Perfis DEV/produção e política de logs/retenção. | Testar perfis e inspecionar saída controlada sem dados pessoais/bind sensível. |
| API-008 | P1 | Persistências compostas podem deixar bolsa órfã em falha posterior. | Fronteira transacional e banco de teste. | Induzir falha na doação e confirmar rollback de ambas as gravações. |
| API-009 | P1 | `permitAll` do actuator tem superfície efetiva não delimitada. | Política DEV e lista explícita de endpoints/perfis. | Executar integração por perfil e testar quais endpoints anônimos respondem. |
| QUA-002 | P1 | Invariantes de AED e limites não têm testes automatizados. | Escolha de framework/target de teste C. | Cobrir vazio, cabeça/meio/fim, FIFO/LIFO, undo sem bolsa e ABB degenerada. |
| QUA-003 | P1 | README não reproduz execução e CI declarada não está versionada. | Decidir CI local/externa e instruções de ambiente. | Seguir README do zero; workflow versionado executa build e testes, ou declaração é corrigida. |
| QUA-004 | P2 | Gate Node documental não cobre fontes Java/C. | Ajuste separado de rastreabilidade e CI. | Configurar fontes reais e comprovar que CI executa Node, Maven e testes C sem apresentar gate documental como funcional. |
| QUA-005 | P1 | A suíte depende de PostgreSQL externo e falhou em 1 de 17 testes. | Perfil de teste ou PostgreSQL efêmero; corrigir P0 Flyway primeiro. | Maven offline com JDK compatível, banco disponível: 17 testes passam, 0 erro, 0 skip. |
| QUA-006 | P2 | Demonstração C comunica estado errado após undo, embora execute. | Ajustar a demonstração para consultar estoque. | Compilar/executar e asserir que `stock_find` mostra `AVAILABLE` após undo. |

### Pendências documentais mapeadas

| ID | Evidência | Encaminhamento no plano |
| --- | --- | --- |
| DOC-001 — arquitetura declarada | README declara Clean Architecture; código observado é controller/service/repository/model. | QUA-003: alinhar declaração ou documentar a arquitetura realmente usada. |
| DOC-002 — Swagger sem evidência | README declara Swagger, mas não há dependência/configuração/teste versionado. | QUA-003: demonstrar contrato OpenAPI ou corrigir a declaração. |
| DOC-003 — Docker só do banco | `api/docker-compose.yml` define PostgreSQL, não serviço/imagem da API. | QUA-003: documentar esse limite ou adicionar instrução de execução combinada. |
| DOC-004 — CI ausente | README declara GitHub Actions; inventário não contém `.github/workflows`. | QUA-003: versionar workflow ou declarar explicitamente que não existe CI local. |

## Conclusão

Há base suficiente para continuar incrementalmente no contexto acadêmico, com
separação útil entre núcleo entregue e extensões planejadas. A próxima etapa
deve primeiro remover os bloqueios P0 e obter prova integrada com PostgreSQL;
depois, elevar cobertura, documentação e automação. Nenhum teste Node desta
consolidação é usado como aprovação funcional de Java, C, Docker ou segurança.

## Verifica??o da entrega

O gate desta revis?o registrou 4/4 crit?rios de aceite com prova PASS e auditoria CI com exit 0. Permanece um aviso: a regra recomendada de segredos aponta para src/**/*.js, sem arquivos correspondentes. Essa lacuna est? mapeada em QUA-004 e n?o foi ocultada nem alterada para aprovar a revis?o. O resultado comprova a entrega do diagn?stico; a su?te funcional Maven continua com 1 erro e PostgreSQL/Docker n?o receberam aprova??o integrada.
