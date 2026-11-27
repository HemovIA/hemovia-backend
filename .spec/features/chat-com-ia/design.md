# Arquitetura do chat com IA

## Interface

A Área do Doador exibe o chat e preserva o acesso aos dados da conta. O chat apresenta perguntas sugeridas, campo de texto, ação para enviar, indicação de espera, histórico da sessão e ação para iniciar nova conversa. A pessoa recebe os links oficiais junto da resposta. A interface usa os componentes, as cores e os estados de foco do produto, com suporte ao tema escuro e a telas estreitas. Falhas preservam a pergunta para nova tentativa e apresentam o FAQ oficial.

## Contrato entre frontend e backend

- A interface chama **POST /api/v1/donation-chat/messages** com os cookies e a proteção CSRF do cliente HTTP existente.
- O pedido contém **messages**, com até nove mensagens em ordem cronológica e no máximo mil caracteres por mensagem. Os papéis são **user** e **assistant**. A última mensagem é do usuário, com conteúdo após remover espaços nas extremidades. O cliente limita o histórico enviado, mantendo os diálogos mais recentes.
- A resposta contém **answer**, texto em português, e **sources**, lista de objetos com **title** e **url**. A interface renderiza **answer** como texto.
- Uma referência a contexto ausente pode gerar uma pergunta de esclarecimento. Falta de regra aplicável gera uma orientação segura com o FAQ e, quando pertinente, a lista oficial de hemocentros. Falha de conexão com o provedor retorna HTTP 503 com mensagem pública.

## Inferência e conteúdo

A API carrega um catálogo versionado de fichas oficiais com identificador, tema, texto revisado, aplicabilidade e referências. O modelo interpreta mensagens em português e seleciona identificadores do catálogo, um pedido de esclarecimento previsto ou um encaminhamento. A API valida a seleção contra o catálogo e monta a resposta usando textos e URLs da própria aplicação. Essa estrutura permite lidar com formulações livres e perguntas complementares e mantém prazos, condições e fontes revisáveis. Nunca se usa o texto livre do modelo como orientação. Resultados inválidos seguem o mesmo encaminhamento de casos em que não há base suficiente.

O contrato do provedor fica atrás de uma interface Java e usa configuração apenas no backend. O provedor escolhido é Ollama em rede local ou privada, com qwen2.5:7b como padrão configurável. Nenhum dado cadastral é incluído na solicitação de inferência. O texto da conversa fica fora de logs, rastreamento, banco de dados e armazenamento do navegador.

## Organização da execução e provas

A especificação fica no backend porque o verificador existente inclui os testes Playwright do frontend irmão. Backend e frontend podem avançar pelo contrato de API acima. O verificador conjunto roda depois de ambos. Os commits de código ficam no repositório correspondente, na branch de trabalho solicitada em cada repositório. O script gerado pela skill usa branches próprias de backend e caminhos relativos de worktree. Para manter as branches dos dois repositórios, seguir o plano por tarefa e usar os checkouts de cada repositório ao integrar.

Os testes da API cobrem autenticação, limites, minimização, respostas normativas e falhas de interpretação. Os testes Playwright cobrem entrada na Área do Doador, envio, links, follow-up, limpeza da conversa e recuperação de falha. O cenário de tatuagem percorre navegador, API e um servidor sintético com o contrato HTTP do Ollama. A execução com um modelo real depende de um Ollama disponível no ambiente.
