# Projeto do cadastro público

## Cadastro e confirmação

1. O formulário do login envia o e-mail para `POST /api/v1/auth/register`. A API devolve `202` com o mesmo resultado público para e-mails novos e já cadastrados. Um e-mail elegível recebe um link para a interface, por meio de SMTP configurado no backend.
2. O link leva um token aleatório de curta duração no fragmento da URL. O backend armazena seu hash e o e-mail associado. A abertura do link mostra o formulário. O envio explícito para `POST /api/v1/auth/register/confirm` apresenta token, nome, CPF e senha.
3. Na confirmação, o backend usa o e-mail associado ao token, valida o CPF e consulta `Person` pelo CPF normalizado. Quando o e-mail confirmado corresponde ao do registro, salva o vínculo. Quando o CPF não consta no cadastro operacional, a conta fica sem vínculo. Quando o e-mail do registro diverge ou está ausente, a confirmação orienta a pessoa a procurar o hemocentro e não cria a conta.
4. O backend consome o token e cria a conta na mesma transação. Restrições de unicidade protegem a conta contra submissões duplicadas e tentativas simultâneas. A confirmação responde sem expor o vínculo ou o histórico. Após criar a conta, o usuário entra pelo login existente.

## Permissões e integração

- A conta pública recebe o papel `DOADOR`. A regra de segurança continua protegendo as rotas operacionais, e a sessão do doador usa apenas os endpoints de autenticação existentes.
- O vínculo fica em uma chave estrangeira opcional e única de `users` para `person`. A migração preserva os usuários e doadores existentes.
- O formulário de cadastro se integra à tela de login. Uma sessão de doador recebe apenas uma confirmação de acesso e uma ação de saída até a implementação da área de doador.
- A URL pública do frontend e o envio de e-mail são configurados por ambiente. Os testes usam dados sintéticos e um transporte de e-mail controlado.
