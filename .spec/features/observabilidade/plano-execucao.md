# Plano de execução — observabilidade

> gerado por `onp-spec plano` em 2026-11-22 17:21 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano observabilidade --sequencial --modelo gpt-6.1-terra --esforco medium`

## Resumo — o que vai acontecer

- **modo SEQUENCIAL (escolha do usuário)**: 4 tarefa(s) pendente(s), UMA APÓS A OUTRA, na árvore principal
- sem worktrees e sem paralelismo — cada tarefa roda numa janela de contexto limpa, na ordem do tasks.md
- **custo travado pelo usuário**: modelo `gpt-6.1-terra` · esforço `medium` em TODAS as tarefas (vence tasks.md e config)
- tudo acontece na branch de trabalho `spec/observabilidade`; levar para a main é decisão sua

### Avisos

- ⚠ T-055 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-056 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-057 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-058 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar

## Ordem de execução (uma tarefa após a outra)

| tarefa | título | modelo | esforço |
|---|---|---|---|
| T-055 | Instrumentar a API e preservar os contratos | `gpt-6.1-terra` | medium |
| T-056 | Montar e provisionar a stack Docker | `gpt-6.1-terra` | medium |
| T-057 | Preparar carga e verificação ponta a ponta | `gpt-6.1-terra` | medium |
| T-058 | Preparar acesso, evidências e relatório | `gpt-6.1-terra` | medium |

## Gestão de branches e commits

1. branch de trabalho `spec/observabilidade` criada do ponto atual (se ainda não existir)
2. as tarefas rodam nela mesma, na ordem — **1 tarefa = 1 commit** (`T-xxx feature: título`), marcada `[concluida]` só com trabalho feito
3. gate final na branch de trabalho: `onp-spec verify observabilidade` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/observabilidade/executar-tarefas.sh
```

Cada tarefa roda `codex exec` com **janela de contexto limpa**, na árvore principal,
uma após a outra, com `--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`.
Os prompts exatos estão embutidos no script.
Logs: `../onp-worktrees/hemovia-backend-observabilidade-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano observabilidade --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa observabilidade T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo observabilidade --tabela   # a tabela de andamento
onp-spec resumo observabilidade            # o resumo em texto
```

