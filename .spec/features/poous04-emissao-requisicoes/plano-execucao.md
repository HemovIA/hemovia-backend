# Plano de execução — poous04-emissao-requisicoes

> gerado por `onp-spec plano` em 2026-11-09 17:39 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano poous04-emissao-requisicoes --paralelizar T-016,T-017`

## Resumo — o que vai acontecer

- **2 tarefa(s) pendente(s)**: 2 em 2 faixa(s) paralela(s) + 0 sequencial(is)
- **seleção do usuário**: paralelizar só T-016, T-017 — as demais rodam uma após a outra, ao final
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano poous04-emissao-requisicoes --paralelizar T-xxx,T-yyy` ou `--sequencial`
- tudo acontece na branch de trabalho `spec/poous04-emissao-requisicoes`; levar para a main é decisão sua

### Avisos

- ⚠ T-016 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-017 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2

#### faixa-1 — branch `spec/poous04-emissao-requisicoes-faixa-1` — worktree `../onp-worktrees/hemovia-backend-poous04-emissao-requisicoes-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-016 | Implementar a emissão e verificar os cenários HTTP | `gpt-6.1-terra` | medium | `api/src/main/java/com/hemovia/api/model/BloodRequest.java`, `api/src/main/java/com/hemovia/api/domain/enums/RequestPriority.java`, `api/src/main/java/com/hemovia/api/application/dto/request/CreateBloodRequest.java`, `api/src/main/java/com/hemovia/api/application/dto/response/BloodRequestResponse.java`, `api/src/main/java/com/hemovia/api/application/port/input/CreateBloodRequestUseCase.java`, `api/src/main/java/com/hemovia/api/application/port/output/BloodRequestStore.java`, `api/src/main/java/com/hemovia/api/application/usecase/request/CreateHospitalBloodRequest.java`, `api/src/main/java/com/hemovia/api/infrastructure/persistence/adapter/BloodRequestStoreAdapter.java`, `api/src/main/java/com/hemovia/api/infrastructure/security/SecurityConfig.java`, `api/src/main/java/com/hemovia/api/presentation/controller/BloodRequestController.java`, `api/src/main/resources/db/migration/V11__add_request_priority_and_deadline.sql`, `api/src/test/java/com/hemovia/api/presentation/controller/BloodRequestAcceptanceTest.java` |

#### faixa-2 — branch `spec/poous04-emissao-requisicoes-faixa-2` — worktree `../onp-worktrees/hemovia-backend-poous04-emissao-requisicoes-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-017 | Verificar o contrato OpenAPI e ligar os testes à auditoria | `gpt-6.1-terra` | medium | `api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java`, `scripts/spec-java/acceptance-cases.json`, `onpspec.config.json` |

## Gestão de branches e commits

1. branch de trabalho `spec/poous04-emissao-requisicoes` criada do ponto atual (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify poous04-emissao-requisicoes` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/poous04-emissao-requisicoes/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/hemovia-backend-poous04-emissao-requisicoes-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano poous04-emissao-requisicoes --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa poous04-emissao-requisicoes T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo poous04-emissao-requisicoes --tabela   # a tabela de andamento
onp-spec resumo poous04-emissao-requisicoes            # o resumo em texto
```

