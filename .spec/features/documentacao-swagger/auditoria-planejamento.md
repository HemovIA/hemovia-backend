# Relatório de auditoria

- Resultado: ❌ FAIL
- Features: 3 · Histórias de usuário: 13 · Critérios de aceite: 21
- Critérios com teste: 15/21 · Critérios provados: 15/21
- Suposições abertas: 1 · Perguntas abertas: 1

| Severidade | Problema | Feature | Detalhe | Local |
|---|---|---|---|---|
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-013 mapeia api/src/main/java/com/hemovia/api/config/OpenApiConfig.java, que não existe | .spec\features\documentacao-swagger\tasks.md:7 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-013 mapeia api/src/test/java/com/hemovia/api/documentation/OpenApiAvailabilityIT.java, que não existe | .spec\features\documentacao-swagger\tasks.md:7 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-013 mapeia api/src/test/java/com/hemovia/api/documentation/OpenApiDisabledIT.java, que não existe | .spec\features\documentacao-swagger\tasks.md:7 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-014 mapeia api/src/main/java/com/hemovia/api/config/OpenApiConfig.java, que não existe | .spec\features\documentacao-swagger\tasks.md:15 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-014 mapeia api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, que não existe | .spec\features\documentacao-swagger\tasks.md:15 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-015 mapeia api/src/test/java/com/hemovia/api/documentation/OpenApiAvailabilityIT.java, que não existe | .spec\features\documentacao-swagger\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-015 mapeia api/src/test/java/com/hemovia/api/documentation/OpenApiDisabledIT.java, que não existe | .spec\features\documentacao-swagger\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-015 mapeia api/src/test/java/com/hemovia/api/documentation/OpenApiContractIT.java, que não existe | .spec\features\documentacao-swagger\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-015 mapeia .spec/features/documentacao-swagger/testes.md, que não existe | .spec\features\documentacao-swagger\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-015 mapeia .spec/features/documentacao-swagger/resultado-validacao.md, que não existe | .spec\features\documentacao-swagger\tasks.md:23 |
| aviso | arquivo não existe (`ARQUIVO_INEXISTENTE`) | documentacao-swagger | a tarefa T-015 mapeia .spec/features/documentacao-swagger/auditoria.md, que não existe | .spec\features\documentacao-swagger\tasks.md:23 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | documentacao-swagger | AC-016 (Swagger UI e OpenAPI disponíveis quando habilitados) não tem nenhum teste anotado com @spec:AC-016 | .spec\features\documentacao-swagger\spec.md:18 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | documentacao-swagger | AC-017 (Documentação pode ser desativada) não tem nenhum teste anotado com @spec:AC-017 | .spec\features\documentacao-swagger\spec.md:25 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | documentacao-swagger | AC-018 (Todas as operações atuais têm descrição e respostas coerentes) não tem nenhum teste anotado com @spec:AC-018 | .spec\features\documentacao-swagger\spec.md:36 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | documentacao-swagger | AC-019 (Modelos e exemplos representam os contratos atuais) não tem nenhum teste anotado com @spec:AC-019 | .spec\features\documentacao-swagger\spec.md:43 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | documentacao-swagger | AC-020 (JWT e credenciais documentados conforme a segurança real) não tem nenhum teste anotado com @spec:AC-020 | .spec\features\documentacao-swagger\spec.md:55 |
| erro | critério de aceite sem teste (`AC_SEM_TESTE`) | documentacao-swagger | AC-021 (Testes reais, instruções de uso e auditoria reproduzível) não tem nenhum teste anotado com @spec:AC-021 | .spec\features\documentacao-swagger\spec.md:67 |
| aviso | verificação não olha nenhum arquivo (`GLOB_SEM_ARQUIVOS`) | — | P-002: o glob `src/**/*.js` não casa nenhum arquivo — verificação inerte (typo no glob?) | .spec\constituicao.md:27 |
