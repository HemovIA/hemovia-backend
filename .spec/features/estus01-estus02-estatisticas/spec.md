# Spec: Estatísticas de estoque e demanda — ESTUS01 e ESTUS02

> feature: estus01-estus02-estatisticas
> status: auditada

## Contexto

Implementar as histórias https://github.com/HemovIA/hemovia-backend/issues/25 e https://github.com/HemovIA/hemovia-backend/issues/26 usando dados operacionais reais da simulação, sem confiar no estado dos cartões. Não há cálculos ou endpoints estatísticos na base develop 2918638. A branch é feat/estus01-estus02-estatisticas.

## Histórias

### US-026 — Distribuir o estoque por tipo sanguíneo e componente (ESTUS01)
Como gestor do hemocentro, quero visualizar quantidade e proporção das bolsas disponíveis por tipo sanguíneo e componente, para avaliar o suprimento da rede.

#### AC-067 — Distribuição exata de 200 bolsas
- **Dado** estoque elegível de 200 bolsas, incluindo 80 O+ e 20 AB-, distribuídas entre componentes
- **Quando** ADMIN ou HEMOCENTRO consulta GET /api/v1/statistics/inventory
- **Então** recebe 200 com totalBags 200, 40% O+, 10% AB- e contagens por ABO/Rh, componente e combinação; cada agrupamento reconcilia o total sem duplicar bolsas e inclui categorias zeradas.

#### AC-068 — Estoque vazio e percentuais consistentes
- **Dado** estoque elegível vazio ou uma distribuição com proporções fracionárias
- **Quando** solicita o resumo
- **Então** retorna JSON válido, sem divisão por zero, com todas as oito categorias ABO/Rh, cinco componentes e quarenta combinações; no vazio contagens e percentuais são zero, e no estoque não vazio os percentuais de cada agrupamento somam 100,00%, com duas casas e desempate determinístico de arredondamento.

#### AC-069 — Resumo considera apenas bolsas realmente disponíveis
- **Dado** bolsas reservadas, em trânsito, entregues, descartadas, vencidas, com vencimento hoje UTC, em centros excluídos ou com reserva ativa apesar do status AVAILABLE
- **Quando** consulta o estoque
- **Então** essas bolsas não entram nos totais; apenas AVAILABLE, com validade posterior à data UTC, centro ativo e sem item RESERVED/IN_TRANSIT são contadas, sem alterar estoque, reservas ou versões.

### US-027 — Tendência central e dispersão da demanda (ESTUS02)
Como analista de operações, quero média, mediana, variância e desvio padrão das quantidades requisitadas, no geral e por hospital, para identificar discrepâncias de consumo.

#### AC-070 — Métricas populacionais e amostrais corretas
- **Dado** requisições não canceladas de 10, 20 e 30 bolsas dos hospitais A, B e C
- **Quando** consulta GET /api/v1/statistics/demand
- **Então** recebe 200, requestCount 3, totalBags 60, média 20, mediana 20, variância populacional 66,67, desvio populacional 8,16, variância amostral 100 e desvio amostral 10; recebe também métricas de cada hospital com histórico.

#### AC-071 — Amostra unitária, vazia, repetida e mediana par
- **Dado** nenhuma requisição, uma requisição ou quantidades repetidas em amostra par
- **Quando** calcula as métricas
- **Então** vazio retorna contagem/total/métricas zero e lista de hospitais vazia; n=1 retorna média/mediana da quantidade e dispersões zero, e ambos n<2 sinalizam insufficientSample e warning; quantidades repetidas mantêm suas frequências e mediana par usa a média dos dois valores centrais, sem expandir artificialmente as frequências.

#### AC-072 — Requisições individuais, período e cancelamentos
- **Dado** várias requisições do mesmo hospital, outros hospitais, cancelamentos e datas de emissão antes, dentro e no limite do período
- **Quando** informa from inclusivo e to exclusivo, em ISO 8601 com fuso, ou omite limites
- **Então** cada requisição não cancelada é uma observação; calcula o conjunto geral e por hospital no período de createdAt, mantém pedidos pendentes/alocados/em trânsito/entregues, exclui CANCELLED e omite hospitais sem observações; limites ausentes não restringem aquele lado, e datas inválidas ou from >= to retornam 400.

#### AC-073 — Consultas protegidas e sem mutações
- **Dado** sessão ausente ou com papel HOSPITAL, TRANSPORTADOR ou sem papel
- **Quando** consulta qualquer endpoint estatístico
- **Então** recebe 401 sem sessão e 403 com papel incompatível; ADMIN e HEMOCENTRO recebem dados da rede sem exigir CSRF para GET, sem expor pessoas, credenciais ou entidades internas e sem criar, modificar ou bloquear estoque para escrita.

#### AC-074 — Contrato Swagger e rastreabilidade
- **Dado** a aplicação com as estatísticas implementadas
- **Quando** consulta /v3/api-docs e executa os testes de aceitação
- **Então** o contrato inclui as duas operações GET, seus DTOs, campos, precisão, unidades, filtros, segurança e erros reais, preservando as 39 operações anteriores; os critérios possuem provas executáveis e o guia explica o preparo e a interpretação dos resultados.

## Fora de escopo
- Frontend, gráficos, dashboard consolidado ESTUS07/POOUS08, previsões, risco de desabastecimento, descarte e tempos logísticos.
- Alterar estados de bolsas, requisições ou centros; incluir cancelamentos na demanda; consumir dados pessoais; importar dados externos.
- Filtros por componente, centro ou hospital nesta entrega. Os dois endpoints mostram a rede aos papéis aprovados, e a demanda inclui o detalhamento por hospital.
- Materializar indicadores, alterar migrações existentes ou inventar séries temporais ausentes.

## Suposições
| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-013 | Quantidade é o número de bolsas e createdAt é o instante de emissão. | confirmada | Contrato existente de BloodRequest e período de emissão confirmado pelo usuário. |

## Perguntas em aberto
| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-018 | Qual estoque entra no resumo? | respondida | Usuário escolheu somente estoque disponível para alocação. |
| Q-019 | Qual a unidade de observação da demanda? | respondida | Usuário escolheu cada requisição não cancelada, geral e por hospital, com filtro opcional de emissão. |
| Q-020 | Endpoints, permissões e dispersão? | respondida | Usuário confirmou os dois GETs propostos, ADMIN/HEMOCENTRO da rede, métricas populacionais e amostrais e aviso n<2. |
