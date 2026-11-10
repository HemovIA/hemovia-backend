# Registro de execução

Usuário autorizou em 22/10/2026 as três tarefas sequenciais T-013 a T-015, sem paralelismo, com gpt-5.6-terra e esforço medium em todas. Confirmou Swagger UI/OpenAPI sem login, desativáveis por configuração, para os 14 endpoints atuais. Não pedir nova confirmação para essas decisões.

A execução usa sessões CLI separadas, uma após a outra, com o modelo autorizado e sandbox workspace-write. O orquestrador faz os commits e o gate integrado. A rota PowerShell local mantém trabalho e logs em caso de falha; evita as limitações do executor automático identificadas anteriormente. Não inicia processos visíveis, não remove worktrees e não registra tarefas concluídas sem prova PASS.

O cache Maven global está disponível para o orquestrador, mas não é gravável nas sessões de implementação. Essas sessões não devem rodar Maven, criar cache alternativo ou baixar dependências para contornar o sandbox. A compilação e o download inicial da dependência nova serão feitos pelo orquestrador com permissões apropriadas. Checks estáticos locais continuam disponíveis.

O PostgreSQL de testes é temporário e isolado. Não carregar .env pessoal nos testes, não modificar o banco do usuário e não alterar migrações. Não enfraquecer, pular ou remover testes/manifestações antigas. Os relatórios históricos continuam ancorados no commit analisado; os novos critérios precisam de provas do OpenAPI e UI reais, não existência de anotações.

Logs das sessões: `api/target/execucao-swagger/`. O relatório final mecânico será gerado em `auditoria.md` com `audit --ci --md`. A documentação não será declarada concluída antes da execução de todos os testes e provas das três features.

## Cuidados verificados durante a integração

O orquestrador baixou a dependência 3.1.1 no cache global e a compilação inicial passou com Boot 4.1.1. Ler arquivos com `Get-Content -Encoding UTF8` nas sessões PowerShell; evitar regravar textos UTF-8 interpretados como ANSI, especialmente descrições e exemplos em português. O teste de documentação desabilitada deve alterar a propriedade de entrada `HEMOVIA_API_DOCS_ENABLED=false` e exigir ausência normal (404), sem aceitar 500 como sucesso. A prova de UI habilitada deve seguir o redirect, carregar HTML, inicializador e ao menos um recurso JavaScript real, além de conferir a configuração do OpenAPI.
