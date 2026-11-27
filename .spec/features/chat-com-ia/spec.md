# Chat com IA sobre doação de sangue

> feature: chat-com-ia
> status: auditada

## Contexto

Uma pessoa cadastrada como doadora, inclusive antes de fazer a primeira doação, pode tirar dúvidas em português na Área do Doador. O chat explica os critérios nacionais de doação de sangue total com fontes oficiais e orienta a pessoa a procurar o hemocentro quando sua situação depende de avaliação.

## Histórias

### US-042 — Conversar na Área do Doador

Como pessoa interessada em doar, quero perguntar na minha tela principal e continuar a conversa para entender como me preparar.

#### AC-132 — Acesso pela área autenticada

- **Dado** um usuário autenticado com o papel de doador, inclusive antes da primeira doação
- **Quando** abre a Área do Doador e envia uma pergunta
- **Então** vê a pergunta, a resposta em português e os links oficiais correspondentes
- **E** a API exige autenticação, papel de doador e proteção contra requisições forjadas conforme a sessão atual

#### AC-133 — Continuidade e nova conversa

- **Dado** que a pessoa já fez uma pergunta
- **Quando** envia uma pergunta complementar na mesma conversa
- **Então** o chat usa as mensagens recentes para interpretar a dúvida
- **E** a pessoa pode iniciar uma nova conversa e a tela abre vazia ao recarregar ou encerrar a sessão

#### AC-134 — Erro recuperável

- **Dado** que a API do assistente está temporariamente indisponível
- **Quando** a pessoa tenta enviar uma mensagem
- **Então** a tela informa a falha, preserva o texto para nova tentativa e oferece um link para o FAQ do Ministério da Saúde

### US-043 — Receber orientação com fonte oficial

Como pessoa que quer doar, quero saber qual regra se aplica ao meu caso e onde posso confirmá-la.

#### AC-135 — Tatuagem há três meses

- **Dado** que a pessoa fez uma tatuagem há três meses e o hemocentro ainda não avaliou as condições do procedimento
- **Quando** pergunta se o prazo impede a doação
- **Então** o chat explica os prazos de sete dias quando a segurança pode ser avaliada e quatro meses quando ela não pode ser avaliada, cita a referência vigente e orienta a confirmar com o hemocentro
- **E** informa que três meses ficam aquém dos quatro meses quando o serviço não consegue avaliar a segurança

#### AC-136 — Dúvida sem base suficiente

- **Dado** que a pergunta está fora de escopo, exige avaliação individual ou não encontra regra na base oficial
- **Quando** a pessoa pede uma conclusão sobre a doação
- **Então** o chat explica que a conclusão depende da triagem ou indica o canal adequado, sem inventar prazo, link ou autorização para doar

#### AC-137 — Cobertura inicial da base

- **Dado** que uma pergunta aborda requisitos básicos, intervalos, preparação, procedimentos recentes, vacinas, medicamentos, condições de saúde ou onde doar
- **Quando** há uma regra aplicável no catálogo
- **Então** a resposta usa a referência oficial correspondente e encaminha para avaliação do hemocentro quando o critério depende de detalhes do caso

### US-044 — Usar o chat com privacidade

Como pessoa que pode mencionar informações de saúde, quero que minhas mensagens sejam usadas apenas para responder na sessão em andamento.

#### AC-138 — Limites e minimização de dados

- **Dado** que a pessoa usa o chat com sua sessão autenticada
- **Quando** a API recebe mensagens válidas, em branco ou acima dos limites
- **Então** a API envia ao modelo só as mensagens necessárias para a pergunta, sem incluir dados cadastrais da sessão, e rejeita entradas inválidas antes da inferência
- **E** o aplicativo mantém o histórico em memória e exclui o conteúdo de mensagens de seus logs e rastreamentos

#### AC-139 — Resposta controlada pelo catálogo

- **Dado** que o resultado da inferência pede conteúdo fora do catálogo, inclui referências desconhecidas ou chega malformado
- **Quando** a API processa esse resultado
- **Então** ela retorna orientação de encaminhamento com links oficiais conhecidos, sem exibir texto livre do modelo

## Escopo da primeira versão

- Orientação sobre doação de sangue total no Brasil e indicação de onde encontrar um hemocentro.
- A decisão de aptidão é tomada pelo profissional de saúde do serviço de hemoterapia durante a triagem presencial.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-016 | Pessoas que nunca doaram acessam o chat após criar a conta de doador. A conversa dura até recarregar a página, iniciar outra conversa ou sair. | confirmada | O dono do produto aprovou a proposta. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-028 | Qual provedor deve executar a IA do aplicativo? | respondida | Ollama local ou em rede privada. |
| Q-029 | Quais tarefas podem rodar em paralelo? | respondida | Backend e frontend em paralelo, com integração ao final. |
| Q-030 | Quais modelos do Codex e níveis de esforço estão aprovados para executar as tarefas? | respondida | GPT-6.2 Sol com esforço médio nas três tarefas. |
