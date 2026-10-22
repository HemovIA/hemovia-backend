# Resultado da validação — Documentação Swagger

Data: 22/10/2026.

## Suíte Java

Comando: `scripts/spec-java/test-postgres.ps1`, com JDK 21.0.10 e PostgreSQL 17 temporário.

```text
Tests run: 79, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
server stopped
```

Os casos incluíram os testes HTTP de disponibilidade/desativação, contrato OpenAPI, fluxo JWT e a suíte Java existente. O usuário sintético de autenticação foi criado no banco de teste descartável.

## Provas por critério

```text
verify documentacao-swagger: 6/6 critério(s) com prova PASS · 30 teste(s) lidos · exit 0
verify finalizando-entidades-base: 11/11 critério(s) com prova PASS · 30 teste(s) lidos · exit 0
verify revisao-projeto: 4/4 critério(s) com prova PASS · 30 teste(s) lidos · exit 0
```

As provas foram gravadas em `.spec/verification/documentacao-swagger.json`, `.spec/verification/finalizando-entidades-base.json` e `.spec/verification/revisao-projeto.json`. O PostgreSQL temporário foi desligado pelo `finally` do script. Não houve alteração em banco pessoal.

## Limitações e observações

- O runtime padrão do terminal era Java 17, embora o projeto exija Java 21. A validação foi executada com JDK 21.0.10 instalado em `C:\Users\Vinicius\.jdk\jdk-21\jdk-21.0.10`.
- O caminho padrão do script não encontrou `initdb.exe` na primeira chamada; a execução bem-sucedida passou `-PostgresBin 'C:\Program Files\PostgreSQL\17\bin'` explicitamente.
- `/auth/me` pode responder 403 pelo filtro de segurança quando o JWT está ausente ou inválido; o contrato documenta 401 e 403. Token válido foi aceito e as demais rotas mantiveram o comportamento público atual.
- A auditoria pode preservar o aviso recomendado P-002 de constituição: o glob JavaScript `src/**/*.js` não casa arquivos. O aviso não é específico da feature e não foi removido ou enfraquecido.