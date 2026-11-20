# Plano de execução — aedus04-poous07-roteirizacao

> gerado por `onp-spec plano` em 2026-11-20 15:50 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano aedus04-poous07-roteirizacao --paralelizar T-028,T-029`

## Resumo — o que vai acontecer

- **5 tarefa(s) pendente(s)**: 2 em 2 faixa(s) paralela(s) + 3 sequencial(is)
- **seleção do usuário**: paralelizar só T-028, T-029 — as demais rodam uma após a outra, ao final
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano aedus04-poous07-roteirizacao --paralelizar T-xxx,T-yyy` ou `--sequencial`
- tudo acontece na branch de trabalho `spec/aedus04-poous07-roteirizacao`; levar para a main é decisão sua

### Avisos

- ⚠ T-028 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-029 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-030 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-031 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-032 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2

#### faixa-1 — branch `spec/aedus04-poous07-roteirizacao-faixa-1` — worktree `../onp-worktrees/hemovia-backend-aedus04-poous07-roteirizacao-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-028 | Implementar Dijkstra e validação da janela de entrega | `gpt-6.1-terra` | medium | `api/src/main/java/com/hemovia/api/model/valueobject/GeoPoint.java`, `api/src/main/java/com/hemovia/api/model/algorithm/graph/RoutingGraph.java`, `api/src/main/java/com/hemovia/api/model/algorithm/graph/Dijkstra.java`, `api/src/main/java/com/hemovia/api/model/algorithm/graph/DeliveryWindow.java`, `api/src/main/java/com/hemovia/api/model/exception/InvalidRouteException.java`, `api/src/test/java/com/hemovia/api/model/algorithm/graph/DijkstraTest.java`, `api/src/test/java/com/hemovia/api/model/algorithm/graph/DeliveryWindowTest.java` |

#### faixa-2 — branch `spec/aedus04-poous07-roteirizacao-faixa-2` — worktree `../onp-worktrees/hemovia-backend-aedus04-poous07-roteirizacao-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-029 | Integrar OSRM e persistir a malha com trechos reais | `gpt-6.1-terra` | medium | `api/src/main/java/com/hemovia/api/model/RoadSegment.java`, `api/src/main/java/com/hemovia/api/model/entity/RouteNode.java`, `api/src/main/java/com/hemovia/api/model/entity/RouteEdge.java`, `api/src/main/java/com/hemovia/api/model/exception/RoutingProviderUnavailableException.java`, `api/src/main/java/com/hemovia/api/service/port/output/RoadRoutingProvider.java`, `api/src/main/java/com/hemovia/api/service/routing/OsrmRoadRoutingProvider.java`, `api/src/main/java/com/hemovia/api/service/config/RoutingProperties.java`, `api/src/main/java/com/hemovia/api/service/config/RoutingConfiguration.java`, `api/src/main/java/com/hemovia/api/service/usecase/route/ManageRouteGraph.java`, `api/src/main/java/com/hemovia/api/repository/port/RouteGraphStore.java`, `api/src/main/java/com/hemovia/api/repository/adapter/RouteGraphStoreAdapter.java`, `api/src/main/java/com/hemovia/api/repository/mapper/RoadGeometryCodec.java`, `api/src/main/java/com/hemovia/api/repository/jpa/RouteNodeRepository.java`, `api/src/main/java/com/hemovia/api/repository/jpa/RouteEdgeRepository.java`, `api/src/main/resources/db/migration/V15__associate_allocations_with_real_routes.sql`, `api/src/main/resources/application.yaml`, `api/.env.example`, `api/src/test/java/com/hemovia/api/service/routing/OsrmRoadRoutingProviderTest.java`, `api/src/main/java/com/hemovia/api/dto/internal/NewRouteEdge.java`, `api/src/main/java/com/hemovia/api/dto/internal/StoredRouteEdge.java` |

## Tarefas sequenciais (após as ondas, na árvore principal)

| tarefa | título | modelo | esforço | por que sequencial |
|---|---|---|---|---|
| T-030 | Expor cadastro de pontos e ligações no Swagger | `gpt-6.1-terra` | medium | fora da seleção do usuário |
| T-031 | Integrar cálculo e histórico da rota à alocação | `gpt-6.1-terra` | medium | fora da seleção do usuário |
| T-032 | Provar os fluxos com PostgreSQL e renovar a auditoria | `gpt-6.1-terra` | medium | fora da seleção do usuário |

## Gestão de branches e commits

1. branch de trabalho `spec/aedus04-poous07-roteirizacao` criada do ponto atual (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify aedus04-poous07-roteirizacao` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/aedus04-poous07-roteirizacao/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/hemovia-backend-aedus04-poous07-roteirizacao-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano aedus04-poous07-roteirizacao --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa aedus04-poous07-roteirizacao T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo aedus04-poous07-roteirizacao --tabela   # a tabela de andamento
onp-spec resumo aedus04-poous07-roteirizacao            # o resumo em texto
```

