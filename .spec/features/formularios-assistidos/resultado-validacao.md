# Resultado de validação — Formulários assistidos

Estado: em validação; não há ainda prova formal completa nem auditoria final aprovada.

O QA visual real realizado pelo agente principal em ambiente isolado confirmou login sintético, seleção de hemocentro e preenchimento do centro de coleta, busca por CPF existente, cadastro rápido de CPF não encontrado preservando os dados, envio da doação e persistência após recarga. A captura `formulario-integrado.png` contém somente dados sintéticos.

Testes específicos de backend em PostgreSQL isolado verificaram migração de pessoa legada, validação de CPF, permissões, CSRF e unicidade concorrente. Testes de navegador verificaram cadastro rápido, teclado, mudança de perfil, paginação, falha e nova tentativa, isolamento de sessões e recarga real. O build frontend passou.

A primeira execução integral do harness identificou expectativa OpenAPI histórica ainda fixada em 47 operações e um teste de estado de carregamento que aguardava a resposta bloqueada; as correções foram aplicadas. A segunda execução está em andamento. Provas antigas serão renovadas apenas depois da estabilização da suíte.
