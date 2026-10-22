# Plano de execução — finalizando-entidades-base

> gerado por `onp-spec plano` em 2026-10-22 18:28 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano finalizando-entidades-base --sequencial --modelo gpt-5.6-terra --esforco medium`

## Resumo — o que vai acontecer

- **modo SEQUENCIAL (escolha do usuário)**: 8 tarefa(s) pendente(s), UMA APÓS A OUTRA, na árvore principal
- sem worktrees e sem paralelismo — cada tarefa roda numa janela de contexto limpa, na ordem do tasks.md
- **custo travado pelo usuário**: modelo `gpt-5.6-terra` · esforço `medium` em TODAS as tarefas (vence tasks.md e config)
- tudo acontece na branch de trabalho `spec/finalizando-entidades-base`; levar para a main é decisão sua

## Ordem de execução (uma tarefa após a outra)

| tarefa | título | modelo | esforço |
|---|---|---|---|
| T-005 | Preparar provas Java/PostgreSQL e recuperar a validação histórica | `gpt-5.6-terra` | medium |
| T-006 | Consolidar pessoas, locais, veículos e tipos comuns | `gpt-5.6-terra` | medium |
| T-007 | Evoluir doações e bolsas com compatibilidade dos contratos | `gpt-5.6-terra` | medium |
| T-008 | Modelar requisições, alocações e histórico de reservas | `gpt-5.6-terra` | medium |
| T-009 | Modelar rotas e seus pontos ordenados | `gpt-5.6-terra` | medium |
| T-010 | Modelar transporte, telemetria e movimentos de estoque | `gpt-5.6-terra` | medium |
| T-011 | Migrar e provar o esquema PostgreSQL | `gpt-5.6-terra` | medium |
| T-012 | Consolidar regressão, documentação e auditoria | `gpt-5.6-terra` | medium |

## Gestão de branches e commits

1. branch de trabalho `spec/finalizando-entidades-base` criada do ponto atual (se ainda não existir)
2. as tarefas rodam nela mesma, na ordem — **1 tarefa = 1 commit** (`T-xxx feature: título`), marcada `[concluida]` só com trabalho feito
3. gate final na branch de trabalho: `onp-spec verify finalizando-entidades-base` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/finalizando-entidades-base/executar-tarefas.sh
```

Cada tarefa roda `codex exec` com **janela de contexto limpa**, na árvore principal,
uma após a outra, com `--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`.
Os prompts exatos estão embutidos no script.
Logs: `../onp-worktrees/hemovia-backend-finalizando-entidades-base-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano finalizando-entidades-base --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa finalizando-entidades-base T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo finalizando-entidades-base --tabela   # a tabela de andamento
onp-spec resumo finalizando-entidades-base            # o resumo em texto
```

