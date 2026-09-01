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

---

<div align="center">
  <h2>Diagramas UML das Funcionalidades</h2>
  <p><i>Clique no título da funcionalidade para acessar o diagrama correspondente completo</i></p>
</div>

### ☕ Programação Orientada a Objetos (POO)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**POOUS01. Cadastro de Doação**](diagramas/README.md#poous01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous01) | [**POOUS02. Controle de Validade**](diagramas/README.md#poous02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous02) | [**POOUS03. Cadastro de Hospitais**](diagramas/README.md#poous03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous03) |
| [**POOUS04. Emissão de Requisições**](diagramas/README.md#poous04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous04) | [**POOUS05. Tela Inicial / Visão Geral**](diagramas/README.md#poous05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous05) | [**POOUS06. Matching ABO/Rh + FEFO**](diagramas/README.md#poous06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous06) |
| [**POOUS07. Roteirização Integrada**](diagramas/README.md#poous07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous07) | [**POOUS08. Painel de Monitoramento**](diagramas/README.md#poous08) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous08) | [**POOUS09. Início do Transporte**](diagramas/README.md#poous09) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous09) |
| [**POOUS10. Cancelamento de Requisição**](diagramas/README.md#poous10) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous10) | [**POOUS11. Baixa Final de Entrega**](diagramas/README.md#poous11) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous11) | [**POOUS12. Bloqueio de Concorrência**](diagramas/README.md#poous12) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#poous12) |

---

### ⚡ Algoritmos e Estruturas de Dados (AED)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**AEDUS01. Fila FIFO de Requisições**](diagramas/README.md#aedus01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus01) | [**AEDUS02. Lista Encadeada Estoque**](diagramas/README.md#aedus02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus02) | [**AEDUS03. Pilha Undo Log**](diagramas/README.md#aedus03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus03) |
| [**AEDUS04. Grafo & Dijkstra**](diagramas/README.md#aedus04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus04) | [**AEDUS05. Min-Heap / FEFO**](diagramas/README.md#aedus05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus05) | [**AEDUS06. Indexação Tabela Hash**](diagramas/README.md#aedus06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus06) |
| [**AEDUS07. Matriz ABO/Rh**](diagramas/README.md#aedus07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus07) | [**AEDUS08. MergeSort Estável**](diagramas/README.md#aedus08) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus08) | [**AEDUS09. Busca Binária**](diagramas/README.md#aedus09) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus09) |
| [**AEDUS10. Buffer Circular**](diagramas/README.md#aedus10) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#aedus10) | | | | |

---

### 📊 Estatística e Probabilidade (EST)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**ESTUS01. Análise Descritiva**](diagramas/README.md#estus01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus01) | [**ESTUS02. Tendência e Dispersão**](diagramas/README.md#estus02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus02) | [**ESTUS03. Tempos de Entrega**](diagramas/README.md#estus03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus03) |
| [**ESTUS04. Risco Desabastecimento**](diagramas/README.md#estus04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus04) | [**ESTUS05. Taxa de Descarte**](diagramas/README.md#estus05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus05) | [**ESTUS06. Previsão de Demanda**](diagramas/README.md#estus06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus06) |
| [**ESTUS07. Dashboard REST**](diagramas/README.md#estus07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#estus07) | | | | |

---

### ⚙️ Infraestrutura de Software (SO)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**SOUS01. Pipeline CI/CD**](diagramas/README.md#sous01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous01) | [**SOUS02. Deploy Contínuo Cloud**](diagramas/README.md#sous02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous02) | [**SOUS03. Ingestão Multi-Thread**](diagramas/README.md#sous03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous03) |
| [**SOUS04. Trava Anti-Race Condition**](diagramas/README.md#sous04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous04) | [**SOUS05. Três Cenários de Carga**](diagramas/README.md#sous05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous05) | [**SOUS06. Orçamento em Nuvem**](diagramas/README.md#sous06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous06) |
| [**SOUS07. Rollback Automatizado**](diagramas/README.md#sous07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#sous07) | | | | |

---

### 🌐 Infraestrutura de Comunicação / Redes (RSD)

| Funcionalidade | Diagrama | Funcionalidade | Diagrama | Funcionalidade | Diagrama |
| :--- | :---: | :--- | :---: | :--- | :---: |
| [**RSDUS01. Topologia da Hemorrede**](diagramas/README.md#rsdus01) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus01) | [**RSDUS02. Mapeamento Técnico**](diagramas/README.md#rsdus02) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus02) | [**RSDUS03. Contratos de Telemetria**](diagramas/README.md#rsdus03) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus03) |
| [**RSDUS04. Benchmarking de Rede**](diagramas/README.md#rsdus04) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus04) | [**RSDUS05. Monitoramento Erros HTTP**](diagramas/README.md#rsdus05) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus05) | [**RSDUS06. Painel Qualidade de Rede**](diagramas/README.md#rsdus06) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus06) |
| [**RSDUS07. Plano Migração & TLS**](diagramas/README.md#rsdus07) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus07) | [**RSDUS08. Implantação em Produção**](diagramas/README.md#rsdus08) | [![Ver Diagrama](https://img.shields.io/badge/UML-Fluxo-2ea44f?style=flat-square)](diagramas/README.md#rsdus08) | | |

---

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
| Alice Sena | [amsp@cesar.school](mailto:amsp@cesar.school) |
| Abraão Santos | [afs6@cesar.school](mailto:afs6@cesar.school) |
| Cecília Lopes | [cvls@cesar.school](mailto:cvls@cesar.school) |
| Matheus | [mngv@cesar.school](mailto:mngv@cesar.school) | 
| Emily Raquel |  [emrs@cesar.school](mailto:emrs@cesar.school) |
| Vinicius Wagner | [vwgg@cesar.school](mailto:vwgg@cesar.school) |
