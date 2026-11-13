# Tarefas de FEFO e compatibilidade ABO/Rh

> feature: aedus05-aedus07

## T-018 — Implementar FEFO e provar ordenação e exclusão por validade [concluida]
- Refs: US-015, AC-026, AC-027
- Arquivos: api/src/main/java/com/hemovia/api/model/algorithm/priority/FefoQueue.java, api/src/test/java/com/hemovia/api/model/algorithm/priority/FefoQueueTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Usar fila de prioridade para ordenar `BloodBag` por `expirationDate`. A data de referência explícita torna a borda de vencimento determinística. Manter a coleção e as entidades de entrada intactas.

## T-019 — Provar os cenários ABO/Rh com a matriz de hemácias existente [concluida]
- Refs: US-016, AC-028, AC-029
- Arquivos: api/src/main/java/com/hemovia/api/model/algorithm/compatibility/BloodCompatibility.java, api/src/test/java/com/hemovia/api/model/algorithm/compatibility/BloodCompatibilityTest.java
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Reusar `canTransfuse` e cobrir os quatro doadores do cenário A+ e os três do cenário O- sem reduzir a cobertura existente. A matriz de produção só precisa mudar se a prova detectar divergência.

## T-020 — Ligar os critérios à prova automatizada [concluida]
- Refs: AC-026, AC-027, AC-028, AC-029
- Arquivos: scripts/spec-java/acceptance-cases.json, onpspec.config.json
- Modelo: gpt-6.1-terra
- Esforço: medio
- Notas: Após T-018 e T-019, registrar os métodos JUnit no manifesto e as duas classes de produção nos globs de fontes. Executar a verificação com PostgreSQL isolado e renovar as provas das especificações anteriores antes da auditoria geral.
