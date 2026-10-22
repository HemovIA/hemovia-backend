param(
    [string]$PostgresBin = 'C:\Program Files\PostgreSQL\17\bin',
    [switch]$Verify
)
$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$taskBuild = Join-Path $taskRoot 'api/target'
$taskCluster = Join-Path $taskBuild ('acceptance-postgres-' + [Guid]::NewGuid().ToString('N'))
if (-not [IO.Path]::GetFullPath($taskCluster).StartsWith([IO.Path]::GetFullPath($taskBuild) + [IO.Path]::DirectorySeparatorChar)) { throw 'Cluster fora da pasta de teste' }
foreach ($taskBinary in @('initdb.exe','pg_ctl.exe','createdb.exe')) {
    if (-not (Test-Path -LiteralPath (Join-Path $PostgresBin $taskBinary))) { throw "Ferramenta ausente: $taskBinary" }
}
New-Item -ItemType Directory -Force -Path $taskBuild | Out-Null
$taskPortProbe = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0)
$taskPortProbe.Start()
$taskPort = $taskPortProbe.LocalEndpoint.Port
$taskPortProbe.Stop()
$taskPrevious = @{}
foreach ($taskName in @('HEMOVIA_TEST_JDBC_URL','HEMOVIA_TEST_DB_USER','HEMOVIA_TEST_DB_PASSWORD')) {
    $taskPrevious[$taskName] = [Environment]::GetEnvironmentVariable($taskName, 'Process')
}
$taskStarted = $false
$taskResult = 1
Push-Location $taskRoot
try {
    & (Join-Path $PostgresBin 'initdb.exe') -D $taskCluster -U hemovia_test -A trust --encoding=UTF8 --locale=C *> (Join-Path $taskBuild 'acceptance-initdb.log')
    if ($LASTEXITCODE -ne 0) { throw 'initdb falhou; consulte api/target/acceptance-initdb.log' }
    & (Join-Path $PostgresBin 'pg_ctl.exe') -D $taskCluster -l (Join-Path $taskCluster 'server.log') -o "-h 127.0.0.1 -p $taskPort" -w -t 15 start
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL temporário não iniciou' }
    $taskStarted = $true
    & (Join-Path $PostgresBin 'createdb.exe') -h 127.0.0.1 -p $taskPort -U hemovia_test hemovia_entities_test
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível criar o banco temporário' }
    $env:HEMOVIA_TEST_JDBC_URL = "jdbc:postgresql://127.0.0.1:$taskPort/hemovia_entities_test"
    $env:HEMOVIA_TEST_DB_USER = 'hemovia_test'
    $env:HEMOVIA_TEST_DB_PASSWORD = ''
    if ($Verify) {
        node .agents/skills/onp-spec-driven/scripts/onp-spec.mjs verify finalizando-entidades-base
        $taskResult = $LASTEXITCODE
    } else {
        & mvn -o -f api/pom.xml -Pbase-entities-acceptance test *> (Join-Path $taskBuild 'base-entities-integration-run.log')
        $taskResult = $LASTEXITCODE
        Get-Content -LiteralPath (Join-Path $taskBuild 'base-entities-integration-run.log') -Tail 18
    }
} finally {
    if ($taskStarted) { & (Join-Path $PostgresBin 'pg_ctl.exe') -D $taskCluster -m fast -w -t 15 stop }
    foreach ($taskName in $taskPrevious.Keys) { [Environment]::SetEnvironmentVariable($taskName, $taskPrevious[$taskName], 'Process') }
    Pop-Location
}
exit $taskResult

