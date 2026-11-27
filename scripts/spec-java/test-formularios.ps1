param(
  [string[]]$Features = @('formularios-assistidos'),
  [switch]$Verify,
  [switch]$All
)
$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$front = [IO.Path]::GetFullPath((Join-Path $taskRoot '../hemovia-frontend'))
if ($All) { $Features = @('formularios-assistidos') + @(Get-ChildItem -LiteralPath (Join-Path $taskRoot '.spec/features') -Directory | Where-Object Name -ne 'formularios-assistidos' | Sort-Object Name | ForEach-Object Name) }
$build = Join-Path $taskRoot 'api/target'
$run = Join-Path $build ('formularios-harness-' + [guid]::NewGuid().ToString('N'))
if (-not $run.StartsWith([IO.Path]::GetFullPath($build) + [IO.Path]::DirectorySeparatorChar)) { throw 'Diretório de teste fora de api/target.' }
New-Item -ItemType Directory -Force $run | Out-Null
$pg = 'C:\Program Files\PostgreSQL\17\bin'
$cluster = Join-Path $run 'postgres'
$listener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0)
$chatListener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0)
try {
  $listener.Start(); $port = $listener.LocalEndpoint.Port
  $chatListener.Start(); $chatModelPort = $chatListener.LocalEndpoint.Port
} finally {
  $chatListener.Stop()
  $listener.Stop()
}
foreach ($fixedPort in @(5179,5180,18080)) {
  $probe = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback,$fixedPort)
  try { $probe.Start() } catch { throw "Porta $fixedPort ocupada; não será encerrado processo externo." } finally { $probe.Stop() }
}
$previous = @{}
$names = @('JAVA_HOME','HEMOVIA_TEST_JDBC_URL','HEMOVIA_TEST_DB_USER','HEMOVIA_TEST_DB_PASSWORD','POSTGRES_HOST','POSTGRES_PORT','POSTGRES_DB','POSTGRES_USER','POSTGRES_PASSWORD','HEMOVIA_API_PROXY_TARGET','HEMOVIA_VITE_ENV_DIR','HEMOVIA_TEST_CHAT_MODEL_PORT')
foreach ($name in $names) { $previous[$name] = [Environment]::GetEnvironmentVariable($name,'Process') }
$oldPath = $env:Path; $env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-22'; $env:Path = "$env:JAVA_HOME\bin;$env:Path"
$started = $false; $app = $null; $viteTest = $null; $viteQa = $null; $result = 1
Push-Location $taskRoot
try {
  & "$pg\initdb.exe" -D $cluster -U qa -A trust --encoding=UTF8 --locale=C *> (Join-Path $run 'initdb.log')
  if ($LASTEXITCODE -ne 0) { throw 'initdb falhou; consulte initdb.log.' }
  $launcher = Start-Process -FilePath "$pg\pg_ctl.exe" -ArgumentList @('-D', "`"$cluster`"", '-l', "`"$cluster\server.log`"", '-o', "`"-h 127.0.0.1 -p $port`"", '-w', 'start') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $run 'pg-start.log') -RedirectStandardError (Join-Path $run 'pg-start.err')
  $started = $true
  if (-not $launcher.WaitForExit(20000)) { throw 'Inicializador PostgreSQL excedeu o prazo.' }
  & "$pg\pg_isready.exe" -h 127.0.0.1 -p $port *> $null
  if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL não ficou disponível.' }
  foreach ($database in @('hemovia_suite','hemovia_ui')) { & "$pg\createdb.exe" -h 127.0.0.1 -p $port -U qa $database; if ($LASTEXITCODE -ne 0) { throw "Falha ao criar banco $database." } }
  $env:HEMOVIA_TEST_JDBC_URL = "jdbc:postgresql://127.0.0.1:$port/hemovia_suite"; $env:HEMOVIA_TEST_DB_USER='qa'; $env:HEMOVIA_TEST_DB_PASSWORD=''
  $env:POSTGRES_HOST='127.0.0.1'; $env:POSTGRES_PORT=[string]$port; $env:POSTGRES_DB='hemovia_suite'; $env:POSTGRES_USER='qa'; $env:POSTGRES_PASSWORD=''
  $env:HEMOVIA_TEST_CHAT_MODEL_PORT = [string]$chatModelPort
  & mvn -o -B -f api/pom.xml '-DskipTests' package *> (Join-Path $run 'package.log')
  if ($LASTEXITCODE -ne 0) { throw 'Build backend falhou; consulte package.log.' }
  $yaml = @"
server:
  port: 18080
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:$port/hemovia_ui
    username: qa
    password: ''
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    locations: classpath:db/migration
hemovia:
  donation-chat:
    enabled: true
    base-url: http://127.0.0.1:$chatModelPort
  security:
    jwt-secret: chave-sintetica-exclusiva-teste-0123456789
    access-token-minutes: 60
    refresh-token-days: 1
    cookie-secure: false
    cookie-same-site: Lax
    cors-allowed-origins: http://127.0.0.1:5180
  admin-bootstrap:
    name: Admin Sintetico
    email: qa-admin@example.org
    cpf: '52998224725'
    password: SenhaTeste123!
management:
  endpoints:
    web:
      exposure:
        include: health
"@
  $config = Join-Path $run 'application.yaml'; Set-Content -LiteralPath $config -Encoding utf8 -Value $yaml
  $jar = Join-Path $taskRoot 'api/target/api-0.0.1-SNAPSHOT.jar'
  if (-not (Test-Path -LiteralPath $jar)) { throw 'Jar de teste não foi gerado.' }
  $app = Start-Process -FilePath (Join-Path $env:JAVA_HOME 'bin/java.exe') -ArgumentList @('-jar', "`"$jar`"", "`"--spring.config.location=file:$($config.Replace('\','/'))`"") -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $run 'api.log') -RedirectStandardError (Join-Path $run 'api.err')
  $ready = $false
  for ($i=0; $i -lt 60; $i++) { try { if ((Invoke-WebRequest -UseBasicParsing 'http://127.0.0.1:18080/actuator/health' -TimeoutSec 1).StatusCode -eq 200) { $ready=$true; break } } catch {}; Start-Sleep -Seconds 1 }
  if (-not $ready) { throw 'API de teste não iniciou; consulte api.log.' }
  & "$pg\psql.exe" -h 127.0.0.1 -p $port -U qa -d hemovia_ui -c "insert into blood_centers(id,name,city,latitude,longitude) values ('10000000-0000-4000-8000-000000000002','Hemocentro Sintetico','Recife',-8.05,-34.9); insert into person(id,name,cpf) values ('10000000-0000-4000-8000-000000000020','Doador Sintetico Existente','11144477735');" *> (Join-Path $run 'seed.log')
  if ($LASTEXITCODE -ne 0) { throw 'Seed sintético falhou.' }
  $chatSeed = @"
INSERT INTO users (id, name, email, cpf, password, role, donor_id)
SELECT '10000000-0000-4000-8000-000000000025', 'Doadora Chat Sintetica',
       'qa-doadora-chat@example.org', '11144477735', password, 'DOADOR',
       '10000000-0000-4000-8000-000000000020'
FROM users
WHERE email = 'qa-admin@example.org';
"@
  & "$pg\psql.exe" -v ON_ERROR_STOP=1 -h 127.0.0.1 -p $port -U qa -d hemovia_ui -c $chatSeed *> (Join-Path $run 'chat-seed.log')
  if ($LASTEXITCODE -ne 0) { throw 'Seed sintético do chat falhou.' }
  $node=(Get-Command node.exe).Source; $vite=Join-Path $front 'node_modules/vite/bin/vite.js'
  $env:HEMOVIA_API_PROXY_TARGET='http://127.0.0.1:18080'; $env:HEMOVIA_VITE_ENV_DIR=$run
  $viteTest=Start-Process -FilePath $node -ArgumentList @("`"$vite`"",'--host','127.0.0.1','--port','5179','--strictPort') -WorkingDirectory $front -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $run 'vite-test.log') -RedirectStandardError (Join-Path $run 'vite-test.err')
  $viteQa=Start-Process -FilePath $node -ArgumentList @("`"$vite`"",'--host','127.0.0.1','--port','5180','--strictPort') -WorkingDirectory $front -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $run 'vite-qa.log') -RedirectStandardError (Join-Path $run 'vite-qa.err')
  foreach ($url in @('http://127.0.0.1:5179/','http://127.0.0.1:5180/')) {
    $ready=$false; for ($i=0; $i -lt 30; $i++) { try { if ((Invoke-WebRequest -UseBasicParsing $url -TimeoutSec 1).StatusCode -eq 200) { $ready=$true; break } } catch {}; Start-Sleep -Seconds 1 }; if (-not $ready) { throw "Frontend não iniciou: $url" }
  }
  if ($Verify) {
    $result=0
    foreach ($feature in $Features) {
      Write-Host "Verificando $feature"
      $began = [DateTimeOffset]::UtcNow
      & node .agents/skills/onp-spec-driven/scripts/onp-spec.mjs verify $feature
      if ($LASTEXITCODE -ne 0) { $result=$LASTEXITCODE; break }
      $proofPath = Join-Path $taskRoot ".spec/verification/$feature.json"
      if (-not (Test-Path -LiteralPath $proofPath)) { Write-Error "Prova ausente: $feature" -ErrorAction Continue; $result=1; break }
      $proof = Get-Content -LiteralPath $proofPath -Raw | ConvertFrom-Json
      $statuses = @($proof.results.PSObject.Properties | ForEach-Object { $_.Value.status })
      $specPath = Join-Path $taskRoot ".spec/features/$feature/spec.md"
      $criteria = [regex]::Matches((Get-Content -LiteralPath $specPath -Raw), '(?m)^#{1,6}\s+(AC-\d+)\b') |
        ForEach-Object { $_.Groups[1].Value }
      $missing = @($criteria | Where-Object { $_ -notin $proof.results.PSObject.Properties.Name })
      $principles = @($proof.principles.PSObject.Properties | ForEach-Object { $_.Value.status })
      $proofTime = if ($proof.timestamp -is [datetime]) { [DateTimeOffset]::new($proof.timestamp.ToUniversalTime()) } else { [DateTimeOffset]::Parse([string]$proof.timestamp, [Globalization.CultureInfo]::InvariantCulture) }
      if ($proof.feature -ne $feature -or $null -eq $proof.exitCode -or $proof.exitCode -ne 0 -or
          $proofTime -lt $began -or $statuses.Count -eq 0 -or $missing.Count -gt 0 -or
          @($statuses + $principles | Where-Object { $_ -ne 'pass' }).Count -gt 0) {
        Write-Error "Prova inválida ou com falha/pulo para $feature; consulte $proofPath" -ErrorAction Continue
        $result=1; break
      }
      Write-Host "Prova validada: $feature; exitCode=0; $($statuses.Count) critérios PASS."
    }
  } else { & node scripts/spec-java/verify-tests.mjs; $result=$LASTEXITCODE }
} finally {
  foreach ($proc in @($viteTest,$viteQa,$app)) { if ($null -ne $proc) { $proc.Refresh(); if (-not $proc.HasExited) { Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue } } }
  if ($started) {
    & "$pg\pg_ctl.exe" -D $cluster status *> $null
    if ($LASTEXITCODE -eq 0) { & "$pg\pg_ctl.exe" -D $cluster -m fast -w stop }
  }
  foreach ($name in $previous.Keys) { [Environment]::SetEnvironmentVariable($name,$previous[$name],'Process') }; $env:Path=$oldPath
  Pop-Location
}
exit $result
