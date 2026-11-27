# Plano de execução — estus01-estus02-estatisticas

> gerado por `onp-spec plano` em 2026-11-20 22:17 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano estus01-estus02-estatisticas --paralelizar T-037,T-038 --modelo gpt-6.1-terra --esforco medium`

## Resumo — o que vai acontecer

- **4 tarefa(s) pendente(s)**: 2 em 2 faixa(s) paralela(s) + 2 sequencial(is)
- **seleção do usuário**: paralelizar só T-037, T-038 — as demais rodam uma após a outra, ao final
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano estus01-estus02-estatisticas --paralelizar T-xxx,T-yyy` ou `--sequencial`
- **custo travado pelo usuário**: modelo `gpt-6.1-terra` · esforço `medium` em TODAS as tarefas (vence tasks.md e config)
- tudo acontece na branch de trabalho `spec/estus01-estus02-estatisticas`; levar para a main é decisão sua

### Avisos

- ⚠ T-037 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-038 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-039 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-040 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2

#### faixa-1 — branch `spec/estus01-estus02-estatisticas-faixa-1` — worktree `../onp-worktrees/hemovia-backend-estus01-estus02-estatisticas-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-037 | Implementar cálculos estatísticos e percentuais | `gpt-6.1-terra` | medium | `api/src/main/java/com/hemovia/api/model/algorithm/statistics/Frequency.java`, `api/src/main/java/com/hemovia/api/model/algorithm/statistics/DescriptiveStatistics.java`, `api/src/main/java/com/hemovia/api/model/algorithm/statistics/PercentageDistribution.java`, `api/src/test/java/com/hemovia/api/model/algorithm/statistics/DescriptiveStatisticsTest.java`, `api/src/test/java/com/hemovia/api/model/algorithm/statistics/PercentageDistributionTest.java` |

#### faixa-2 — branch `spec/estus01-estus02-estatisticas-faixa-2` — worktree `../onp-worktrees/hemovia-backend-estus01-estus02-estatisticas-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-038 | Consultar estoque e demanda por agregação | `gpt-6.1-terra` | medium | `api/src/main/java/com/hemovia/api/dto/internal/InventoryCount.java`, `api/src/main/java/com/hemovia/api/dto/internal/DemandCount.java`, `api/src/main/java/com/hemovia/api/repository/port/StatisticsStore.java`, `api/src/main/java/com/hemovia/api/repository/adapter/StatisticsStoreAdapter.java` |

## Tarefas sequenciais (após as ondas, na árvore principal)

| tarefa | título | modelo | esforço | por que sequencial |
|---|---|---|---|---|
| T-039 | Expor indicadores com serviços, segurança e Swagger | `gpt-6.1-terra` | medium | fora da seleção do usuário |
| T-040 | Provar estatísticas e atualizar guia e auditoria | `gpt-6.1-terra` | medium | fora da seleção do usuário |

## Gestão de branches e commits

1. branch de trabalho `spec/estus01-estus02-estatisticas` criada do ponto atual (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify estus01-estus02-estatisticas` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/estus01-estus02-estatisticas/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/hemovia-backend-estus01-estus02-estatisticas-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano estus01-estus02-estatisticas --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa estus01-estus02-estatisticas T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo estus01-estus02-estatisticas --tabela   # a tabela de andamento
onp-spec resumo estus01-estus02-estatisticas            # o resumo em texto
```

