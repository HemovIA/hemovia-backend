param([string]$FromTask = 'T-013', [string]$ToTask = 'T-015')
$ErrorActionPreference = 'Stop'
$OutputEncoding = [Text.UTF8Encoding]::new($false)
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
Set-Location -LiteralPath $taskRoot
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-22'
$taskLogDir = Join-Path $taskRoot 'api/target/execucao-swagger'
New-Item -ItemType Directory -Force -Path $taskLogDir | Out-Null
$taskPlan = Get-Content -Raw -Encoding UTF8 (Join-Path $PSScriptRoot 'plano.json') | ConvertFrom-Json
$taskTasksText = Get-Content -Raw -Encoding UTF8 (Join-Path $PSScriptRoot 'tasks.md')
$taskStatePath = Join-Path $taskLogDir 'estado.json'
$taskEngine = '.agents/skills/onp-spec-driven/scripts/onp-spec.mjs'
foreach ($taskEntry in $taskPlan.sequenciais) {
    if ($taskEntry.id -lt $FromTask -or $taskEntry.id -gt $ToTask) { continue }
    $taskId = $taskEntry.id
    $taskSection = [regex]::Match($taskTasksText, '(?ms)^## ' + [regex]::Escape($taskId) + ' .+?(?=^## |\z)').Value
    if (-not $taskSection) { throw "Tarefa ausente: $taskId" }
    node $taskEngine tarefa documentacao-swagger $taskId em-andamento
    node $taskEngine evento --run $taskPlan.runId --tipo tarefa --tarefa $taskId --estado executando
    @{ tarefa=$taskId; fase='executando'; instante=[DateTime]::UtcNow.ToString('o') } | ConvertTo-Json | Set-Content -Encoding UTF8 $taskStatePath
    $taskPrompt = @"
Execute somente $taskId da feature documentacao-swagger neste workspace.
O usuário confirmou explicitamente as três tarefas em sequência, sem paralelismo, gpt-5.6-terra e esforço medium. Não peça nova autorização para custo/escopo/execução já confirmados.
Leia .spec/features/documentacao-swagger/spec.md, .spec/features/documentacao-swagger/execucao.md e .spec/constituicao.md. Sua tarefa:

$taskSection

Contrato de execução:
- Implemente código e testes reais da sua tarefa, não só relatório. Preserve todo trabalho das features anteriores e das etapas anteriores desta execução.
- Não execute git add/commit/switch/checkout/reset/stash, nem apague trabalho. O orquestrador revisa e registra commits por tarefa. Não altere spec.md, tasks.md, constituição nem artefatos gerados do plano.
- Não execute Maven nem baixe dependências nas sessões sandbox workspace-write: o cache global existe, mas não admite escrita nessas sessões. Não redirecione user.home nem crie repositório .m2 alternativo. O orquestrador faz Maven com as permissões necessárias. Checks estáticos sem privilégios são permitidos.
- Não inicie PostgreSQL nem rode verify/audit. O gate final real é do orquestrador. Não declare PASS sem resultados reais.
- Spring Boot é 4.1.1. Proposta validada na documentação oficial springdoc.org: org.springdoc:springdoc-openapi-starter-webmvc-ui 3.1.1. Não mudar Boot, migrar Jackson da aplicação nem reescrever contratos para fazer o Swagger funcionar. Use APIs da dependência e confirme assinaturas nos arquivos em cache quando disponíveis.
- Swagger UI/OpenAPI públicos, habilitados por padrão; HEMOVIA_API_DOCS_ENABLED=false desativa ambos. Sem novo CRUD, migrações ou alterações na política dos endpoints existentes. Somente /auth/me requer JWT atualmente. Esquema Bearer tem botão Authorize e não persiste autorização por padrão. Não documentar entidades JPA ou principal interno como input/response.
- Testes devem fazer HTTP real com Spring MVC/springdoc, usando contexto isolado já existente e PostgreSQL temporário do orquestrador. Não substituir o documento por JSON estático nem provar só presença de anotações. Inclua um contexto separado para docs desativadas. Cada critério precisa de @DisplayName("@spec:AC-xxx ...") e asserções positivas/negativas pertinentes. Não pular nem enfraquecer testes existentes.
- Métodos, status, erros, required fields, enums, datas e exemplos devem refletir os controllers/DTOs/handlers reais. Não inventar 401/400/404 globais por conveniência nem mudar handlers. Password writeOnly em schemas de entrada; respostas nunca expõem password. Datas de exemplos válidos precisam continuar passando conforme relógio do teste, sem dados reais. Criar doação por bloodBagId mantém payload atual; bloodBag singular nulo para zero/várias bolsas e bloodBags completo.
- Na T-015, preservar todos os métodos antigos do manifesto. Incluir os seis novos critérios e toda produção alterada no srcGlobs com referências em tasks.md existentes. Atualizar script de PostgreSQL para verify das três features. Documentar os comandos, a opção de configuração e JWT. Criar testes/resultado preliminares honestos e deixar auditoria.md para o audit --ci --md do orquestrador; não criar um PASS manual. Se encontrar integração pendente, corrigir produção sem contornar provas e informar arquivos extras.
- Ao terminar, responder brevemente com arquivos alterados, comportamento implementado e limitações conhecidas. Não pedir confirmação para continuar o trabalho já autorizado.
"@
    $taskPromptPath = Join-Path $taskLogDir "$taskId-prompt.txt"
    $taskPrompt | Set-Content -Encoding UTF8 $taskPromptPath
    Write-Output "INICIO $taskId — $($taskEntry.titulo)"
    $ErrorActionPreference = 'Continue'
    Get-Content -Raw -Encoding UTF8 $taskPromptPath | codex exec --model gpt-5.6-terra -c model_reasoning_effort=medium --sandbox workspace-write --json --output-last-message (Join-Path $taskLogDir "$taskId-resumo.txt") - 1> (Join-Path $taskLogDir "$taskId.jsonl") 2> (Join-Path $taskLogDir "$taskId-stderr.log")
    $taskExit = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($taskExit -ne 0) {
        @{ tarefa=$taskId; fase='falhou'; exit=$taskExit } | ConvertTo-Json | Set-Content -Encoding UTF8 $taskStatePath
        throw "Sessão $taskId falhou, trabalho preservado: $taskLogDir"
    }
    $taskFiles = @($taskEntry.arquivos | Where-Object { Test-Path -LiteralPath $_ })
    if ($taskFiles.Count) { git add -- $taskFiles; if ($LASTEXITCODE -ne 0) { throw "git add falhou: $taskId" } }
    git diff --cached --quiet
    if ($LASTEXITCODE -eq 1) {
        git commit -m "$taskId documentacao-swagger: $($taskEntry.titulo)"
        if ($LASTEXITCODE -ne 0) { throw "Commit falhou: $taskId" }
    }
    $taskCommit = git rev-parse --short HEAD
    node $taskEngine evento --run $taskPlan.runId --tipo tarefa --tarefa $taskId --estado aguardando-prova
    @{ tarefa=$taskId; fase='implementada-aguardando-prova'; commit=$taskCommit; instante=[DateTime]::UtcNow.ToString('o') } | ConvertTo-Json | Set-Content -Encoding UTF8 $taskStatePath
    Write-Output "IMPLEMENTADA $taskId — commit $taskCommit — aguardando gate integrado"
}
@{ fase='tarefas-implementadas'; instante=[DateTime]::UtcNow.ToString('o') } | ConvertTo-Json | Set-Content -Encoding UTF8 $taskStatePath
Write-Output 'Tarefas executadas. O orquestrador deve rodar PostgreSQL temporário, verify das três features e audit --ci.'

