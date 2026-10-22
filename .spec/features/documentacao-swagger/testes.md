# Testes da documentação Swagger

## Escopo anotado

| Critério | Prova executada |
|---|---|
| AC-016 | `OpenApiAvailabilityIT#ac016_exposesGeneratedDocumentationAndUiConfiguration` consulta `/v3/api-docs`, configuração, redirect da UI, inicializador e bundle JavaScript por HTTP. |
| AC-017 | `OpenApiDisabledIT#ac017_doesNotExposeDocumentationWhenDisabled` inicia contexto com a opção desabilitada e confirma ausência de docs/UI sem alterar acesso às rotas da API. |
| AC-018 | `OpenApiContractIT#ac018DocumentsCurrentOperationsAndResponses` valida as 14 operações geradas, seus métodos, IDs, tags, parâmetros, corpos JSON e respostas. |
| AC-019 | `OpenApiContractIT#ac019DocumentsDtoSchemasAndExamples` verifica schemas, campos obrigatórios/opcionais, datas, enums, unidades e a coleção `bloodBags`. |
| AC-020 | `OpenApiContractIT#ac020DocumentsBearerSecurityWithoutExposingInternalParameters` verifica o esquema Bearer apenas em `/auth/me`; `ac020PreservesHttpAuthenticationBehavior` registra e autentica usuário sintético por HTTP e testa token ausente, inválido e válido. |
| AC-021 | `OpenApiContractIT#ac021ReadmeDocumentsSwaggerWorkflow` confere as URLs, variáveis, autenticação e pré-requisitos documentados no README; o gate Maven completo executa os testes de regressão existentes. |

Todos os casos estão relacionados no manifesto `scripts/spec-java/acceptance-cases.json`. A suíte de aceite usa relatórios Surefire recentes por método, rejeita falhas e skips e executa PostgreSQL real. Nenhum teste é aprovado apenas por existir uma anotação.

## Execução

O teste HTTP de contrato usa o documento gerado pela aplicação real em `/v3/api-docs`. O contexto usa o perfil `base-entities-test`; os gates oficiais iniciam PostgreSQL em cluster temporário dentro de `api/target`, com porta aleatória, e o encerram ao final. Não usar o `.env` pessoal nem executar migrações em bancos de desenvolvimento.

Comandos executados em 22/10/2026, com JDK 21.0.10 e PostgreSQL 17:

```powershell
$env:JAVA_HOME = 'C:\Users\Vinicius\.jdk\jdk-21\jdk-21.0.10'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
& ./scripts/spec-java/test-postgres.ps1 -PostgresBin 'C:\Program Files\PostgreSQL\17\bin'
& ./scripts/spec-java/test-postgres.ps1 -PostgresBin 'C:\Program Files\PostgreSQL\17\bin' -Verify -Features @('documentacao-swagger', 'finalizando-entidades-base', 'revisao-projeto')
```

O manifesto usa TAP e o gate executa Maven offline (`-o`) com o perfil `base-entities-acceptance`. Em ambientes sem JDK 21, PostgreSQL 17 ou dependências Maven em cache, a execução é impedida e não deve ser substituída por skip ou aprovação por configuração estática.