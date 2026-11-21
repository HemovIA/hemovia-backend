# Spec: Formulários assistidos e validação integrada

> feature: formularios-assistidos
> status: pronta

## Contexto

O usuário quer validar o ambiente integrado sem preencher UUIDs manualmente nos formulários. Solicitou listas ou pesquisa filtrada de cadastros existentes e busca de doador por CPF, com caminho de cadastro quando não encontrado. Inclui conectar as leituras persistidas já implementadas para eliminar os identificadores manuais restantes da operação.

## Histórias

### US-032 — Encontrar e cadastrar doador por CPF

Como operador, quero encontrar e cadastrar doador por cpf para preencher e retomar a operação pelos dados existentes.

#### AC-094 — Cadastrar doador sem conta

- **Dado** um operador ADMIN ou HEMOCENTRO autenticado
- **Quando** cadastra nome e CPF válido, com ou sem e-mail e telefone
- **Então** o doador é persistido como Person, sem conta de acesso; CPF é normalizado e único; nome e CPF são obrigatórios, contatos são opcionais e os registros legados sem CPF permanecem inalterados.

#### AC-095 — Buscar CPF exato

- **Dado** um CPF completo com ou sem máscara
- **Quando** ADMIN ou HEMOCENTRO consulta o doador pelo CPF
- **Então** a API retorna somente o doador correspondente; inválido resulta 400, inexistente 404, duplicidade de cadastro 409, sessão ausente 401 e HOSPITAL ou TRANSPORTADOR 403.

#### AC-096 — Vincular doador encontrado

- **Dado** um doador existente e formulário de doação ou bolsa aberto
- **Quando** o operador informa CPF válido e seleciona o resultado
- **Então** a interface mostra nome e CPF identificado e envia donorId correspondente sem solicitar UUID; editar o CPF invalida a seleção anterior e respostas atrasadas não substituem uma busca mais recente.

#### AC-097 — Cadastrar sem perder formulário

- **Dado** um CPF válido não cadastrado e dados já preenchidos
- **Quando** o operador clica Cadastrar doador e confirma nome e contatos opcionais
- **Então** o cadastro rápido reutiliza o CPF, preserva os dados da doação ou bolsa e seleciona o doador criado; falha ou CPF duplicado não gera cadastro fictício nem apaga o formulário.

### US-033 — Selecionar registros existentes nos formulários

Como operador, quero selecionar registros existentes nos formulários para preencher e retomar a operação pelos dados existentes.

#### AC-098 — Pesquisar hemocentro

- **Dado** hemocentros ativos disponíveis e formulário de doação ou bolsa aberto
- **Quando** o operador procura e seleciona um hemocentro pelo nome
- **Então** a interface exibe nome e cidade, envia bloodCenterId selecionado e preseleciona o vínculo da sessão quando disponível, sem exigir UUID digitado.

#### AC-099 — Preencher centro de coleta

- **Dado** um hemocentro selecionado no registro de doação
- **Quando** a seleção é feita ou alterada antes de uma edição manual do local
- **Então** o centro de coleta é preenchido pelo nome do hemocentro, permite edição para coleta externa e não sobrescreve uma edição manual já feita.

#### AC-100 — Selecionar bolsa existente

- **Dado** bolsas já cadastradas
- **Quando** o operador escolhe vincular bolsa existente e pesquisa pelo código exibido, tipo sanguíneo ou componente
- **Então** uma bolsa existente pode ser selecionada sem copiar UUID e o payload envia seu bloodBagId; texto livre sem seleção não é aceito como identificador.

#### AC-101 — Selecionar hospital, veículo e unidade

- **Dado** cadastros disponíveis
- **Quando** o operador preenche hospital de destino, vínculo de novo usuário, unidade de ponto logístico ou veículo de transporte
- **Então** os campos oferecem seleção pesquisável por nome ou código, enviam o identificador da opção escolhida e limpam seleções incompatíveis ao trocar o perfil ou tipo de unidade.

#### AC-102 — Consultar todas as páginas e estados

- **Dado** catálogo maior que uma página, vazio ou indisponível
- **Quando** o operador abre um seletor, pesquisa ou navega pelos resultados
- **Então** os registros fora da primeira página continuam acessíveis por busca ou paginação; carregamento, nenhum resultado, erro e nova tentativa são explícitos; uma falha de consulta nunca é apresentada como inexistência.

#### AC-103 — Operar seletores por teclado

- **Dado** um seletor pesquisável em foco
- **Quando** o operador usa teclado para pesquisar, escolher ou limpar
- **Então** a opção escolhida fica identificada por rótulo acessível; controles têm foco visível e não dependem apenas do mouse, nem enviam o texto da busca como UUID.

### US-034 — Retomar a operação com dados do backend

Como operador, quero retomar a operação com dados do backend para preencher e retomar a operação pelos dados existentes.

#### AC-104 — Carregar pedidos e viagens persistidos

- **Dado** pedidos, alocações e transportes persistidos e sessão real
- **Quando** o operador abre listas ou detalhe, recarrega a página ou retorna após uma mutação
- **Então** as telas recuperam os dados pelas novas consultas paginadas e detalhes da API, respeitam seu escopo e permitem alocar, iniciar transporte e receber entrega sem digitar allocationId ou transportId.

#### AC-105 — Carregar hospitais cadastrados

- **Dado** hospitais persistidos e cadastro administrativo aberto
- **Quando** o operador consulta ou cria hospital
- **Então** a lista utiliza a resposta paginada real, inclui o novo registro após atualização e não depende apenas dos comprovantes desta sessão.

#### AC-106 — Preservar modo demonstrativo

- **Dado** a aplicação no modo demonstrativo
- **Quando** o usuário pesquisa referências, cadastra doador ou percorre pedido e transporte
- **Então** os fluxos equivalentes funcionam com dados sintéticos locais, sem chamar API real e mantendo a distinção visual do modo demonstrativo.

### US-035 — Validar o fluxo integrado

Como operador, quero validar o fluxo integrado para preencher e retomar a operação pelos dados existentes.

#### AC-107 — Comprovar contratos e formulários

- **Dado** a implementação e um ambiente de teste isolado
- **Quando** os testes backend e frontend são executados
- **Então** os testes comprovam migração preservando pessoas legadas, CPF normalizado e único, contatos opcionais, permissões, seleção por identificadores reais, fluxo de cadastro rápido e adaptação dos contratos paginados, sem pulos ou provas fabricadas.

#### AC-108 — Validar interface integrada

- **Dado** backend e frontend disponíveis em ambiente local de teste com dados sintéticos
- **Quando** o fluxo é exercitado no navegador
- **Então** seleção por nome, busca de CPF existente, CPF inexistente com cadastro rápido, envio de doação e recarga do registro são verificados; build passa e o relatório diferencia o que foi comprovado de qualquer bloqueio real.

## Fora de escopo

- Publicação, push, dados pessoais reais, validação clínica, contas de acesso para doadores e reformulação visual geral.
- Não tornar obrigatório o vínculo de doador nos registros legados: o campo permanece opcional quando desconhecido, conforme contrato existente. Um CPF digitado precisa ser resolvido ou cadastrado antes do envio.
- Não fabricar CPF nem contatos para pessoas já existentes no banco.

## Suposições

Nenhuma pendente. As escolhas de produto estão registradas abaixo. Arquitetura e componentes seguem as convenções dos repositórios.

## Perguntas em aberto

Nenhuma pendente. O usuário confirmou nesta conversa:

- Cadastro rápido: "Sim, seguir esse cadastro (recomendado)" — nome e CPF obrigatórios, e-mail e telefone opcionais, ADMIN e HEMOCENTRO consultam/cadastram, sem conta de acesso.
- Centro de coleta: "Preencher automaticamente e permitir editar (recomendado)".

## Decisões de execução

As preferências já autorizadas na conversa permanecem: tarefas sequenciais, gpt-6.1-terra com esforço médio, commits locais e testes; sem push. O usuário solicitou continuidade do desenvolvimento do mesmo projeto. Não solicitar novamente as escolhas já respondidas.

## Compatibilidade e projeto

- CPF será uma adição nullable para pessoas legadas, único quando informado e obrigatório nos novos cadastros de doadores. Usar nova migração Flyway sem editar migrações aplicadas.
- Contatos hoje obrigatórios em Person se tornam opcionais conforme decisão explícita do usuário; dados existentes e vínculos de doações/bolsas são preservados.
- As respostas e escritas atuais são preservadas. A busca de CPF usa valor exato normalizado, sem busca parcial que retorne todos os doadores.
- Componente de seleção reutilizável para hemocentro, hospital, veículo e bolsa; IDs ficam no estado e no payload. Para listas não paginadas existentes, filtragem local é suficiente; hospitais utilizam a paginação/consulta por nome disponível.
- O frontend acompanha cargas e falhas dos dados persistidos e remove avisos antigos de que as consultas ainda não existem.
- Testes de interface devem exercitar comportamento; não substituir cliques/seleção por verificações de texto-fonte. Critérios visuais têm prova no navegador e testes automatizados relacionados.
