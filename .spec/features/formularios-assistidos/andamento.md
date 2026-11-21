# Andamento — Formulários assistidos

- T-046: cadastro/consulta com CPF, migração e segurança em 361dbb4; testes específicos de unidade, arquitetura e PostgreSQL passaram.
- T-047: seletor pesquisável e contratos em 782ef0f, com ajustes posteriores em 0cbffe4.
- T-048: formulários e cadastro rápido em 0cbffe4; interação e build aprovados.
- T-049: seletores operacionais em 33c03c2; teste de troca de perfil aprovado.
- T-050: consultas e detalhes persistidos em 159bd5b; testes de paginação, recarga, troca de identidade e resposta atrasada aprovados.
- T-051 em andamento: QA visual real aprovado pelo agente principal em ambiente isolado; doação, cadastro rápido e recarga conferidos. Captura sintética em formulario-integrado.png. Teste de ponta a ponta real e testes frontend específicos passaram. Harness reproduzível com bancos separados está em execução; regressões de expectativas antigas foram atualizadas para V17 e rota de doadores, preservando cenários anteriores.
- T-052 pendente: verificação de todas as features, exitCode de cada prova e audit --ci após o harness passar.
