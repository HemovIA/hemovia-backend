# Plano de execução — poous09-poous11-transporte-entrega

> gerado por `onp-spec plano` em 2026-11-20 20:43 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano poous09-poous11-transporte-entrega --paralelizar T-033,T-034 --modelo gpt-6.1-terra --esforco medium`

## Resumo — o que vai acontecer

- **4 tarefa(s) pendente(s)**: 2 em 2 faixa(s) paralela(s) + 2 sequencial(is)
- **seleção do usuário**: paralelizar só T-033, T-034 — as demais rodam uma após a outra, ao final
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano poous09-poous11-transporte-entrega --paralelizar T-xxx,T-yyy` ou `--sequencial`
- **custo travado pelo usuário**: modelo `gpt-6.1-terra` · esforço `medium` em TODAS as tarefas (vence tasks.md e config)
- tudo acontece na branch de trabalho `spec/poous09-poous11-transporte-entrega`; levar para a main é decisão sua

### Avisos

- ⚠ T-033 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-034 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-035 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-036 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2

#### faixa-1 — branch `spec/poous09-poous11-transporte-entrega-faixa-1` — worktree `../onp-worktrees/hemovia-backend-poous09-poous11-transporte-entrega-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-033 | Implementar início e conclusão transacionais | `gpt-6.1-terra` | medium | `api/src/main/java/com/hemovia/api/service/usecase/transport/StartTransport.java`, `api/src/main/java/com/hemovia/api/service/usecase/transport/CompleteTransport.java`, `api/src/main/java/com/hemovia/api/service/usecase/transport/TransportLifecycle.java`, `api/src/main/java/com/hemovia/api/repository/port/TransportLifecycleStore.java`, `api/src/main/java/com/hemovia/api/repository/adapter/TransportLifecycleStoreAdapter.java`, `api/src/main/java/com/hemovia/api/repository/jpa/TransportRepository.java`, `api/src/main/java/com/hemovia/api/repository/jpa/AllocationRepository.java`, `api/src/main/java/com/hemovia/api/repository/jpa/VehicleRepository.java`, `api/src/main/java/com/hemovia/api/model/exception/VehicleUnavailableException.java`, `api/src/main/java/com/hemovia/api/dto/request/StartTransportRequest.java`, `api/src/main/java/com/hemovia/api/dto/response/TransportResponse.java`, `api/src/main/resources/db/migration/V16__one_active_transport_per_vehicle.sql` |

#### faixa-2 — branch `spec/poous09-poous11-transporte-entrega-faixa-2` — worktree `../onp-worktrees/hemovia-backend-poous09-poous11-transporte-entrega-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-034 | Disponibilizar cadastro e listagem de veículos | `gpt-6.1-terra` | medium | `api/src/main/java/com/hemovia/api/service/usecase/vehicle/ManageVehicles.java`, `api/src/main/java/com/hemovia/api/dto/request/VehicleRequest.java`, `api/src/main/java/com/hemovia/api/dto/response/VehicleResponse.java`, `api/src/main/java/com/hemovia/api/controller/VehicleController.java`, `api/src/test/java/com/hemovia/api/service/usecase/vehicle/ManageVehiclesTest.java` |

## Tarefas sequenciais (após as ondas, na árvore principal)

| tarefa | título | modelo | esforço | por que sequencial |
|---|---|---|---|---|
| T-035 | Expor transporte com segurança e Swagger | `gpt-6.1-terra` | medium | fora da seleção do usuário |
| T-036 | Provar o ciclo e renovar a auditoria | `gpt-6.1-terra` | medium | fora da seleção do usuário |

## Gestão de branches e commits

1. branch de trabalho `spec/poous09-poous11-transporte-entrega` criada do ponto atual (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify poous09-poous11-transporte-entrega` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/poous09-poous11-transporte-entrega/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/hemovia-backend-poous09-poous11-transporte-entrega-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano poous09-poous11-transporte-entrega --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa poous09-poous11-transporte-entrega T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo poous09-poous11-transporte-entrega --tabela   # a tabela de andamento
onp-spec resumo poous09-poous11-transporte-entrega            # o resumo em texto
```

