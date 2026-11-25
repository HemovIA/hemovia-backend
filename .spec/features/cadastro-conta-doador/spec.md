# Cadastro público de conta de doador

> feature: cadastro-conta-doador
> status: em-implementacao

## Contexto

A API autentica contas existentes e reserva a criação de usuários a administradores. O cadastro operacional de doadores usa a entidade `Person`, separada do usuário e referenciada pelas doações. A tela de login ainda precisa oferecer a criação de uma conta pública e o vínculo com um cadastro anterior.

## Histórias

### US-040 — Criar uma conta a partir da tela de login

Como pessoa interessada em doar, quero criar minha conta pela tela de login para poder usar a área de doador quando ela estiver disponível.

#### AC-124 — Iniciar cadastro sem uma sessão

- **Dado** que a pessoa está na tela de login sem uma sessão
- **Quando** ela escolhe Criar conta e envia um e-mail válido
- **Então** a tela orienta sobre a confirmação, a API responde com a mesma mensagem para e-mails novos e já cadastrados e a solicitação por si só não cria uma conta nem inicia uma sessão.

#### AC-125 — Confirmar e-mail e escolher uma senha

- **Dado** um link de confirmação válido recebido no e-mail
- **Quando** a pessoa abre o link e conclui o formulário com nome, CPF e senha
- **Então** o backend cria uma conta com o papel de doador e o e-mail verificado, grava a senha como hash e o login existente aceita a nova conta. Link expirado, adulterado ou já utilizado não cria outra conta.

#### AC-126 — Manter o cadastro público separado da operação

- **Dado** o fluxo público de cadastro
- **Quando** uma solicitação fornece campos extras de papel ou de vínculo, ou uma conta de doador autenticada acessa uma rota operacional
- **Então** nenhum papel ou vínculo operacional é atribuído e as rotas operacionais continuam restritas. Os papéis e os logins existentes continuam funcionando.

### US-041 — Conectar a conta ao cadastro de doador existente

Como pessoa já cadastrada como doadora, quero que minha conta fique associada ao meu cadastro existente para que a área de doador possa consultar os meus dados no futuro.

#### AC-127 — Vincular pelo CPF e pelo e-mail confirmado

- **Dado** um cadastro de doador com CPF e e-mail correspondentes aos da pessoa que confirmou o e-mail
- **Quando** ela conclui a criação da conta
- **Então** o usuário referencia o identificador do cadastro existente, inclusive quando o CPF tem máscara ou o e-mail usa outra capitalização, preservando a pessoa e suas doações.

#### AC-128 — Criar conta para quem ainda não tem cadastro de doador

- **Dado** um e-mail confirmado e um CPF sem cadastro de doador
- **Quando** a pessoa conclui a criação da conta
- **Então** ela recebe uma conta de doador sem vínculo e o fluxo não cria um cadastro de doador operacional.

#### AC-129 — Proteger a identidade e evitar duplicatas

- **Dado** um CPF já associado a outro cadastro de doador, uma conta existente com o mesmo CPF ou e-mail ou duas tentativas concorrentes
- **Quando** o cadastro público é concluído
- **Então** divergência ou ausência de e-mail no cadastro de doador impede a criação e orienta a pessoa a procurar o hemocentro. A API preserva as contas e os vínculos existentes, com no máximo uma conta por CPF, e-mail e cadastro de doador.

#### AC-130 — Concluir o cadastro na interface

- **Dado** um link de confirmação recebido por e-mail
- **Quando** a pessoa chega à interface de confirmação, corrige dados inválidos e conclui o cadastro
- **Então** a tela oferece os campos e mensagens adequados e orienta a entrar com a nova conta. A sessão de doador oferece saída e não abre o painel operacional.

#### AC-131 — Preservar o cadastro persistido e os papéis existentes

- **Dado** um banco com usuários operacionais e doadores criados antes da mudança
- **Quando** a API atualiza o esquema e grava uma conta de doador
- **Então** os usuários antigos continuam disponíveis, a conta aceita um vínculo opcional com uma pessoa existente e o banco recusa dois usuários vinculados à mesma pessoa.

## Limites da entrega

- Inclui o formulário público, a confirmação por e-mail e a persistência do vínculo.
- A área de doador e os endpoints de acesso ao histórico de doações ficam para uma próxima entrega.

## Suposições

Nenhuma.

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-025 | Como comprovar o controle sobre o e-mail? | respondida | Enviar um link de confirmação por e-mail. |
| Q-026 | Como proceder quando o CPF consta em um cadastro de doador com e-mail ausente ou divergente? | respondida | Orientar a pessoa a atualizar o cadastro no hemocentro antes de concluir. |
| Q-027 | Qual ordem de execução, modelo e esforço usar por tarefa? | respondida | Sequencial com GPT-6.2 Sol e esforço médio nas três tarefas. |
