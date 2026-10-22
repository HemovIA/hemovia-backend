# Revisão técnica — API, dados e segurança

> Critério coberto: AC-002 — Revisão da API, dados e segurança  
> Método: inspeção estática do código, contratos, configuração e migrações versionados.  
> Escopo temporal: árvore de trabalho analisada em 22/10/2026. Valores de segredos foram deliberadamente omitidos.

## Resultado por área

| Área | Resultado | Evidência ou justificativa de não aplicabilidade |
| --- | --- | --- |
| Arquitetura e camadas | Com ressalvas | Controladores delegam a serviços e repositórios, mas as operações compostas não definem fronteira transacional (API-008). |
| Contratos REST e validação | Com achados | Há DTOs e `@Valid` nos controladores, porém falhas de domínio e IDs inválidos não têm conversão HTTP explícita (API-006); o contrato de bolsa exige um doador que o serviço ignora (API-005). |
| Regras de doação, bolsas e estoque | Com achados | Criação de doação pode persistir bolsa incompleta (API-004) e a atualização de bolsa aceita o estado recebido diretamente; não foi localizado fluxo de reserva/solicitação de hospital nesta API. |
| Dados, JPA e Flyway | Falha confirmada | Existem duas migrações com a versão `V2`, condição que o Flyway rejeita antes de aplicar migrações (API-001). |
| Autenticação e autorização | Falha confirmada | JWT e BCrypt estão presentes, mas a cadeia libera todos os endpoints fora de `/auth/me`, incluindo operações de doação e estoque (API-002). |
| Segredos e configuração | Com achados | Credenciais de banco são variáveis de ambiente, mas o segredo JWT tem valor-padrão no código (API-003); a configuração também registra SQL (API-007). |
| Observabilidade de segurança | Não verificado funcionalmente | Actuator é liberado por configuração; não foram encontrados testes de integração nem uma execução local que confirme quais endpoints são expostos. O risco é documentado em API-009. |

## Achados

### API-001 — Versão Flyway duplicada impede a inicialização

- **Tipo:** defeito confirmado
- **Severidade:** alta
- **Arquivo e linha:** `api/src/main/resources/db/migration/V2__create_users_table.sql:1`; `api/src/main/resources/db/migration/V2__create_hospital_table.sql:1`
- **Esperado:** cada migração Flyway deve ter versão única e ordem determinística.
- **Observado:** dois arquivos de migração usam a versão `V2`.
- **Impacto:** o Flyway detecta versões duplicadas durante a validação, impedindo a aplicação de iniciar com migrações habilitadas.
- **Reprodução ou justificativa estática:** listar os dois nomes em `api/src/main/resources/db/migration/` revela o mesmo prefixo de versão; `application.yaml:15-17` habilita Flyway e aponta para esse diretório.
- **Recomendação:** renumerar uma das migrações para uma versão inédita, preservando a ordem necessária, e executar o bootstrap contra uma base PostgreSQL vazia.

### API-002 — Operações de domínio estão públicas

- **Tipo:** defeito confirmado
- **Severidade:** alta
- **Arquivo e linha:** `api/src/main/java/com/hemovia/api/config/SecurityConfig.java:29-34`
- **Esperado:** operações que criam, alteram, removem ou consultam doações, bolsas e hospitais devem exigir autenticação e autorização compatível com o papel do usuário.
- **Observado:** após liberar apenas rotas de autenticação e actuator, a regra final é `anyRequest().permitAll()`; os controladores de doação e bolsa expõem CRUD em `/donations` e `/blood-bags`.
- **Impacto:** qualquer cliente sem token pode ler e alterar dados operacionais, inclusive excluir recursos.
- **Reprodução ou justificativa estática:** seguir a ordem das regras da cadeia em `SecurityConfig.java:29-34` e os mapeamentos em `DonationController.java:23-48` e `BloodBagController.java:23-48`.
- **Recomendação:** substituir a regra abrangente por autenticação padrão e regras de autorização por rota/papel; cobrir anonimamente cada rota mutável com testes de integração que esperem 401/403.

### API-003 — Segredo JWT possui valor-padrão embutido

- **Tipo:** risco de segurança
- **Severidade:** alta
- **Arquivo e linha:** `api/src/main/java/com/hemovia/api/service/JwtService.java:19-23`
- **Esperado:** o segredo de assinatura deve ser obrigatório, fornecido por ambiente ou cofre de segredos, e não ter fallback reutilizável no artefato.
- **Observado:** a injeção de `jwt.secret` define um valor-padrão literal no código. O valor em si não é reproduzido neste relatório.
- **Impacto:** uma implantação que não configure a variável pode assinar tokens com um segredo conhecido a quem lê o repositório.
- **Reprodução ou justificativa estática:** a anotação `@Value` em `JwtService.java:20` contém fallback, enquanto `application.yaml` não torna a propriedade obrigatória.
- **Recomendação:** remover o fallback, falhar na inicialização quando a propriedade estiver ausente e injetar o valor por mecanismo de segredo do ambiente; rotacionar o valor usado por ambientes afetados.

### API-004 — Criação de doação aceita dados insuficientes para uma nova bolsa

- **Tipo:** defeito confirmado
- **Severidade:** alta
- **Arquivo e linha:** `api/src/main/java/com/hemovia/api/model/DTOs/DonationRequestDTO.java:14-26`; `api/src/main/java/com/hemovia/api/service/implementation/DonationServiceImpl.java:88-105`
- **Esperado:** ao criar uma bolsa como parte de uma doação, tipo sanguíneo, fator RH, componente e validade devem ser obrigatórios e validados antes da persistência.
- **Observado:** esses campos são opcionais no DTO. Sem `bloodBagId`, o serviço cria uma bolsa e copia os valores possivelmente nulos.
- **Impacto:** o banco pode rejeitar a operação por colunas `NOT NULL`, retornando erro técnico; a regra de negócio não é comunicada pelo contrato.
- **Reprodução ou justificativa estática:** enviar uma doação com centro e data, sem `bloodBagId` nem dados de bolsa, satisfaz as anotações do DTO e percorre `resolveBloodBag`; a migração `V1__create_initial_schema.sql:8-15` declara as colunas da bolsa como não nulas.
- **Recomendação:** tornar os campos condicionais explicitamente obrigatórios quando não houver `bloodBagId`, usando validador de classe ou DTOs separados, e retornar 400 para entrada inválida.

### API-005 — Contrato exige `donorId`, mas serviço não o aplica

- **Tipo:** defeito confirmado
- **Severidade:** média
- **Arquivo e linha:** `api/src/main/java/com/hemovia/api/model/DTOs/BloodBagRequestDTO.java:29-30`; `api/src/main/java/com/hemovia/api/service/implementation/BloodBagServiceImpl.java:44-55`
- **Esperado:** o identificador de doador exigido pelo contrato deve ser resolvido e associado à bolsa, ou deve deixar de ser obrigatório se a regra não existir.
- **Observado:** `donorId` é `@NotNull`, mas `create` não o consulta nem atribui a `BloodBag.donor`.
- **Impacto:** consumidores enviam dado obrigatório que é descartado; rastreabilidade do doador fica ausente apesar da API aceitar a operação.
- **Reprodução ou justificativa estática:** comparar o getter/validação do DTO com as atribuições em `BloodBagServiceImpl.java:47-54`; nenhuma usa `getDonorId()`.
- **Recomendação:** criar ou reutilizar repositório de pessoa para validar/associar o doador, ou remover o campo e ajustar a regra documentada.

### API-006 — Erros comuns de domínio retornam 500 sem contrato estável

- **Tipo:** defeito confirmado
- **Severidade:** alta
- **Arquivo e linha:** `api/src/main/java/com/hemovia/api/exceptions/GlobalExceptionHandler.java:17-61`; `api/src/main/java/com/hemovia/api/service/implementation/DonationServiceImpl.java:41-45,127-132`
- **Esperado:** recurso inexistente deve retornar 404 e formato de ID inválido deve retornar 400, ambos em corpo de erro consistente.
- **Observado:** o handler cobre somente duplicidade de usuário, credenciais e validação de bean; o serviço lança `EntityNotFoundException` e `IllegalArgumentException`, sem manipuladores correspondentes.
- **Impacto:** clientes recebem erro interno para entrada ou ausência esperada, dificultando tratamento e revelando falha operacional.
- **Reprodução ou justificativa estática:** requisitar `GET /donations/not-a-uuid` alcança `parseId`, que lança `IllegalArgumentException`; não há `@ExceptionHandler` para ela no advice.
- **Recomendação:** mapear explicitamente exceções de ID/validação para 400 e inexistência para 404, com testes MVC para cada endpoint afetado.

### API-007 — Registro de SQL pode expor dados pessoais em logs

- **Tipo:** risco de segurança
- **Severidade:** média
- **Arquivo e linha:** `api/src/main/resources/application.yaml:11-14`
- **Esperado:** produção deve registrar consultas somente com política que impeça exposição de dados pessoais e parâmetros sensíveis.
- **Observado:** `spring.jpa.show-sql` está habilitado sem perfil de ambiente que o restrinja.
- **Impacto:** dependendo do logger/configuração do provedor, consultas e parâmetros ligados a e-mail, CPF e telefone podem ir para logs.
- **Reprodução ou justificativa estática:** a propriedade está global no único arquivo de configuração encontrado; os modelos `User` e `Person` armazenam identificadores pessoais.
- **Recomendação:** desabilitar em configuração padrão/produtiva, criar perfil de desenvolvimento com retenção apropriada e revisar logging de bind parameters.

### API-008 — Fluxo de doação não declara atomicidade

- **Tipo:** risco de consistência
- **Severidade:** média
- **Arquivo e linha:** `api/src/main/java/com/hemovia/api/service/implementation/DonationServiceImpl.java:49-57,88-105`
- **Esperado:** criação de bolsa e doação deve ser atômica: ambas persistem ou nenhuma persiste.
- **Observado:** o serviço chama dois repositórios e não declara `@Transactional`; a primeira gravação ocorre antes da segunda.
- **Impacto:** uma falha posterior pode deixar bolsa órfã e estoque inconsistente.
- **Reprodução ou justificativa estática:** seguir `create` até `resolveBloodBag`, que chama `bloodBagRepository.save`, antes de `donationRepository.save`; não há anotação transacional na classe ou método.
- **Recomendação:** definir uma transação de serviço para a operação composta e testar rollback ao falhar a persistência da doação.

### API-009 — Exposição de actuator não foi delimitada por evidência

- **Tipo:** risco sem reprodução
- **Severidade:** média
- **Arquivo e linha:** `api/src/main/java/com/hemovia/api/config/SecurityConfig.java:30-33`; `api/pom.xml:33-36`
- **Esperado:** somente endpoints de gestão explicitamente necessários devem ser expostos e protegidos conforme o ambiente.
- **Observado:** `/actuator/**` está liberado, mas não há configuração versionada de exposição nem teste que demonstre a superfície efetiva.
- **Impacto:** se endpoints adicionais forem habilitados por ambiente, poderão ficar acessíveis sem autenticação.
- **Reprodução ou justificativa estática:** a permissão é abrangente; a confirmação de quais endpoints estão ativos depende de executar a aplicação com a configuração do ambiente, o que não foi realizado nesta tarefa.
- **Recomendação:** restringir a rota e configurar explicitamente a lista de endpoints expostos por perfil, com teste de integração para acesso anônimo.

## Pontos positivos comprovados

- `SecurityConfig.java:28` usa sessões sem estado e `SecurityConfig.java:41-43` configura BCrypt para senhas.
- `AuthServiceImpl.java:35-51` normaliza e-mail/CPF antes de persistir e usa o codificador de senha; `V2__create_users_table.sql:4-7` reforça unicidade e não nulidade no banco.
- Controladores usam DTOs e `@Valid`; `HospitalRequestDTO.java:6-25` define limites para coordenadas e prioridade.
- `application.yaml:5-10` referencia credenciais de banco por variáveis de ambiente, sem valores de conexão sensíveis versionados.

## Limitações e itens não verificados

- Não foram executados Maven, PostgreSQL, Docker nem requisições HTTP nesta tarefa; esta entrega separa defeitos estáticos de riscos que exigem execução.
- Não foram encontrados contratos OpenAPI, testes de autorização de endpoints, políticas de papel/permissão ou configuração versionada de CORS/rate limit; a ausência é registrada como lacuna de evidência, não como prova de inexistência em ambiente.
- Não foram incluídos valores de segredos, arquivos `.env` não versionados ou credenciais na análise.

## Complemento de validação

A suíte Maven foi executada posteriormente pelo orquestrador com JDK 22, compilando para Java 21: 17 testes, 16 aprovados e 1 erro de contexto por conexão PostgreSQL recusada. O bloqueio de migrações duplicadas continua confirmado por inspeção estática, sem reprodução em banco ativo. A classificação de severidade foi calibrada para alta conforme o design: bloqueio de fluxo central e risco à integridade, sem evidência de perda grave de dados em ambiente real.

