# AEDUS05 e AEDUS07 — Validade e compatibilidade

> feature: aedus05-aedus07
> status: auditada

## Histórias

### US-015 — Priorizar bolsas compatíveis pelo vencimento

Como gerenciador de estoque, quero ordenar as bolsas compatíveis pela data de vencimento para usar primeiro as que vencem mais cedo.

Fonte: AEDUS05 ([issue #19](https://github.com/HemovIA/hemovia-backend/issues/19)).

#### AC-026 — A bolsa com menor validade tem prioridade

- **Dado** três bolsas compatíveis com validade de 3, 15 e 1 dia a partir da data de referência, nessa ordem de entrada
- **Quando** o algoritmo FEFO monta e consome a fila de prioridade
- **Então** retorna as bolsas na ordem de 1, 3 e 15 dias

#### AC-027 — Bolsas que vencem hoje ou já venceram ficam fora da fila

- **Dado** bolsas compatíveis com vencimento anterior, igual e posterior à data de referência
- **Quando** o algoritmo FEFO prepara a fila
- **Então** somente a bolsa com vencimento posterior à data de referência pode ser selecionada
- **E** uma coleção sem bolsas elegíveis produz uma sequência vazia

### US-016 — Consultar compatibilidade ABO/Rh para hemácias

Como motor de matching, quero consultar uma matriz ABO/Rh em tempo constante para decidir se uma bolsa de hemácias pode seguir para o FEFO.

Fonte: AEDUS07 ([issue #21](https://github.com/HemovIA/hemovia-backend/issues/21)).

#### AC-028 — O receptor A positivo aceita os quatro doadores do cenário

- **Dado** um receptor A+ e doadores A+, A-, O+ e O-
- **Quando** o algoritmo consulta a matriz ABO/Rh de hemácias
- **Então** as quatro consultas retornam verdadeiro

#### AC-029 — O receptor O negativo rejeita os doadores incompatíveis do cenário

- **Dado** um receptor O- e doadores A+, B- e AB-
- **Quando** o algoritmo consulta a matriz ABO/Rh de hemácias
- **Então** as três consultas retornam falso

## Desenho

- FEFO recebe as bolsas previamente filtradas por compatibilidade e uma `LocalDate` de referência. Uma fila de prioridade ordena por validade. A seleção preserva a coleção e o estado das bolsas.
- A classe `BloodCompatibility` já mantém uma matriz 8 × 8 de hemácias com acesso direto e oferece `canTransfuse`. Os cenários da issue serão executados por esse método.
- A orquestração da seleção e reserva, as transições de estoque e as rotas HTTP pertencem ao matching da POOUS06.

## Suposições

Nenhuma.

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-008 | Quais tarefas devem rodar em paralelo e quais modelos e esforços serão usados? | respondida | T-018 e T-019 em paralelo, depois T-020. GPT-6.1 Terra com esforço médio nas três. |
