# ADR 0001 · Decomposição do Rota Vital

**Status:** proposto · 26/11/2026

## Contexto

O backend Spring Boot tem um fluxo transacional entre estoque e requisições, um único PostgreSQL e publicação no GitHub Actions para Lightsail em Ohio. A carga ainda é uma projeção, e o squad opera a aplicação coletivamente. A Parte 3 precisa remodelar o código sem perder os fluxos e testes atuais.

## Decisão

Adotar um monólito modular com quatro contextos: Estoque, Requisições, Expedição e Identidade e Acesso. Os contextos são organizados dentro dos cinco pacotes controller, dto, model, repository e service. Os subpacotes e contratos explicitam suas fronteiras. A Parte 3 usa uma imagem e um PostgreSQL. Dados de um módulo só são alterados pelo módulo responsável.

## Consequências

- Uma esteira e um deploy preservam a transação local e simplificam diagnóstico e rollback.
- A aplicação escala como unidade. É necessário vigiar o acoplamento com ArchUnit e testes de contrato.
- A alternativa de extrair microsserviços já na Parte 3 foi descartada pelo custo de vários deploys, bancos, filas e consistência eventual.
- Revisar a decisão se testes mostrarem gargalo isolado e recorrente em Expedição, ou se o squad precisar de ciclos independentes de implantação.
