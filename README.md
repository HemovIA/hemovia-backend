# 🩸 HEMOVIA — Rota Vital

## Descrição do projeto

O **HEMOVIA (Rota Vital)** é um sistema para simulação da gestão e distribuição de hemocomponentes em uma rede de hemocentros e hospitais. O projeto centraliza o cadastro de hospitais e bolsas de sangue, controle de estoque, requisições hospitalares, verificação de compatibilidade ABO/Rh, priorização por validade (FEFO), roteirização logística, telemetria de transporte e indicadores estatísticos da rede. Todos os dados utilizados são **sintéticos** — nenhuma informação real de pacientes ou doadores é armazenada.

Projeto Integrador desenvolvido na **CESAR School**, curso de Análise e Desenvolvimento de Sistemas, 3º semestre (2026.2), integrando as disciplinas de POO, AED, Estatística, Infraestrutura de Software (SO) e Infraestrutura de Comunicação (RSD).

## Tecnologias usadas

- **Backend:** Java + Spring Boot
- **Arquitetura:** Clean Architecture (Domain, Application, Infrastructure, Presentation) + princípios SOLID
- **Banco de dados:** PostgreSQL (Spring Data JPA)
- **Documentação de API:** OpenAPI/Swagger
- **CI/CD:** GitHub Actions
- **Containerização:** Docker / docker-compose
- **Protótipo:** Figma (Lo-Fi)

---

## Entregas

### Entrega 01 — 31/08

#### Histórias de usuário (BDD)

> Histórias completas, com cenários de validação (positivo e negativo), escritas em formato BDD (Dado/Quando/Então).

<details>
<summary><strong>POOUS01 — Cadastro de Doação e Entrada de Bolsa no Estoque</strong></summary>

**Cartão:** Como operador do hemocentro, quero registrar uma doação de sangue e a entrada da bolsa correspondente no estoque para que o sistema mantenha rastreabilidade entre a doação recebida e o hemocomponente disponível para uso.

- **Cenário 1 (Positivo):** Dado uma doação sintética registrada hoje no centro de coleta "Centro Recife", tipo O+, quando o operador confirma o registro e a geração da bolsa de Hemácias, então o sistema cria a Doação, vincula a bolsa e a insere no estoque com status `AVAILABLE`.
- **Cenário 2 (Negativo):** Dado um registro de doação sem centro de coleta informado, quando o operador tenta submeter o cadastro, então o sistema rejeita a requisição com `400 Bad Request`.
</details>

<details>
<summary><strong>POOUS02 — Cadastro e Controle de Validade das Bolsas de Sangue</strong></summary>

**Cartão:** Como gestor do hemocentro, quero cadastrar bolsas e consultar o estoque disponível para garantir controle rigoroso de validade.

- **Cenário 1 (Positivo):** Dado uma bolsa O+ com validade em 35 dias, quando o gestor cadastra, então a bolsa é salva com status `AVAILABLE`.
- **Cenário 2 (Negativo):** Dado uma bolsa com data de validade vencida, quando o gestor tenta registrá-la, então o domínio dispara `BloodBagExpiredException`.
</details>

<details>
<summary><strong>POOUS03 — Gestão de Cadastro de Hospitais</strong></summary>

**Cartão:** Como operador de distribuição, quero cadastrar e gerenciar hospitais na rede para saber origem das requisições e localizações.

- **Cenário 1 (Positivo):** Dado nome, cidade, coordenadas e prioridade válidos, quando o cadastro é solicitado via API, então o sistema retorna `201 Created` com UUID.
- **Cenário 2 (Negativo):** Dado uma latitude inválida (105.00), quando o cadastro é solicitado, então o sistema retorna `400 Bad Request`.
</details>

<details>
<summary><strong>POOUS04 — Emissão de Requisições Hospitalares</strong></summary>

**Cartão:** Como profissional do hospital, quero criar solicitações de hemocomponentes informando tipo, quantidade e prioridade.

- **Cenário 1 (Positivo):** Dado um hospital válido solicitando 4 bolsas de Plaquetas A+ em 6h, quando a requisição é submetida, então é gravada com status `PENDING`.
- **Cenário 2 (Negativo):** Dado uma quantidade nula ou negativa, quando o serviço é consumido, então retorna `400 Bad Request`.
</details>

<details>
<summary><strong>POOUS05 — Tela Inicial da Aplicação</strong></summary>

**Cartão:** Como usuário do sistema, quero uma tela inicial com visão geral rápida do estado da rede.

- **Cenário 1 (Positivo):** Dado bolsas e requisições cadastradas, quando o usuário acessa a rota inicial, então exibe total de bolsas, requisições pendentes e status do sistema.
- **Cenário 2 (Negativo):** Dado falha temporária no banco, quando o usuário acessa a tela, então exibe mensagem amigável de indisponibilidade, sem erro não tratado.
</details>

<details>
<summary><strong>AEDUS01 — Fila de Processamento de Requisições Hospitalares</strong></summary>

**Cartão:** Como sistema de distribuição, quero gerenciar as requisições em uma Fila (FIFO) para processá-las na ordem de chegada.

- **Cenário 1 (Positivo):** Dado a fila vazia recebendo Req#101 e depois Req#102, quando solicita-se o próximo item, então retorna Req#101, mantendo Req#102.
- **Cenário 2 (Negativo):** Dado a fila vazia, quando tenta-se um `dequeue`, então lança `EmptyQueueException`.
</details>

<details>
<summary><strong>AEDUS02 — Lista para Armazenamento Geral de Bolsas no Estoque</strong></summary>

**Cartão:** Como módulo de estoque, quero armazenar bolsas em uma Lista Encadeada para inserções, remoções e buscas dinâmicas.

- **Cenário 1 (Positivo):** Dado uma bolsa BAG-999 com validade em dia, quando inserida, então é adicionada e localizável com complexidade O(n).
- **Cenário 2 (Negativo):** Dado uma lista populada, quando tenta-se remover BAG-000 (inexistente), então retorna falso/exceção sem corromper a lista.
</details>

<details>
<summary><strong>AEDUS03 — Pilha de Histórico e Reversão de Operações de Estoque</strong></summary>

**Cartão:** Como operador do sistema, quero registrar movimentações em uma Pilha (LIFO) para permitir estorno da última operação.

- **Cenário 1 (Positivo):** Dado uma bolsa que mudou de `AVAILABLE` → `RESERVED` → `IN_TRANSIT`, quando aciona-se Undo, então a Pilha reverte para `RESERVED`.
- **Cenário 2 (Negativo):** Dado a Pilha vazia, quando aciona-se reversão, então lança `EmptyStackException`.
</details>

Documento completo com todas as histórias do backlog (POO, AED, EST, SO e RSD — mínimo 7 aqui apresentadas): **[link para o documento de histórias no GitHub]**

#### Protótipo (Lo-Fi — Figma)

- Protótipo Lo-Fi cobrindo no mínimo 5 histórias: **[colar link do Figma aqui]**
- Screencast explicando cada história implementada no protótipo: **[YouTube](https://youtu.be/6ujUBhdNyCA?is=7e5ZPCPDCTEUPSc6)**

---

## Como rodar o projeto

*(seção detalhada a partir da segunda entrega)*

---

## Equipe

### Membros atuais

| Nome | E-mail |
|---|---|
| Alice Sena | [<email>@cesar.school](mailto:amsp@cesar.school) |
| Abraão Santos | [afs6@cesar.school](mailto:afs6@cesar.school) |
| Cecília Lopes | [<email>@cesar.school](mailto:cvls@cesar.school) |
| Matheus | [<email>@cesar.school](mailto:mngv@cesar.school) | 
| Emily Raquel |  [<email>@cesar.school](mailto:emrs@cesar.school) |
| Vinicius Wagner | [<email>@cesar.school](mailto:vwgg@cesar.school) |
