# Plano de execução — poous10-cancelamento-requisicoes

> gerado por `onp-spec plano` em 2026-11-20 13:54 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano poous10-cancelamento-requisicoes --paralelizar T-025,T-026`

## Resumo — o que vai acontecer

- **3 tarefa(s) pendente(s)**: 2 em 2 faixa(s) paralela(s) + 1 sequencial(is)
- **seleção do usuário**: paralelizar só T-025, T-026 — as demais rodam uma após a outra, ao final
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano poous10-cancelamento-requisicoes --paralelizar T-xxx,T-yyy` ou `--sequencial`
- tudo acontece na branch de trabalho `spec/poous10-cancelamento-requisicoes`; levar para a main é decisão sua

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2

#### faixa-1 — branch `spec/poous10-cancelamento-requisicoes-faixa-1` — worktree `../onp-worktrees/hemovia-backend-poous10-cancelamento-requisicoes-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-025 | Implementar cancelamento e liberação transacional | `gpt-6.1-terra` | medium | `api/src/main/java/com/hemovia/api/service/usecase/request/CancelBloodRequest.java`, `api/src/main/java/com/hemovia/api/repository/port/RequestCancellationStore.java`, `api/src/main/java/com/hemovia/api/repository/adapter/RequestCancellationStoreAdapter.java`, `api/src/main/java/com/hemovia/api/repository/jpa/BloodRequestRepository.java`, `api/src/main/java/com/hemovia/api/repository/jpa/AllocationRepository.java`, `api/src/main/java/com/hemovia/api/repository/jpa/AllocationItemRepository.java`, `api/src/main/java/com/hemovia/api/repository/jpa/BloodBagRepository.java`, `api/src/main/java/com/hemovia/api/repository/jpa/TransportRepository.java` |

#### faixa-2 — branch `spec/poous10-cancelamento-requisicoes-faixa-2` — worktree `../onp-worktrees/hemovia-backend-poous10-cancelamento-requisicoes-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-026 | Expor o cancelamento autenticado e documentado no Swagger | `gpt-6.1-terra` | medium | `api/src/main/java/com/hemovia/api/controller/BloodRequestController.java`, `api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java`, `api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java` |

## Tarefas sequenciais (após as ondas, na árvore principal)

| tarefa | título | modelo | esforço | por que sequencial |
|---|---|---|---|---|
| T-027 | Provar cancelamento, rollback e concorrência com PostgreSQL | `gpt-6.1-terra` | medium | fora da seleção do usuário |

## Gestão de branches e commits

1. branch de trabalho `spec/poous10-cancelamento-requisicoes` criada do ponto atual (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify poous10-cancelamento-requisicoes` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/poous10-cancelamento-requisicoes/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/hemovia-backend-poous10-cancelamento-requisicoes-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano poous10-cancelamento-requisicoes --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa poous10-cancelamento-requisicoes T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo poous10-cancelamento-requisicoes --tabela   # a tabela de andamento
onp-spec resumo poous10-cancelamento-requisicoes            # o resumo em texto
```

