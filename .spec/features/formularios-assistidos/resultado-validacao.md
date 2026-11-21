# Resultado de validação — Formulários assistidos

Estado: auditada em 21/11/2026. A funcionalidade tem 15/15 critérios PASS e código de saída zero; as 12 funcionalidades anteriores foram novamente verificadas com sucesso.

O QA visual real realizado pelo agente principal em ambiente isolado confirmou login sintético, seleção de hemocentro e preenchimento do centro de coleta, busca por CPF existente, cadastro rápido de CPF não encontrado preservando os dados, envio da doação e persistência após recarga. A captura `formulario-integrado.png` contém somente dados sintéticos.

Testes específicos de backend em PostgreSQL isolado verificaram migração de pessoa legada, validação de CPF, permissões, CSRF e unicidade concorrente. Testes de navegador verificaram cadastro rápido, teclado, mudança de perfil, paginação, falha e nova tentativa, isolamento de sessões e recarga real. O build frontend passou.

A execução integral do ambiente reproduzível passou com 478 testes Java, 14 testes Playwright e 117 verificações TAP, sem falhas, erros ou testes pulados. As verificações formais foram executadas com fontes e testes congelados; os 13 arquivos em `.spec/verification` registram `exitCode: 0` e todos os seus 108 critérios estão em PASS.

As expectativas históricas de versão Flyway e de rotas OpenAPI foram atualizadas para contemplar V17 e as duas operações de doador, preservando os cenários anteriores. Os testes que bloqueiam respostas utilizam navegação apropriada para observar o estado pendente antes de liberar a resposta. Nenhuma falha foi aceita como prova e nenhum resultado de verificação foi preenchido manualmente.

O harness verifica também a existência e a atualização do arquivo de prova, o nome da funcionalidade, o código de saída global e os estados de cada critério e princípio. Isso complementa o CLI da skill, que isoladamente pode retornar sucesso pelos critérios selecionados mesmo quando outra parte da suíte falha.

## Gate final

```text
resumo: 13 feature(s) · 35 história(s) de usuário · 108 critério(s) de aceite · 108/108 com teste · 108/108 provados
✔ auditoria limpa (0 aviso(s))
```

`onp-spec audit --ci`: exit 0. As sete tarefas foram implementadas sequencialmente com gpt-6.1-terra, esforço médio. A consolidação dos resultados e da documentação foi concluída pelo coordenador, sem novas alterações no código testado.

## Repetir a validação

A partir do repositório backend, com as dependências do frontend instaladas:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/spec-java/test-formularios.ps1 -Verify
```

Acrescente `-All` para verificar a funcionalidade atual e renovar as 12 anteriores. O script exige as portas de teste 5179, 5180 e 18080 livres e as ferramentas locais documentadas. Ele prepara PostgreSQL temporário com bancos distintos para Java e interface, API e frontends próprios, dados sintéticos e logs em `api/target`; encerra somente os processos que criou. O ambiente do usuário em 8080 não foi utilizado nos testes nem encerrado.

## Entrega e limites

O proxy habitual do frontend mantém o destino padrão `http://localhost:8080`. A migração V17 é aplicada pelo Flyway na inicialização do backend atualizado. Para testar no ambiente habitual, reinicie backend e frontend. Busca por CPF requer um doador com CPF cadastrado; pessoas antigas sem CPF permanecem preservadas. Não houve publicação nem push. Alterações anteriores, incluindo `sinais.json`, `docs/`, o diretório `frontend/` do backend e `.specs/`, foram preservadas fora dos commits desta consolidação.
