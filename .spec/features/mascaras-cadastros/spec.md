# Spec: Máscaras de CPF e telefone

> feature: mascaras-cadastros
> status: pronta

## Contexto

O usuário pediu máscaras nos cadastros: o telefone permite digitação ilimitada e o CPF de usuário rejeita colagem com pontos e traço. A correção abrange criação e edição de usuários, busca de CPF de doador e seu cadastro rápido. O contexto do produto é brasileiro (CPF e interface pt-BR).

## Histórias

### US-036 — Preencher documentos e contatos sem remover pontuação manualmente

Como operador, quero digitar ou colar CPF e telefone nos cadastros para preencher os dados de forma previsível.

#### AC-109 — CPF aceita digitação e colagem com máscara

- **Dado** o cadastro de usuário ou a busca/cadastro rápido de doador
- **Quando** o operador digita ou cola CPF com ou sem pontos e traço
- **Então** a tela exibe progressivamente 000.000.000-00, conserva até 11 dígitos sem cortar o CPF pontuado de 14 caracteres e envia/consulta apenas os dígitos; a validação existente dos dígitos verificadores permanece ativa.

#### AC-110 — Telefone brasileiro é limitado e formatado

- **Dado** um telefone de usuário ou do cadastro rápido de doador
- **Quando** o operador digita ou cola números com ou sem pontuação
- **Então** o campo limita a 11 dígitos e apresenta (81) 3333-4444 para 10 dígitos ou (81) 99999-9999 para 11, incluindo DDD; letras e dígitos excedentes não permanecem no campo.

#### AC-111 — Telefone opcional exige preenchimento completo quando informado

- **Dado** cadastro/edição de usuário ou cadastro rápido de doador
- **Quando** o operador salva com telefone vazio, completo ou incompleto
- **Então** vazio continua permitido, 10 ou 11 dígitos são enviados à API sem pontuação, preenchimento parcial impede o envio com mensagem compreensível, e o telefone já salvo aparece formatado ao abrir edição.

#### AC-112 — Máscaras continuam editáveis e preservam vínculo do doador

- **Dado** campos preenchidos ou um doador selecionado por CPF
- **Quando** o operador usa teclado para apagar, substituir ou colar valores e altera o CPF selecionado
- **Então** os campos podem ser corrigidos e limpos, o donorId anterior é invalidado e respostas atrasadas não o restauram; o campo misto de login continua aceitando e-mail e CPF.

#### AC-113 — Comprovar a correção nos formulários reais

- **Dado** a implementação e os testes de navegador existentes
- **Quando** a suíte de interface e o build são executados
- **Então** testes de comportamento comprovam colagem pontuada e sem máscara, limites, telefone fixo/celular/opcional/incompleto, edição, payloads normalizados e preservação do fluxo de doador sem falhas ou pulos; as provas da funcionalidade e o audit --ci terminam em zero.

## Fora de escopo

- Migrações, alterações dos endpoints ou novos formatos internacionais de telefone.
- Mudanças na identidade visual, no login misto ou nas permissões.
- Preencher automaticamente contatos ausentes ou modificar registros no banco de desenvolvimento para testar.

## Suposições

Nenhuma de produto pendente. CPF e telefone com DDD brasileiro seguem o contexto existente do aplicativo. A máscara é de apresentação; os payloads preservam o contrato de identificadores numéricos. O telefone permanece opcional conforme decisão já confirmada do usuário.

## Perguntas em aberto

Nenhuma. O usuário solicitou explicitamente ajustar as máscaras e a colagem. Modelo e execução permanecem conforme preferências já autorizadas: gpt-6.1-terra, esforço médio, tarefas sequenciais e commits locais; sem push.

## Execução e verificação

Alteração restrita ao frontend, com reaproveitamento de formatadores/controles e testes de interação. Não reduzir ou excluir testes existentes. Registrar cada critério em teste real com @spec e conservar o gate do projeto. Atualizar documentação e screenshot sintético, se produzido. O backend mantém suas regras atuais.
