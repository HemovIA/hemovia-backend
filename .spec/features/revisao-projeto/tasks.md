# Tasks: Revisão do projeto

> feature: revisao-projeto

Arquivos lista entregas criadas ou alteradas. O código do produto será lido. As três primeiras tarefas são independentes; a quarta depende de todas elas.

## T-001 — Mapear objetivos e implementação [concluida]
- Refs: US-001, AC-001
- Arquivos: .spec/features/revisao-projeto/relatorios/alinhamento.md, tests/revisao-projeto/alinhamento.test.mjs
- Notas: Inventariar arquivos versionados e alterações locais relevantes; comparar README, diagramas, módulos e testes. Registrar evidências e lacunas por funcionalidade. Teste @spec:AC-001 verifica completude da matriz e existência das referências.

## T-002 — Revisar arquitetura, API, dados e segurança [concluida]
- Refs: US-002, AC-002
- Arquivos: .spec/features/revisao-projeto/relatorios/api-dados-seguranca.md, tests/revisao-projeto/api-dados-seguranca.test.mjs
- Notas: Inspecionar camadas, contratos, regras de negócio, validações, migrações, transações e controle de acesso. Usar achados API-001 em diante. Reproduzir quando possível e distinguir riscos de defeitos; ocultar valores sensíveis. Teste @spec:AC-002 verifica campos, áreas cobertas e referências.

## T-003 — Revisar AED, testes e operação [concluida]
- Refs: US-002, AC-003
- Arquivos: .spec/features/revisao-projeto/relatorios/qualidade-operacao.md, tests/revisao-projeto/qualidade-operacao.test.mjs
- Notas: Revisar Java/C, casos extremos e configurações; executar verificações disponíveis em ambiente local isolado. Registrar comandos, resultados e impedimentos. Usar achados QUA-001 em diante. Documentar diferenças entre o motor Node e testes Maven/Java/C. Teste @spec:AC-003 verifica cobertura e registro de execução ou impedimento.

## T-004 — Consolidar diagnóstico e verificar provas [concluida]
- Refs: US-003, AC-004
- Arquivos: .spec/features/revisao-projeto/relatorios/alinhamento.md, .spec/features/revisao-projeto/relatorios/api-dados-seguranca.md, .spec/features/revisao-projeto/relatorios/qualidade-operacao.md, .spec/features/revisao-projeto/relatorio-final.md, tests/revisao-projeto/consolidacao.test.mjs
- Notas: Depende de T-001, T-002 e T-003. Os caminhos compartilhados impedem consolidação simultânea no motor. Consolidar todos os achados, prioridades, dependências, evidências positivas e limitações. Fixar commit e estado local. Teste @spec:AC-004 verifica rastreabilidade dos achados ao plano de ação. Rodar verify e audit --ci. Se o gate exigir alteração fora das entregas, registrar o impedimento e propor ajuste sem contornar a verificação.
