# Validação — consultas operacionais

- PostgreSQL isolado real e suíte Maven completa: 474 testes, zero falhas, zero pulos.
- `onp-spec verify consultas-operacionais`: exit 0, 19/19 critérios com prova PASS.
- Provas das 11 funcionalidades anteriores renovadas por execuções reais da suíte. Os 12 arquivos de verificação registram exit 0 e critérios PASS.
- Uma renovação anterior do Swagger falhou por conexão transitória em um teste de migração, apesar de seus seis critérios terem passado. A repetição em PostgreSQL temporário terminou com BUILD SUCCESS e `verify documentacao-swagger`: 6/6 PASS, exit 0. Nenhuma prova foi editada manualmente e nenhum teste foi enfraquecido ou pulado.
- `onp-spec audit --ci`: exit 0, 12 funcionalidades, 31 histórias, 93/93 critérios com teste e prova, nenhum aviso. Relatório em `auditoria.md`.

## Entrega

Foram adicionados GET de lista e detalhe para `/api/v1/requests`, `/api/v1/transports` e `/api/v1/hospitals`. Filtros e escopo de acesso são aplicados antes da paginação e contagem. As respostas paginadas usam `content`, `page`, `size`, `totalElements` e `totalPages`, com padrão de 20 e máximo de 100 registros por página.

Os testes incluem os quatro perfis, recursos alheios, sessão ausente, parâmetros inválidos, filtros combinados, ordenação com desempates, página seguinte, deslocamento extremo, rotas e bolsas persistidas, ausência de chamadas ao roteador e snapshots integrais de oito tabelas antes e depois das consultas. O OpenAPI documenta os contratos.

O desenvolvimento ocorreu sequencialmente com `gpt-6.1-terra`, esforço médio. A execução manual preservou o checkout e os arquivos anteriores em `docs/`, `frontend/`, `.specs/` e o histórico `sinais.json`. Não houve publicação, push ou alteração das telas. O frontend ainda precisa consumir as novas consultas para recuperar o histórico entre sessões.
