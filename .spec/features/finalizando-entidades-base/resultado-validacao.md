# Resultado da etapa de testes — 22/10/2026

## Escopo e ambiente

Foram escritos os testes solicitados, sem implementar entidades, alterar serviços/controllers de produção ou criar migrações novas. O POM recebeu apenas um perfil de teste; a configuração do motor passou a reconhecer os testes Java e executar a coleta TAP real.

Ambiente usado: Windows, JDK 22 instalado, Maven offline e PostgreSQL 17 instalado. O banco foi um cluster novo, com porta aleatória em 127.0.0.1 e dados sintéticos, encerrado ao terminar. Nenhum .env pessoal foi usado. O teste de contexto recebeu apenas application-base-entities-test.yaml.

## Execução final

Comando: `powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/spec-java/test-postgres.ps1 -Verify`, com JAVA_HOME no JDK 22.

| Suíte | Executados | PASS | Falhas | Erros | Ignorados |
|---|---:|---:|---:|---:|---:|
| BaseEntitiesUnitTest | 29 | 3 | 26 | 0 | 0 |
| ExistingContractsRegressionTest | 7 | 3 | 4 | 0 | 0 |
| MigrationSourcesTest | 1 | 1 | 0 | 0 | 0 |
| BaseEntitiesPersistenceIT | 14 | 1 | 12 | 1 | 0 |
| Suítes Java existentes, incluindo ApiApplicationTests.contextLoads | 17 | 17 | 0 | 0 | 0 |
| Total Java | 68 | 25 | 42 | 1 | 0 |

Os novos testes correspondem a 37 casos unitários/web e 14 de integração, contando parametrizações. Os seis testes Node do leitor de relatórios também passaram. A coleta Node completa executou dez casos: sete passaram e três testes documentais históricos já existentes falharam nas referências à migration renomeada. Esses três testes não foram alterados nem ignorados.

Estado da implementação: RED. As falhas indicam entidades/tipos ainda ausentes, invariantes inexistentes, descarte do donorId, contrato singular/coleção e formato de data ainda não adaptados, além das migrações V4–V7 não implementadas. O erro de integração do upgrade expõe a ausência da coluna donation_id no esquema legado. A conexão e a execução PostgreSQL reais foram comprovadas; isso não aprova os cenários que ainda falham.

O perfil inicial de coleta não incluía nomes terminados em *Tests; isso foi corrigido, e contextLoads foi incluído expressamente no manifesto. Somente a coleta final acima é o resultado considerado para encerramento desta etapa.

## Evidências

- Prova por critério: `.spec/verification/finalizando-entidades-base.json`.
- Relatórios Surefire e logs finais: `api/target/base-entities-reports/c65717f3-43be-45b4-b58e-4ed98215cc2b/`.
- `maven.log` registra as 68 execuções e `BUILD FAILURE`; `node.log` registra as regressões documentais e os testes do adaptador.
- O resultado de verify foi exit 1, 0/11 critérios com PASS, 13 casos TAP de agregação lidos. Cada critério exige que todos os seus casos obrigatórios, incluindo integração, passem; aprovação parcial não fecha o critério.
- O PostgreSQL temporário foi encerrado, conforme a saída `server stopped` do finally.

Não há skip/todo, provas inventadas, entidades fictícias ou mudanças de expectativa para transformar a fase RED em verde. Os arquivos da implementação permanecem pendentes. O próximo trabalho é implementar o modelo contra esses testes e resolver a validação histórica prevista em T-005; este pedido não autorizou concluir automaticamente o restante do plano.

## Consolidação T-012 — pré-gate, 22/10/2026

Foram consolidados o manifesto por critério, o escopo explícito de fontes auditadas, a documentação do modelo e as verificações de regressão de credenciais nas respostas HTTP. A documentação registra as adaptações internas legítimas: uma doação agora tem coleção de bolsas, sem romper o payload singular para exatamente uma bolsa; `donorId` é resolvido para `Person`; e a ausência de dado legado continua ausência, sem vínculo artificial.

Não foi executado nesta tarefa nenhum PostgreSQL, `onp-spec verify` ou `onp-spec audit --ci`, por instrução expressa de reservar o gate integrado ao orquestrador. Não há relatório novo de Surefire/TAP, nem resultado PASS a declarar. A evidência pendente é a execução real, em ordem: suíte completa com `scripts/spec-java/test-postgres.ps1 -Verify`, verify das features aplicáveis e audit CI. Qualquer falha, relatório antigo, skip ou impedimento deve permanecer registrado como tal antes de concluir tarefas ou alterar o status da spec.
