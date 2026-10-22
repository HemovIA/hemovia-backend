# Revisão de AED, testes e operação

## Base e método

- **Commit analisado:** `33c34209340eb7e9ef78921134d233feaf5ab891` (`revisao-projeto: registrar especificacao e plano autorizado`).
- **Estado local no início:** limpo (`git status --short` sem saída).
- **Escopo inspecionado:** `aed-u1/`, as estruturas em `api/src/main/java/com/hemovia/api/structures/basic/`, os testes Java, `api/pom.xml`, migrações Flyway, `api/docker-compose.yml`, `application.yaml`, `.env.example`, README e arquivos de automação versionados.
- **Regra de interpretação:** resultado de leitura estática não é aprovação de execução. “Não verificada” significa que a ferramenta, dependência ou ambiente não permitiu a verificação.

## Estruturas de dados (AED)

| Área | Evidência e invariantes observados | Casos extremos e testes | Resultado |
|---|---|---|---|
| Lista de estoque C | `stock_add`, `stock_find` e `stock_remove` mantêm uma cadeia por `next`; ao remover a cabeça ou nó interno, o elo é religado e `size` é reduzido (`aed-u1/c/hemovia_aed_u1.c:23-47`). Os nós remanescentes são liberados por `stock_destroy` (`:88-91`). | O `main` demonstra busca existente e remoção ausente (`:124-126`), mas não há teste automatizado de lista vazia, remoção da cabeça/meio/fim, duplicidade ou falha de alocação. | Parcialmente evidenciado por leitura e demonstração; demonstração posteriormente compilada e executada com sucesso; invariantes extremas continuam sem testes. |
| Fila FIFO C e Java | C atualiza `rear` para nulo ao retirar o último nó (`aed-u1/c/hemovia_aed_u1.c:58-65`); Java mantém o mesmo invariante e lança `EmptyQueueException` em fila vazia (`api/src/main/java/com/hemovia/api/structures/basic/HospitalRequestQueue.java:16-35`). | A demonstração retira somente o primeiro de dois itens (`aed-u1/c/hemovia_aed_u1.c:128-131`). Não há teste para fila vazia, `peek`, transição para vazia ou múltiplas operações. | Invariante FIFO presente estaticamente; cobertura automatizada ausente. |
| Pilha/undo C e Java | Push/pop operam no topo; o undo localiza a bolsa antes de remover o evento, preservando-o quando a bolsa não existe (`aed-u1/c/hemovia_aed_u1.c:68-85`; `api/src/main/java/com/hemovia/api/structures/basic/StockHistoryStack.java:38-47`). | O C demonstra um undo bem-sucedido (`aed-u1/c/hemovia_aed_u1.c:133-138`). Não há teste para pilha vazia, bolsa ausente, valor inválido de status ou sequência de undos. | Sem defeito estático confirmado nesse fluxo, mas sem prova de execução. |
| ABB e recursão | Inserção preserva chaves menores à esquerda e maiores à direita; duplicatas são ignoradas. O percurso inorder é recursivo em C e Java (`aed-u1/c/hemovia_aed_u1.c:104-117`; `api/src/main/java/com/hemovia/api/structures/basic/BinarySearchTree.java:12-38`). | Não há teste para árvore vazia, duplicata, entrada ordenada/degenerada ou profundidade grande. A recursão pode esgotar a pilha com entrada adversarial; é risco, não defeito reproduzido. | Exemplo didático coerente por leitura; cobertura insuficiente. |

## Testes e build

Os testes versionados cobrem contexto Spring, autenticação/JWT e parte de criação de bolsa (`api/src/test/java/com/hemovia/api/`). Não há arquivo de teste para `structures/basic`, para o executável C, para Hospital/Donation, nem teste de integração de migração PostgreSQL. Assim, os testes existentes não comprovam as invariantes AED ou o fluxo operacional completo.

O `pom.xml` declara Java 21 (`api/pom.xml:30`). Inicialmente foram encontrados Java 8 no PATH e Java 17 no Maven. A checagem complementar localizou o JDK 22 já instalado e executou Maven com JAVA_HOME ajustado somente no processo: compilação com release 21 concluída; 17 testes executados, 16 passaram e 1 teve erro de contexto por conexão PostgreSQL recusada. A suíte completa falhou e não foi contabilizada como aprovada. O teste documental Node não substitui testes Maven/Java/C.

## Banco, Docker, automação e documentação

| Área | Evidência | Resultado |
|---|---|---|
| Banco/Flyway | Há duas migrações de versão `V2`: `api/src/main/resources/db/migration/V2__create_hospital_table.sql:1` e `api/src/main/resources/db/migration/V2__create_users_table.sql:1`. `api/src/main/resources/application.yaml:15-17` habilita Flyway. | Defeito estático QUA-001: versões duplicadas impedem uma sequência Flyway inequívoca; a tentativa posterior de carregar o contexto falhou antes por ausência de conexão PostgreSQL; a duplicidade ainda não foi reproduzida contra um banco ativo. |
| Configuração de banco | Datasource e Compose dependem de `POSTGRES_*` (`api/src/main/resources/application.yaml:6-10`, `api/docker-compose.yml:5-17`); `.env.example` contém chaves vazias. | Configuração requer valores reais; não verificada contra PostgreSQL. |
| Docker | O Compose define PostgreSQL com healthcheck, volume e rede (`api/docker-compose.yml:1-24`). Não existe comando `docker` no ambiente. | Não verificado; contêiner, healthcheck e migrações não foram executados. |
| Automação | Não há diretório `.github` versionado, embora o README apresente “GitHub Actions” como tecnologia (`README.md:15`). | Risco de documentação/entrega: não há workflow CI versionado para inspecionar ou executar. |
| Documentação | O README tem duas seções “Como rodar o projeto” sem instruções efetivas (`README.md:68-70` e `:103-105`). Já `aed-u1/README.md:30-37` descreve a compilação C e declara Java 21. | Defeito de documentação QUA-003: a execução do backend/Docker não é reprodutível apenas pelo README. |

## Achados priorizados

### QUA-001 — Migrações Flyway com versão duplicada

- **Tipo / severidade:** defeito confirmado por análise estática / alta.
- **Arquivo e linhas:** `api/src/main/resources/db/migration/V2__create_hospital_table.sql:1`; `api/src/main/resources/db/migration/V2__create_users_table.sql:1`.
- **Esperado:** cada migração versionada deve ter versão única e ordem determinística.
- **Observado:** dois arquivos usam a mesma versão `V2`.
- **Impacto:** Flyway não consegue estabelecer uma ordem de migrações válida, bloqueando inicialização/migração do banco.
- **Reprodução ou justificativa estática:** os dois nomes versionados são evidência direta; `api/src/main/resources/application.yaml:15-17` habilita Flyway. A reprodução com Maven/PostgreSQL ficou não verificada pelos impedimentos abaixo.
- **Recomendação:** renumerar uma das migrações conforme a sequência pretendida e adicionar teste de migração contra PostgreSQL efêmero.

### QUA-002 — Invariantes AED não possuem testes automatizados

- **Tipo / severidade:** risco de qualidade / média.
- **Arquivo e linhas:** `aed-u1/c/hemovia_aed_u1.c:23-117`; `api/src/main/java/com/hemovia/api/structures/basic/BloodBagStockList.java:18-54`; `api/src/main/java/com/hemovia/api/structures/basic/HospitalRequestQueue.java:16-35`; `api/src/main/java/com/hemovia/api/structures/basic/StockHistoryStack.java:38-47`; `api/src/main/java/com/hemovia/api/structures/basic/BinarySearchTree.java:12-38`.
- **Esperado:** operações de lista, fila, pilha/undo e ABB devem ter testes para invariantes e limites, incluindo estruturas vazias, transições de tamanho e entradas adversariais.
- **Observado:** há demonstração C de caminho feliz, mas nenhum teste versionado das estruturas AED/C; os testes Java existentes cobrem outras áreas.
- **Impacto:** regressões de ponteiros/elos, FIFO/LIFO, undo e recursão podem alcançar entrega sem detecção automatizada.
- **Reprodução ou justificativa estática:** a busca em `api/src/test/java` não contém testes para `structures/basic`; o único programa C é a demonstração em `main`.
- **Recomendação:** criar testes JUnit parametrizados para as estruturas Java e um alvo de teste C que cubra vazio, cabeça/meio/fim, FIFO/LIFO, undo sem bolsa e ABB degenerada.

### QUA-003 — Instruções operacionais e CI não permitem reprodução completa

- **Tipo / severidade:** defeito de documentação/operabilidade / média.
- **Arquivo e linhas:** `README.md:15`, `README.md:68-70`, `README.md:103-105`; ausência de `.github/` no inventário versionado.
- **Esperado:** README deve explicar pré-requisitos, variáveis, banco, Docker e comando de teste; automação declarada deve estar versionada ou ser explicitamente indicada como externa.
- **Observado:** as seções de execução são placeholders e não há workflow CI no repositório, apesar da tecnologia declarada.
- **Impacto:** um mantenedor não consegue reproduzir o ambiente nem saber qual validação CI protege a aplicação.
- **Reprodução ou justificativa estática:** leitura das seções citadas e inventário por `git ls-files`.
- **Recomendação:** documentar Java 21, preenchimento seguro do `.env`, `docker compose up`, comando Maven e diagnóstico; versionar workflow CI ou corrigir a alegação no README.

## Comandos executados e limitações

| Comando | Diretório | Resultado | Limitação / interpretação |
|---|---|---|---|
| `git status --short` | raiz | saída vazia | Estado local inicial limpo; não valida comportamento. |
| `git ls-files` | raiz | êxito; inventário usado na revisão | Só evidencia arquivos versionados. |
| `java -version` | raiz | Java 1.8.0_401 | Incompatível com Java 21 declarado no POM. |
| `mvn -v` | raiz | Maven 3.10.0 usou Java 17.0.14 | Também incompatível com Java 21; `api/mvnw.cmd -v` falhou com “Cannot start maven from wrapper”. A verificação inicial não executou test; a checagem complementar abaixo usou JDK 22 e executou a suíte real. |
| `gcc --version` | raiz | GCC 15.2.0 disponível | Disponibilidade não comprova compilação. |
| `gcc -std=c11 -Wall -Wextra -pedantic aed-u1/c/hemovia_aed_u1.c -o <temporário>` | raiz | exit 1; `cc1.exe` foi invocado e não concluiu, sem diagnóstico do compilador | Na tentativa inicial o executável não foi gerado; a repetição complementar abaixo compilou e executou o exemplo com sucesso. A saída foi direcionada a diretório temporário isolado, não ao repositório. |
| `docker` | raiz | comando não encontrado | Docker/Compose, banco e healthcheck não verificados. |

## Conclusão desta revisão

As estruturas AED apresentam implementação didática coerente em leitura estática e limpeza explícita no C, mas não dispõem de testes automatizados de invariantes e extremos. A operação não está aprovada: a versão duplicada de migração é um bloqueio estático; na checagem complementar o exemplo C passou, a suíte Maven falhou em 1 de 17 testes e Docker/PostgreSQL continuam sem validação integrada. Prioridade imediata: corrigir QUA-001; em seguida, tornar testes AED e instruções/CI reprodutíveis.

## Checagem complementar do orquestrador

Após recuperar os arquivos da sessão, foram repetidas as verificações com ferramentas já instaladas, sem alterar código do produto. O Java 22 foi selecionado somente para o processo Maven; o projeto continuou compilando com release 21.

| Comando | Diretório | Resultado | Limitação / interpretação |
|---|---|---|---|
| `gcc -std=c11 -Wall -Wextra -pedantic aed-u1/c/hemovia_aed_u1.c -o <temporário>/hemovia-aed.exe` | raiz | exit 0 | Compilação da demonstração; não substitui testes de invariantes. |
| `<temporário>/hemovia-aed.exe` | raiz | exit 0 | Imprimiu busca existente, remoção ausente, FIFO REQ-101, undo para RESERVED e percurso ABB 1 3 6 8 10. |
| `mvn -o -f api/pom.xml test`, com JAVA_HOME no JDK 22 instalado | raiz | exit 1; 17 testes, 16 aprovados, 1 erro, 0 ignorados | Compilou 49 fontes principais e 5 classes de teste; ApiApplicationTests.contextLoads falhou por conexão PostgreSQL recusada. Sem baixar dependências. Não foi iniciado banco nem aplicada migração. |

### QUA-004 — Auditoria mecânica não cobre o código Java/C

- **Tipo / severidade:** risco de cobertura / média.
- **Arquivo e linhas:** `onpspec.config.json:1-14`; `.spec/constituicao.md:23-27`.
- **Esperado:** a rastreabilidade deve explicitar quais componentes e comportamentos realmente verifica.
- **Observado:** srcGlobs aponta para src/**, enquanto a API está em api/; a regra recomendada de segredos olha src/**/*.js. O comando de teste do motor é Node.
- **Impacto:** um gate documental aprovado pode dar falsa impressão de aprovação da API Java e do módulo C.
- **Reprodução ou justificativa estática:** comparar os caminhos configurados com o inventário; o audit informa GLOB_SEM_ARQUIVOS para a regra de segredos.
- **Recomendação:** em mudança separada, mapear fontes reais e tarefas correspondentes e integrar provas Java/C ao CI e à rastreabilidade. Manter claro que o gate desta revisão aprova artefatos documentais, sem alterar regras apenas para zerar avisos.

### QUA-005 — Teste de contexto depende de PostgreSQL externo

- **Tipo / severidade:** risco de reprodutibilidade / média.
- **Arquivo e linhas:** `api/src/test/java/com/hemovia/api/ApiApplicationTests.java:1-13`; `api/src/main/resources/application.yaml:5-17`.
- **Esperado:** a equipe deve conseguir reproduzir a suíte com dependências de teste documentadas e isoladas.
- **Observado:** contextLoads carrega a configuração padrão e tenta conectar ao banco local; sem PostgreSQL disponível, a suíte completa falha, embora os outros 16 testes passem.
- **Impacto:** validar o backend exige configuração externa não explicada pelo README; a inicialização e as migrações permanecem sem prova integrada.
- **Reprodução ou justificativa estática:** execução real Maven com JDK 22; relatório Surefire registra Connection refused e 1 erro em 17 testes. Valores de credenciais foram omitidos.
- **Recomendação:** documentar dependências e criar perfil de teste ou PostgreSQL efêmero para integração; preservar o teste de contexto e validar todas as migrações. Não pular o teste para declarar aprovação.

### QUA-006 — Demonstração C imprime a cópia local em vez da bolsa restaurada

- **Tipo / severidade:** defeito confirmado na demonstração / baixa.
- **Arquivo e linhas:** `aed-u1/c/hemovia_aed_u1.c:26`; `aed-u1/c/hemovia_aed_u1.c:78-85`; `aed-u1/c/hemovia_aed_u1.c:133-138`.
- **Esperado:** o exemplo de undo deve exibir o estado AVAILABLE restaurado na bolsa armazenada no estoque.
- **Observado:** stock_add copia bag para o nó; main altera e imprime a variável local bag, enquanto history_undo modifica o objeto no estoque. A execução imprime RESERVED apesar de anunciar que voltou ao estado anterior.
- **Impacto:** a demonstração visual não comprova o undo e pode induzir avaliação incorreta; o algoritmo de undo em si não foi demonstrado incorreto.
- **Reprodução ou justificativa estática:** compilar e executar o programa existente; a saída real contém “Undo: BAG-999 volta para RESERVED”, enquanto previous_status é AVAILABLE.
- **Recomendação:** operar e consultar a bolsa armazenada por stock_find durante a demonstração e adicionar uma asserção que compare o status do estoque com previous_status. Exit 0 do executável demonstra execução, não correção de toda a saída.
