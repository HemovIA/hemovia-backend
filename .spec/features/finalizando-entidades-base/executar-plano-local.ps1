param([string]$FromTask = 'T-005', [string]$ToTask = 'T-012')
$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
Set-Location -LiteralPath $taskRoot
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-22'
$taskLogDir = Join-Path $taskRoot 'api/target/execucao-entidades'
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
    node $taskEngine tarefa finalizando-entidades-base $taskId em-andamento
    node $taskEngine evento --run $taskPlan.runId --tipo tarefa --tarefa $taskId --estado executando
    @{ tarefa=$taskId; fase='executando'; instante=[DateTime]::UtcNow.ToString('o') } | ConvertTo-Json | Set-Content -Encoding UTF8 $taskStatePath
    $taskPrompt = @"
Execute somente $taskId da feature finalizando-entidades-base no workspace atual.
O usuário autorizou o plano sequencial completo, gpt-5.6-terra, esforço medium. Não pergunte novamente sobre custo, sequência ou escopo já confirmado.
Leia .spec/features/finalizando-entidades-base/spec.md, design.md, execucao.md e .spec/constituicao.md; depois leia os arquivos relevantes à sua tarefa e os testes existentes. O texto da sua tarefa é:

$taskSection

Contrato de trabalho:
- Implemente de verdade os arquivos de produção/infraestrutura da sua tarefa. Não faça apenas plano ou relatório. Preserve o trabalho anterior e a compilação.
- NÃO rode git add, git commit, git checkout/switch, reset, stash ou remoção de arquivos de outras tarefas. O orquestrador faz o commit. Não altere tasks.md, spec.md nem a constituição.
- NÃO execute verify/audit nem inicie servidor PostgreSQL; o orquestrador cuida do gate integrado real ao final. Pode fazer checks locais que não precisem de privilégios.
- Os testes de aceite em BaseEntitiesUnitTest, BaseEntitiesPersistenceIT, ExistingContractsRegressionTest e MigrationSourcesTest são o contrato. NUNCA enfraqueça, apague, pule ou remova métodos do manifesto para aprovar implementação. Nada de entidades fictícias, aprovações manuais ou H2 em lugar do PostgreSQL.
- Em T-005, as mudanças dos testes históricos devem somente consultar suas evidências no commit original 91d1f981e8860df0cb53fa9472edd8f1bcd91e89 via git show, mantendo conteúdo/linhas/asserções. Relatórios históricos não são reescritos. O srcGlobs deve incluir as fontes de produção desta feature, com caminhos explicitamente mapeados às tarefas, além dos scripts já observados.
- Em T-006 a T-010, não mude os testes de aceite. Leia BaseEntityFixtures para implementar exatamente os contratos Java esperados. Setters não devem ter sobrecargas ambíguas de mesma aridade. Datas do domínio são LocalDate; DTOs legados podem manter Date com conversão UTC consistente. Não criar relações falsas para valores legados desconhecidos.
- T-007 deve adaptar DTOs/serviços na mesma tarefa da entidade, resolver donorId e evitar cascade REMOVE. Os dois testes antigos de BloodBagServiceImplTest mantêm suas regras; adaptar apenas fixture/assinatura se necessário, sem retirar asserções.
- Em T-011, escreva SQL V4–V7 completo e transacional, sem alterar V1–V3 ou seus checksums. Preflight rejeita código legado desconhecido e bolsa vinculada a múltiplas doações. O índice parcial protege RESERVED e IN_TRANSIT; RELEASED permite nova alocação mantendo histórico. CHECKs precisam impedir NaN/Infinity em numeric. Respeite o esquema usado nos testes JDBC.
- Em T-012, consolidar documentação e manifestos sem reduzir cobertura; corrigir problemas de integração restantes no código da feature se identificados, reportando arquivos extras. Não declarar PASS sem execução real do orquestrador.
- Use Bean Validation e restrições de banco para invariantes, UUID, enums STRING, instantes Instant e associações LAZY. Não expor entidades JPA diretamente em JSON nem dados de credenciais. Escopo é modelo persistente e compatibilidade, sem CRUDs novos, FEFO ou Dijkstra.
- Ao terminar, responda brevemente com arquivos alterados, o que implementou e limitações. Não peça permissão para continuar a tarefa autorizada.
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
        git commit -m "$taskId finalizando-entidades-base: $($taskEntry.titulo)"
        if ($LASTEXITCODE -ne 0) { throw "Commit falhou: $taskId" }
    }
    $taskCommit = git rev-parse --short HEAD
    node $taskEngine evento --run $taskPlan.runId --tipo tarefa --tarefa $taskId --estado aguardando-prova
    @{ tarefa=$taskId; fase='implementada-aguardando-prova'; commit=$taskCommit; instante=[DateTime]::UtcNow.ToString('o') } | ConvertTo-Json | Set-Content -Encoding UTF8 $taskStatePath
    Write-Output "IMPLEMENTADA $taskId — commit $taskCommit — aguardando gate integrado"
}
@{ fase='tarefas-implementadas'; instante=[DateTime]::UtcNow.ToString('o') } | ConvertTo-Json | Set-Content -Encoding UTF8 $taskStatePath
Write-Output 'Tarefas executadas. O orquestrador deve rodar PostgreSQL temporário, verify das duas features e audit --ci.'

