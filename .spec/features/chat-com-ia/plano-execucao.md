# Plano de execução — chat-com-ia

> gerado por `onp-spec plano` em 2026-11-27 18:10 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano chat-com-ia --paralelizar T-062,T-063 --modelo gpt-6.2-sol --esforco medium`

## Resumo — o que vai acontecer

- **3 tarefa(s) pendente(s)**: 2 em 2 faixa(s) paralela(s) + 1 sequencial(is)
- **seleção do usuário**: paralelizar só T-062, T-063 — as demais rodam uma após a outra, ao final
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano chat-com-ia --paralelizar T-xxx,T-yyy` ou `--sequencial`
- **custo travado pelo usuário**: modelo `gpt-6.2-sol` · esforço `medium` em TODAS as tarefas (vence tasks.md e config)
- tudo acontece na branch de trabalho `spec/chat-com-ia`; levar para a main é decisão sua

### Avisos

- ⚠ T-062 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-063 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-064 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2

#### faixa-1 — branch `spec/chat-com-ia-faixa-1` — worktree `../onp-worktrees/hemovia-backend-chat-com-ia-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-062 | Criar a base oficial e a API de conversa | `gpt-6.2-sol` | medium | `api/src/main/resources/donation-chat/catalog.json`, `api/src/main/resources/application.yaml`, `api/src/main/java/com/hemovia/api/controller/DonationChatController.java`, `api/src/main/java/com/hemovia/api/controller/config/DonationChatRequestConfiguration.java`, `api/src/main/java/com/hemovia/api/controller/security/SecurityConfig.java`, `api/src/main/java/com/hemovia/api/controller/handler/GlobalExceptionHandler.java`, `api/src/main/java/com/hemovia/api/dto/request/DonationChatRequest.java`, `api/src/main/java/com/hemovia/api/dto/response/DonationChatResponse.java`, `api/src/main/java/com/hemovia/api/service/config/DonationChatConfiguration.java`, `api/src/main/java/com/hemovia/api/service/config/DonationChatProperties.java`, `api/src/main/java/com/hemovia/api/service/chat/DonationChatCatalog.java`, `api/src/main/java/com/hemovia/api/service/chat/ModelDonationChatInterpreter.java`, `api/src/main/java/com/hemovia/api/service/port/output/DonationChatInterpreter.java`, `api/src/main/java/com/hemovia/api/service/usecase/chat/AnswerDonationQuestion.java`, `api/src/main/java/com/hemovia/api/model/exception/DonationChatUnavailableException.java`, `api/src/test/java/com/hemovia/api/controller/DonationChatIntegrationTest.java`, `api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java`, `api/src/test/java/com/hemovia/api/service/chat/DonationChatCatalogTest.java`, `api/src/test/java/com/hemovia/api/service/chat/ModelDonationChatInterpreterTest.java`, `scripts/spec-java/acceptance-cases.json`, `api/.env.example`, `README.md` |

#### faixa-2 — branch `spec/chat-com-ia-faixa-2` — worktree `../onp-worktrees/hemovia-backend-chat-com-ia-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-063 | Integrar o chat à Área do Doador | `gpt-6.2-sol` | medium | `../hemovia-frontend/src/donor-account.tsx`, `../hemovia-frontend/src/donor-chat.tsx`, `../hemovia-frontend/src/http.ts`, `../hemovia-frontend/src/styles.css`, `../hemovia-frontend/tests/donor-chat.spec.ts` |

## Tarefas sequenciais (após as ondas, na árvore principal)

| tarefa | título | modelo | esforço | por que sequencial |
|---|---|---|---|---|
| T-064 | Validar a integração e registrar as provas | `gpt-6.2-sol` | medium | fora da seleção do usuário |

## Gestão de branches e commits

1. branch de trabalho `spec/chat-com-ia` criada do ponto atual (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify chat-com-ia` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/chat-com-ia/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/hemovia-backend-chat-com-ia-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano chat-com-ia --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa chat-com-ia T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo chat-com-ia --tabela   # a tabela de andamento
onp-spec resumo chat-com-ia            # o resumo em texto
```

