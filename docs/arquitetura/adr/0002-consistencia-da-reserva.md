# ADR 0002 · Consistência da reserva

**Status:** proposto · 26/11/2026

## Contexto

Duas requisições podem disputar a mesma bolsa. Uma falha após a reserva pode deixar estoque indisponível sem transporte correspondente. O código atual testa reserva concorrente, estoque insuficiente e rollback.

## Decisão

Confirmar a alocação integral em uma transação local PostgreSQL, com bloqueio das bolsas elegíveis e restrição única para reserva ativa. Se Estoque e Expedição virarem serviços, Requisições coordena a saga, persiste eventos via outbox e libera a reserva depois de confirmar que a expedição foi invalidada antes do transporte.

## Consequências

- A raiz Alocação protege a quantidade e a elegibilidade dos itens. A transação e a restrição do banco protegem a exclusividade entre alocações concorrentes.
- A ordem de aquisição de bloqueios e a duração da transação precisam de testes de carga. A projeção distribuída exige deduplicação, tentativas com identificadores, timeout e tratamento de falhas persistentes.
- A alternativa de transação distribuída em duas fases foi descartada pela dependência operacional entre bancos e pela baixa autonomia durante falhas. Para a Parte 3, preserva-se a transação local.
