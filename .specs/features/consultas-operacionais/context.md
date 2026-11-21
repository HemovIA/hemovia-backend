# Consultas operacionais Context

**Gathered:** 2026-11-20
**Status:** Ready for specification

## Feature Boundary

Completar as consultas de requisições, transportes e hospitais no backend. O usuário fará o teste conjunto depois.

## Implementation Decisions

Visibilidade proposta e aceita pelo usuário: "Sim, seguir essas regras".

- ADMIN consulta todos os registros.
- HOSPITAL consulta apenas seus pedidos, entregas e seu próprio hospital.
- HEMOCENTRO consulta todos os pedidos pendentes e aqueles que alocou; consulta os transportes de suas alocações e todos os hospitais.
- TRANSPORTADOR consulta os transportes do veículo vinculado e os pedidos dessas viagens; não consulta o cadastro de hospitais.
- Identificadores fora do escopo do usuário recebem 404, seguindo a ocultação já usada em cancelamento e confirmação de entrega.

## Specific References

- Pedido do usuário: "Vamos completar as consultas da API, depois testo tudo junto".
- Regras aceitas em resposta à pergunta de visibilidade nesta conversa.
- Controllers, entidades e AuthenticatedUser do backend local.

## Deferred Ideas

Integração das novas consultas nas telas, telemetria em tempo real e publicação.
