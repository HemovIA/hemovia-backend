# Arquitetura da observabilidade

A raiz do backend contém o Compose da demonstração. A API utiliza a imagem construída em `api/`. O PostgreSQL da demonstração tem volume próprio. O cliente React pode consumir as rotas usuais da API e não é necessário para gerar carga.

| Origem | Destino | Porta | Fluxo |
|---|---|---|---|
| Prometheus | API | 8081 | GET /actuator/prometheus |
| Prometheus | Node Exporter | 9100 | CPU, memória, disco, rede e carga |
| Prometheus | cAdvisor | 8080 | CPU e memória por contêiner |
| API | PostgreSQL | 5432 | Operações de negócio e pool HikariCP |
| API | Tempo | 4318 | Exportação OTLP HTTP de traces |
| API → Alloy | Loki | 3100 | Logs JSON da API coletados por Alloy |
| Grafana | Prometheus, Loki, Tempo | 9090, 3100, 3200 | Consultas pelos três datasources |
| Usuário → HTTPS | Grafana | 3000 interno | Interface e conta Viewer |

## Contratos entre tarefas

- A API é identificada como `hemovia-api` em métricas, logs e traces. A série HTTP expõe histogramas para o p95.
- O gerenciamento usa a porta 8081 e fica acessível ao Prometheus pela rede Docker. Para a execução fora da stack, o gerenciamento é vinculado a loopback. A porta principal oferece `/readyz` com apenas o estado para Docker, Render e CI. Rotas de negócio mantêm o modelo de cookies, CSRF e papéis.
- As métricas `hemovia.doacoes.registradas` e `hemovia.requisicoes.emitidas` são contadores agregados, sem dimensões de pessoa ou de registro. O incremento ocorre após o commit da operação. Os nomes Prometheus previstos são `hemovia_doacoes_registradas_total` e `hemovia_requisicoes_emitidas_total`.
- A stack é configurada para amostrar 100% dos traces da demonstração. Pelo menos um fluxo real da API terá um span interno e uma mensagem de log com MDC `traceId`. O Loki armazena esse ID no log, com `application="hemovia-api"` como label de seleção e link de correlação provisionado no Grafana.
- Banco, Actuator, exporters, Loki, Tempo e Prometheus usam a rede Docker. A API e o Grafana ficam disponíveis localmente via loopback; a entrada HTTPS da demonstração encaminha somente ao Grafana. O mecanismo efetivo de acesso e o teste externo dependem do host confirmado.
- O script de carga reutiliza rotas da API e gera um erro controlado em uma base isolada. A verificação consulta Prometheus, Loki, Tempo e Grafana e registra evidências de execução com horário.
- O relatório inclui a distinção entre métricas do host Linux e métricas da VM do Docker Desktop quando for o ambiente usado para as capturas.

## Verificação em 22/11/2026

O modo de observabilidade executou 10 critérios locais contra o Compose em funcionamento. Na revalidação histórica, o runner completo executou 480 testes Maven com PostgreSQL temporário e 16 testes Playwright com a API em porta separada. A saída TAP integral de uma rodada aprovada foi usada para registrar as provas históricas restantes no mesmo estado do código. O comando configurado do runner foi restaurado antes da auditoria final.
