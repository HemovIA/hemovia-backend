# Resultado da implementação — 22/10/2026

A feature `finalizando-entidades-base` implementa o modelo persistente do ciclo completo: 14 entidades JPA, o valor embutido RouteWaypoint, repositórios, enums, migrações V4–V7 e compatibilidade dos endpoints existentes. Não adiciona novos CRUDs nem algoritmos de alocação/roteamento.

## Verificação real

Ambiente: Windows, JDK 22, Maven offline, PostgreSQL 17 em cluster temporário com porta aleatória em 127.0.0.1. Sem carregar o .env pessoal. O cluster foi encerrado no finally; nenhum serviço ou banco pessoal foi alterado.

Comando executado:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-22'
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/spec-java/test-postgres.ps1 -Verify
```

| Suíte | Casos aprovados | Falhas/erros/skips |
|---|---:|---:|
| BaseEntitiesUnitTest | 29 | 0 |
| ExistingContractsRegressionTest | 7 | 0 |
| DonationAssociationsRegressionTest | 4 | 0 |
| MigrationSourcesTest | 1 | 0 |
| BaseEntitiesPersistenceIT, PostgreSQL real | 14 | 0 |
| Suítes Java existentes, incluindo contextLoads | 17 | 0 |
| Total Java | 72 | 0 |
| Regressões históricas Node | 4 | 0 |
| Testes Node do leitor Surefire | 7 | 0 |

```text
Tests run: 72, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
verify finalizando-entidades-base: 11/11 critérios com prova PASS · 24 testes TAP lidos · exit 0
verify revisao-projeto: 4/4 critérios com prova PASS · 24 testes TAP lidos · exit 0
server stopped
```

Os 24 casos TAP incluem os 11 casos Node e 13 agregações comportamentais do Java/execução completa. Os 72 casos JUnit são obtidos de XMLs Surefire recentes em diretório único. Todas as parametrizações e casos obrigatórios precisam passar; falha, ausência, relatório antigo ou skip impedem aprovação. O manifesto não foi reduzido.

Provas persistidas: `.spec/verification/finalizando-entidades-base.json` (19:07:06 UTC) e `.spec/verification/revisao-projeto.json` (19:07:36 UTC). Logs e XMLs: `api/target/base-entities-reports/d422c3f2-21a6-4d08-b77b-d379de58a2ef/` e `api/target/base-entities-reports/cbd4a22c-4a9d-45a1-9efb-ec64b5d96530/`. As verificações capturaram HEAD b66f926 com as correções locais presentes; essas mesmas correções foram depois registradas nos commits abaixo, sem modificar as fontes testadas.

## Execução e commits

| Tarefa | Entrega | Commit inicial |
|---|---|---|
| T-005 | Infraestrutura de provas Java/PostgreSQL e snapshots históricos | cab54a3 |
| T-006 | Pessoas, hospitais, hemocentros, veículos e tipos comuns | c1c5240 |
| T-007 | Doação com várias bolsas e API compatível | e4b1c1c |
| T-008 | Requisições, alocações e itens de reserva | a5aa53f |
| T-009 | Rotas com pontos ordenados | 875da40 |
| T-010 | Transporte, leituras e movimentos de estoque | ee52b0e |
| T-011 | Migrações PostgreSQL V4–V7 | 937fcf6 |
| T-012 | Regressões de autenticação e documentação | b66f926 |

Correções da revisão integrada: 6c0ed72 protege vínculos e coleção singular; 442c991 torna a restrição explícita responsável pela rejeição de temperatura não finita; 7e0a87b alinha nomes Hibernate e publica provas históricas no TAP externo. Modelo/esforço das oito sessões: gpt-5.6-terra, medium, sequencial, conforme autorização.

## Histórico de validação

A etapa anterior de escrita dos testes executou 68 casos Java em RED: 25 passaram, 42 falharam e houve 1 erro. As falhas conduziram a implementação, sem enfraquecer testes. Na primeira integração desta execução, 71/72 passaram: a coluna NUMERIC(8,3) rejeitava Infinity pelo limite do tipo (22003) antes da restrição de finitude (23514). A produção foi corrigida para NUMERIC com CHECK explícito; o teste original foi preservado e passou.

Os testes documentais da revisão passaram a consultar o commit histórico analisado, mantendo as asserções e relatórios originais. Todas as 42 fontes de produção alteradas nesta feature estão explicitamente no escopo e mapeadas às tarefas. O restante do código legado não recebe atribuição fictícia de criação; suas regressões existentes continuam na suíte completa.

## Limites da entrega

Associações legadas desconhecidas permanecem nulas. Uma doação aceita várias bolsas; o campo singular representa apenas exatamente uma e não substitui silenciosamente uma coleção múltipla. Bolsas de outra doação e origens conhecidas divergentes são rejeitadas. Não há exclusão em cascata de cadastros para históricos. As migrações V1–V3 mantêm seus conteúdos e checksums, verificados por teste.

Novos fluxos HTTP, seleção FEFO/compatibilidade, Dijkstra, automação operacional e regras clínicas ficam para features próprias. A constituição mantém o aviso recomendado P-002: seu glob JavaScript não encontra arquivos; este gate não comprova uma varredura completa de segredos no projeto.

## Auditoria final

Executado `node .agents/skills/onp-spec-driven/scripts/onp-spec.mjs audit --ci`, com exit 0:

```text
AVISO verificação não olha nenhum arquivo (GLOB_SEM_ARQUIVOS) P-002: o glob src/**/*.js não casa nenhum arquivo
resumo: 2 feature(s) · 9 história(s) de usuário · 15 critério(s) de aceite · 15/15 com teste · 15/15 provados
✔ auditoria limpa (1 aviso(s))
```

As oito tarefas receberam status concluída somente depois das provas PASS. A especificação foi marcada auditada após esse gate; o aviso recomendado preexistente foi preservado sem alterar a constituição.
