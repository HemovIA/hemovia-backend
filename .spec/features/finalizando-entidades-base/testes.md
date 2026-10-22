# Testes dos critérios de aceite

## Escopo desta etapa

O usuário autorizou escrever os testes unitários e de integração em 22/10/2026. Esta etapa não implementa as entidades nem as migrações. A suíte é intencionalmente RED nos comportamentos que ainda faltam; nenhuma tarefa completa do plano de entidades foi encerrada por haver testes escritos.

## Organização

| Arquivo | Responsabilidade |
|---|---|
| api/src/test/java/com/hemovia/api/model/BaseEntitiesUnitTest.java | Invariantes de cadastro, coordenadas, doações/bolsas, datas, volumes, requisições, rotas e telemetria |
| api/src/test/java/com/hemovia/api/controller/ExistingContractsRegressionTest.java | MockMvc dos endpoints atuais e serviços reais com repositórios Mockito; donorId, múltiplas bolsas e datas em dois fusos |
| api/src/test/java/com/hemovia/api/model/BaseEntitiesPersistenceIT.java | PostgreSQL real, Hibernate validate, FKs/CHECKs/uniquidade, transações concorrentes, versão obsoleta, rotas, histórico e migrações |
| api/src/test/java/com/hemovia/api/model/MigrationSourcesTest.java | SHA-256 do conteúdo original de V1–V3, normalizando apenas CRLF/LF para portabilidade entre checkouts |
| api/src/test/java/com/hemovia/api/support/BaseEntityFixtures.java | Construção das entidades reais pelo contrato proposto; ausência de tipo/método é falha, nunca aprovação |
| api/src/test/java/com/hemovia/api/support/BaseEntitiesPostgres.java | Schema aleatório por teste e bootstrap Hibernate programático, sem carregar o .env pessoal |
| tests/finalizando-entidades-base/reporter.test.mjs | Provas contra relatório antigo, caso ausente, falha, error, skip e parametrização parcialmente falha |
| scripts/spec-java/acceptance-cases.json | Casos obrigatórios por critério, incluindo contextLoads explicitamente |

As fixtures usam reflexão para que os testes compilem antes de existirem as nove entidades novas. Cada teste continua exercitando objetos reais, validação, persistência ou contrato; não são classes simuladas nem testes que só procuram arquivos. Erros de assinatura/reflexão não são capturados como rejeições válidas do domínio. Construtores JPA protegidos são aceitos. Quando as classes existirem, imports diretos poderão substituir a reflexão mantendo as mesmas asserções de comportamento.

Os testes fixam um contrato inicial de propriedades Java e nomes SQL a ser implementado: por exemplo Vehicle.code/capacity, BloodBag.volumeMl, BloodRequest.quantity, DeliveryRoute.waypoints/distanceKm/estimatedDurationSeconds, TelemetryReading.temperatureCelsius; allocation_items(allocation_id,blood_bag_id,status), route_waypoints(route_id,position), blood_bags.donation_id. Uma mudança legítima de nomenclatura deve adaptar fixtures e esquema de forma coerente, preservando os comportamentos e negativos testados.

## Rastreabilidade

| Critério | Asserções principais |
|---|---|
| AC-005 | User separado de Person; DTO sem credenciais; veículo com capacidade positiva; código único e doador reutilizável no banco |
| AC-006 | Zero e limites geográficos inclusivos; nulos, nomes vazios, coordenadas e prioridade inválidas; CHECK/NOT NULL no PostgreSQL |
| AC-007 | Duas bolsas por doação, conflito de doadores, recarga e remoção de vínculo sem apagar bolsa/pessoa |
| AC-008 | LocalDate, enums, volume positivo e validade coerente; SQL inválido rejeitado pelo banco |
| AC-009 | Quantidade positiva em bolsas, PENDING/createdAt, FK hospitalar e códigos inválidos |
| AC-010 | Duas conexões/transações disputam a bolsa: um commit e uma violação de unicidade; IN_TRANSIT continua exclusivo; RELEASED permite nova associação e mantém histórico; atualização obsoleta falha |
| AC-011 | Ordem após recarga, posição única e distância negativa rejeitada |
| AC-012 | Transporte/leituras/movimentos recarregados; precisão; ausência/NaN/Infinity rejeitados; cronologia, FK e histórico protegido contra cascata |
| AC-013 | V1–V3 imutáveis, banco vazio, migrate repetido, backfill preservando UUIDs, legado inválido e vínculo ambíguo abortando sem perda |
| AC-014 | PostgreSQL real, contextLoads obrigatório, relatórios recentes por caso; falha/skip/ausência e falha global impedem aprovação |
| AC-015 | POSTs atuais, validação HTTP, resolução de donorId, ausência de bolsa arbitrária na resposta singular e datas ISO em UTC/São Paulo; autenticação permanece coberta pela suíte existente |

Isso representa cobertura escrita dos critérios, não declaração de que todos os cenários já passam. As provas combinam casos unitários e de integração; um caso isolado aprovado não aprova o critério inteiro.

## Executar

Pré-requisitos: JDK 21 ou superior, Maven e dependências em cache (os comandos usam modo offline). Neste ambiente foi usado o JDK 22 já instalado. Para executar só os unitários novos:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-22'
mvn -o -f api/pom.xml '-Dtest=BaseEntitiesUnitTest,ExistingContractsRegressionTest,MigrationSourcesTest' test
node --test tests/finalizando-entidades-base/reporter.test.mjs
```

Para executar toda a suíte Java, inclusive integração e contextLoads, com um cluster PostgreSQL local temporário:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/spec-java/test-postgres.ps1
```

Para também gerar a prova mecânica por critério:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/spec-java/test-postgres.ps1 -Verify
```

ExecutionPolicy vale apenas para o processo desse comando. O script usa os binários PostgreSQL 17 instalados (caminho substituível por -PostgresBin), cria cluster e banco exclusivos em api/target, escolhe uma porta local livre e escuta somente em 127.0.0.1. Encerra o cluster no finally e restaura as variáveis do processo. Não cria serviço Windows nem usa credenciais do projeto. Dados e logs temporários permanecem na pasta de build para diagnóstico.

Alternativamente, fornecer HEMOVIA_TEST_JDBC_URL, HEMOVIA_TEST_DB_USER e HEMOVIA_TEST_DB_PASSWORD de um PostgreSQL dedicado de teste e executar o perfil Maven `base-entities-acceptance`. O perfil inclui *Test, *Tests e *IT; aponta exclusivamente para application-base-entities-test.yaml. Não fornecer uma base de uso corrente: os testes criam/removem seus schemas e o teste de contexto executa Flyway no schema padrão da base de teste.

## Prova e limitações

onpspec.config.json passa a reconhecer os testes Java e executar o adaptador TAP. O adaptador executa Maven uma vez, usa diretório único de relatórios, verifica os casos obrigatórios e recusa resultados antigos. Todas as parametrizações de um método precisam passar; nenhuma aprovação é inferida do exit code global. A falha global também impede aprovação de AC-014.

O gate preserva os quatro testes documentais da revisão anterior. Três já falhavam antes desta etapa, por referências do snapshot histórico ao arquivo V2 de hospitais renomeado para V3; esses testes não foram enfraquecidos ou alterados neste pedido. Essa pendência de T-005 continua registrada separadamente.

Não foram criadas entidades substitutas, migrações fictícias, skips ou marcações manuais PASS. O resultado real e os caminhos das provas estão em resultado-validacao.md.

## Consolidação T-012

Além dos testes de aceite, a regressão de autenticação preserva registro, login, normalização de e-mail/CPF e JWT somente com `sub`, `iat` e `exp`. `AuthControllerTest` verifica que as respostas de registro e login não expõem `password`; `AuthServiceImplTest` continua verificando que a senha é codificada antes de persistir; `JwtServiceTest` verifica que o token não recebe e-mail, CPF ou papel.

`ExistingContractsRegressionTest` prova os três POSTs legados, a resolução de `donorId`, a recusa de doador inexistente, `bloodBags` completo com `bloodBag` singular somente no caso de uma bolsa e a preservação de `LocalDate` em UTC e America/Sao_Paulo. A serialização da resposta é exercitada para detectar ciclo de JSON e presença de credencial. A adaptação legítima foi trocar o relacionamento interno de uma bolsa por uma coleção, sem mudar o payload singular aceito; novos campos de rastreabilidade continuam opcionais e não são inventados.

O manifesto `scripts/spec-java/acceptance-cases.json` exige todos os métodos de aceite, inclusive `ApiApplicationTests.contextLoads`. `onpspec.config.json` enumera explicitamente os scripts de prova e fontes de produção das tarefas T-005 a T-011, além de migrações. Isso permite que a auditoria detecte cobertura ausente sem ampliar o escopo para código didático não relacionado.

Não houve execução da suíte PostgreSQL, `verify` ou `audit --ci` nesta consolidação: esses comandos são gate integrado do orquestrador, conforme o contrato de trabalho. Portanto esta seção documenta cobertura e comando; não é prova de PASS.
