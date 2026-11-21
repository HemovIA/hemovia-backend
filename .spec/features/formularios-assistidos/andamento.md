# Andamento — Formulários assistidos

- T-046: cadastro/consulta com CPF, migração e segurança em 361dbb4; testes específicos de unidade, arquitetura e PostgreSQL passaram.
- T-047: seletor pesquisável e contratos em 782ef0f, com ajustes posteriores em 0cbffe4.
- T-048: formulários e cadastro rápido em 0cbffe4; interação e build aprovados.
- T-049: seletores operacionais em 33c03c2; teste de troca de perfil aprovado.
- T-050: consultas e detalhes persistidos em 159bd5b; testes de paginação, recarga, troca de identidade e resposta atrasada aprovados.
- T-051: concluída nos commits frontend 58e851d e backend 984bb34, incluindo ajustes posteriores aos testes. QA visual real aprovado pelo coordenador; doação, cadastro rápido, vínculos e recarga conferidos. Captura sintética em formulario-integrado.png. Harness reproduzível com bancos separados aprovado: 478 testes Java, 14 de navegador e 117 verificações TAP, sem falhas ou testes pulados.
- T-052: concluída. Nova funcionalidade com 15/15 critérios PASS e exitCode 0; 12 provas anteriores renovadas. Os 13 arquivos de verificação têm código de saída zero e todos os critérios aprovados. Auditoria final: 108/108 comprovados, nenhum aviso e exit 0.
- Todos os processos do ambiente isolado foram encerrados pelo harness. O servidor auxiliar 5185 do coordenador também foi encerrado. O backend do usuário em 8080 foi preservado.
