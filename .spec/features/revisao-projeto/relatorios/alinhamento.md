# Alinhamento entre objetivos documentados e implementação

> Escopo: T-001 / AC-001 — levantamento estático, sem correções.
> Referência do produto: `91d1f981e8860df0cb53fa9472edd8f1bcd91e89`.
> Fontes: `README.md`, `diagramas/README.md`, `aed-u1/README.md` e arquivos
> versionados nesse commit. A referência não possui diferença de produto em
> relação ao diretório inspecionado (`git diff --name-status <referência> -- .
> :(exclude).spec :(exclude).agents :(exclude)onpspec.config.json`, sem saída).

## Método e legenda

O inventário inclui os arquivos versionados, inclusive `.gitignore`,
`api/.env.example`, `.gitattributes`, Maven Wrapper e `.gitkeep`; exclui saídas
de build, caches e os artefatos não rastreados da própria revisão. Uma
funcionalidade é **implementada** quando há código correspondente; **parcial**
quando só parte do resultado documentado está no código; **planejada** quando o
diagrama a apresenta como objetivo mas não há código correspondente; e **sem
evidência** quando o README declara uma capacidade sem arquivo configurado ou
código que a demonstre. A ausência de uma funcionalidade planejada abaixo não é
achado de defeito: é diferença de escopo entre o backend atual e o backlog de
44 histórias.

As referências `arquivo:linha` apontam para o estado inspecionado. “Sem teste
real localizado” significa que não há teste Java/C versionado para aquela
funcionalidade; não é substituído pelo teste Node deste relatório.

## Componentes encontrados

| Componente | Estado | Evidência de implementação | Testes reais localizados | Conclusão |
| --- | --- | --- | --- | --- |
| Documentação e backlog | implementada | `README.md:5-18`; `diagramas/README.md:14-66` e histórias `:100-1919`; `aed-u1/README.md:3-37` | Não aplicável | O produto documenta a simulação, os objetivos e os 44 fluxos. |
| API Spring MVC | implementada | bootstrap em `api/src/main/java/com/hemovia/api/ApiApplication.java:6`; controladores em `api/src/main/java/com/hemovia/api/controller/AuthController.java:15-38`, `api/src/main/java/com/hemovia/api/controller/BloodBagController.java:13-46`, `api/src/main/java/com/hemovia/api/controller/DonationController.java:13-46` e `api/src/main/java/com/hemovia/api/controller/HospitalController.java:11-25` | `api/src/test/java/com/hemovia/api/controller/AuthControllerTest.java:51-151` | Há API para autenticação, doações, bolsas e criação de hospital. |
| Domínio, serviços e persistência | implementada | entidades `api/src/main/java/com/hemovia/api/model/BloodBag.java:10-11`, `api/src/main/java/com/hemovia/api/model/Donation.java:8-9`, `api/src/main/java/com/hemovia/api/model/Hospital.java:7-8`, `api/src/main/java/com/hemovia/api/model/User.java:7-8`; serviços em `service/implementation`; Flyway em `api/src/main/resources/db/migration/V1__create_initial_schema.sql:1-27`, `api/src/main/resources/db/migration/V2__create_hospital_table.sql:1-8`, `api/src/main/resources/db/migration/V2__create_users_table.sql:1-9` | `api/src/test/java/com/hemovia/api/service/implementation/BloodBagServiceImplTest.java:36-57`, `api/src/test/java/com/hemovia/api/service/implementation/AuthServiceImplTest.java:44-184` | Há JPA/PostgreSQL e regras pontuais de validade e autenticação. |
| Autenticação JWT | implementada | rotas `/auth` em `api/src/main/java/com/hemovia/api/controller/AuthController.java:16-38`; serviço em `api/src/main/java/com/hemovia/api/service/JwtService.java:13-41`; filtro/configuração em `api/src/main/java/com/hemovia/api/config/JwtAuthenticationFilter.java:16-55` e `api/src/main/java/com/hemovia/api/config/SecurityConfig.java:15-40` | `api/src/test/java/com/hemovia/api/service/JwtServiceTest.java:21-45`; `api/src/test/java/com/hemovia/api/service/implementation/AuthServiceImplTest.java:44-184`; `api/src/test/java/com/hemovia/api/controller/AuthControllerTest.java:51-151` | Implementada além do escopo explícito das histórias POO do diagrama. |
| Estruturas Java AED U1 | implementada | lista `api/src/main/java/com/hemovia/api/structures/basic/BloodBagStockList.java:8-52`; fila `api/src/main/java/com/hemovia/api/structures/basic/HospitalRequestQueue.java:5-38`; pilha/undo `api/src/main/java/com/hemovia/api/structures/basic/StockHistoryStack.java:7-49`; ABB complementar `api/src/main/java/com/hemovia/api/structures/basic/BinarySearchTree.java:4-39` | Sem teste real localizado | As estruturas existem, porém não estão integradas aos serviços (o próprio `aed-u1/README.md:24-26` usa “pode compor”). |
| Exemplo C AED U1 | implementada | `aed-u1/c/hemovia_aed_u1.c:1-145` (lista, fila, pilha, ABB, destrutores e `main`) | Sem teste C automatizado localizado; execução manual é descrita em `aed-u1/README.md:28-37` | Exemplo didático separado da API, não um módulo de produção integrado. |
| Build e banco local | implementada | Java 21 e dependências em `api/pom.xml:18-112`; configuração PostgreSQL/Flyway em `api/src/main/resources/application.yaml:1-17`; PostgreSQL em `api/docker-compose.yml:1-23` | `api/src/test/java/com/hemovia/api/ApiApplicationTests.java:6-11` existe, mas não foi executado nesta T-001 | O compose sobe somente o banco, não um container da API. |
| OpenAPI/Swagger | sem evidência | Declarado em `README.md:14`; não há dependência ou configuração OpenAPI/Swagger no `api/pom.xml:31-111` | Sem teste real localizado | Declaração não confirmada por arquivo versionado. |
| CI/CD | sem evidência | Declarado em `README.md:15`; inventário versionado não contém `.github/workflows` | Sem teste real localizado | Não confundir o diagrama SOUS01 com uma esteira implementada. |
| Docker da aplicação | parcial | Declarado em `README.md:16`; `api/docker-compose.yml:1-23` define apenas `postgres` | Sem teste real localizado | Há containerização do banco, não imagem/serviço da API. |
| Clean Architecture e SOLID | parcial | Declarado em `README.md:14`; código está organizado em controller/service/repository/model, por exemplo `api/src/main/java/com/hemovia/api/controller/BloodBagController.java:13-46` e `api/src/main/java/com/hemovia/api/service/implementation/BloodBagServiceImpl.java:19-105` | Sem teste arquitetural localizado | Não há pacotes Domain/Application/Infrastructure/Presentation declarados como o README afirma. |
| Interface Lo-Fi e painel web | planejada | Protótipo externo citado em `README.md:34-37`; não há projeto frontend versionado | Sem teste real localizado | Não é defeito da API atual; é entrega/documentação externa ou futura. |

## Matriz das funcionalidades de README e diagramas

| História documentada | Classificação | Evidência de implementação e testes reais | Justificativa |
| --- | --- | --- | --- |
| POOUS01 — cadastro de doação e entrada de bolsa | implementada | Rotas de doação `api/src/main/java/com/hemovia/api/controller/DonationController.java:13-46`, associação de bolsa em `api/src/main/java/com/hemovia/api/service/implementation/DonationServiceImpl.java:49-106`, bolsa em `api/src/main/java/com/hemovia/api/controller/BloodBagController.java:13-46`; teste real de validade da bolsa `api/src/test/java/com/hemovia/api/service/implementation/BloodBagServiceImplTest.java:36-57` | O fluxo central existe; não foi localizado teste dedicado à associação por doação. |
| POOUS02 — cadastro e controle de validade de bolsas | parcial | CRUD em `api/src/main/java/com/hemovia/api/controller/BloodBagController.java:23-46`; rejeição de validade expirada em `api/src/main/java/com/hemovia/api/service/implementation/BloodBagServiceImpl.java:44-105`; testes `api/src/test/java/com/hemovia/api/service/implementation/BloodBagServiceImplTest.java:36-57` | Cadastro e validação na gravação existem; não há evidência de processo contínuo de expiração/controle de estoque descrito no diagrama `diagramas/README.md:143-181`. |
| POOUS03 — gestão de cadastro de hospitais | parcial | POST em `api/src/main/java/com/hemovia/api/controller/HospitalController.java:11-25`; persistência em `api/src/main/java/com/hemovia/api/service/implementation/HospitalServiceImpl.java:10-33` e migração `api/src/main/resources/db/migration/V2__create_hospital_table.sql:1-8` | Há criação, mas não CRUD/consulta nem teste real localizado para a gestão completa do diagrama `diagramas/README.md:184-222`. |
| POOUS04 — emissão de requisições hospitalares | planejada | Só a estrutura genérica `api/src/main/java/com/hemovia/api/structures/basic/HospitalRequestQueue.java:5-38`; não há controller/serviço de requisição | O diagrama `diagramas/README.md:225-263` é intenção futura; a fila isolada não implementa emissão REST. |
| POOUS05 — tela inicial | planejada | Nenhum frontend versionado | Fluxo de UI em `diagramas/README.md:266-310`, fora do backend atual. |
| POOUS06 — matching, compatibilidade e FEFO | planejada | Nenhum algoritmo/rota correspondente localizado | Requisito futuro no diagrama `diagramas/README.md:313-360`. |
| POOUS07 — roteirização e rotas | planejada | Nenhum módulo de grafo/rota localizado | Requisito futuro no diagrama `diagramas/README.md:363-403`. |
| POOUS08 — painéis e estatística | planejada | Nenhum endpoint de indicadores/painel localizado | Requisito futuro no diagrama `diagramas/README.md:406-446`. |
| POOUS09 — transporte e cadeia fria | planejada | Nenhuma telemetria/transporte localizado | Requisito futuro no diagrama `diagramas/README.md:449-491`. |
| POOUS10 — cancelamento e liberação de estoque | planejada | Nenhum agregado de requisição/reserva localizado | Requisito futuro no diagrama `diagramas/README.md:494-532`. |
| POOUS11 — recebimento e baixa de entrega | planejada | Nenhum fluxo de entrega localizado | Requisito futuro no diagrama `diagramas/README.md:535-577`. |
| POOUS12 — reserva concorrente | planejada | Nenhuma reserva/lock de bolsa localizado | Requisito futuro no diagrama `diagramas/README.md:580-626`. |
| AEDUS01 — fila de requisições | implementada | `api/src/main/java/com/hemovia/api/structures/basic/HospitalRequestQueue.java:5-38`; equivalente C em `aed-u1/c/hemovia_aed_u1.c:50-63` | FIFO está implementada nas duas linguagens; sem teste automatizado localizado. |
| AEDUS02 — lista de estoque | implementada | `api/src/main/java/com/hemovia/api/structures/basic/BloodBagStockList.java:8-52`; equivalente C em `aed-u1/c/hemovia_aed_u1.c:23-48` | Inserção, busca e remoção existem; sem teste automatizado localizado. |
| AEDUS03 — pilha e undo | implementada | `api/src/main/java/com/hemovia/api/structures/basic/StockHistoryStack.java:7-49`; equivalente C em `aed-u1/c/hemovia_aed_u1.c:65-85` | Push/pop/undo existem; sem teste automatizado localizado. |
| AEDUS04 — Dijkstra | planejada | Nenhum grafo/Dijkstra localizado | Requisito futuro em `diagramas/README.md:811-854`. |
| AEDUS05 — FEFO/heap | planejada | Nenhum heap/fila de prioridade localizado | Requisito futuro em `diagramas/README.md:857-893`. |
| AEDUS06 — tabela hash | planejada | Nenhuma estrutura hash própria localizada | Requisito futuro em `diagramas/README.md:896-937`. |
| AEDUS07 — compatibilidade ABO/Rh | planejada | Enum de tipo sanguíneo existe em `api/src/main/java/com/hemovia/api/model/enums/BloodType.java:3-4`, sem matriz/regra de compatibilidade | O enum não prova a funcionalidade do diagrama `diagramas/README.md:940-979`. |
| AEDUS08 — ordenação para validade | planejada | Nenhum MergeSort/QuickSort localizado | Requisito futuro em `diagramas/README.md:982-1016`. |
| AEDUS09 — busca binária de hospitais | planejada | Nenhum vetor indexado/busca binária de hospitais localizado | Requisito futuro em `diagramas/README.md:1019-1059`. |
| AEDUS10 — buffer circular de telemetria | planejada | Nenhum buffer/telemetria localizado | Requisito futuro em `diagramas/README.md:1062-1104`. |
| ESTUS01 — estoque por tipo/componente | planejada | Nenhum cálculo/endpoint estatístico localizado | Requisito futuro em `diagramas/README.md:1107-1143`. |
| ESTUS02 — tendência e dispersão de demanda | planejada | Nenhum cálculo estatístico localizado | Requisito futuro em `diagramas/README.md:1146-1182`. |
| ESTUS03 — tempos de atendimento/entrega | planejada | Nenhuma métrica de entrega localizada | Requisito futuro em `diagramas/README.md:1185-1223`. |
| ESTUS04 — probabilidade de desabastecimento | planejada | Nenhum modelo probabilístico localizado | Requisito futuro em `diagramas/README.md:1226-1260`. |
| ESTUS05 — taxa de descarte por validade | planejada | Nenhum relatório de descarte localizado | Requisito futuro em `diagramas/README.md:1263-1295`. |
| ESTUS06 — previsão de demanda | planejada | Nenhuma regressão/média móvel localizada | Requisito futuro em `diagramas/README.md:1298-1330`. |
| ESTUS07 — painel consolidado/REST | planejada | Nenhum endpoint de indicadores localizado | Requisito futuro em `diagramas/README.md:1333-1371`. |
| SOUS01 — pipeline CI/CD | sem evidência | Sem `.github/workflows` no inventário versionado | O diagrama `diagramas/README.md:1374-1408` e a menção no README não equivalem a pipeline configurado. |
| SOUS02 — deploy em nuvem | planejada | Nenhum manifesto/infraestrutura de nuvem localizado | Requisito futuro em `diagramas/README.md:1411-1445`. |
| SOUS03 — telemetria multithread | planejada | Nenhum consumidor/telemetria localizado | Requisito futuro em `diagramas/README.md:1448-1484`. |
| SOUS04 — sincronização anti-race | planejada | Nenhuma reserva concorrente localizada | Requisito futuro em `diagramas/README.md:1487-1521`. |
| SOUS05 — cenários de carga | planejada | Nenhum script de carga localizado | Requisito futuro em `diagramas/README.md:1524-1563`. |
| SOUS06 — orçamento em nuvem | planejada | Nenhuma planilha/configuração de custo localizada | Requisito futuro em `diagramas/README.md:1566-1596`. |
| SOUS07 — rollback automatizado | planejada | Nenhum pipeline/deploy localizado | Requisito futuro em `diagramas/README.md:1599-1639`. |
| RSDUS01 — topologia da hemorrede | planejada | Nenhuma configuração de topologia localizada | Requisito futuro em `diagramas/README.md:1642-1674`. |
| RSDUS02 — requisitos técnicos | planejada | Nenhum artefato técnico de rede localizado | Requisito futuro em `diagramas/README.md:1677-1709`. |
| RSDUS03 — contratos de telemetria | planejada | Nenhum contrato/endpoint de telemetria localizado | Requisito futuro em `diagramas/README.md:1712-1750`. |
| RSDUS04 — benchmark de rede | planejada | Nenhum script k6/JMeter ou métrica localizado | Requisito futuro em `diagramas/README.md:1753-1785`. |
| RSDUS05 — monitoramento HTTP | planejada | Actuator é dependência (`api/pom.xml:33-36`), mas não há métrica/alerta configurado | A dependência isolada não implementa o fluxo de `diagramas/README.md:1788-1818`. |
| RSDUS06 — painel de rede/telemetria | planejada | Nenhum painel/ingestão localizado | Requisito futuro em `diagramas/README.md:1821-1851`. |
| RSDUS07 — migração, TLS e contingência | planejada | Nenhum plano/manifesto TLS localizado | Requisito futuro em `diagramas/README.md:1854-1884`. |
| RSDUS08 — implantação de comunicação | planejada | Nenhuma implantação/certificado/telemetria localizado | Requisito futuro em `diagramas/README.md:1887-1916`. |

## Síntese de alinhamento

O backend atual está alinhado com o núcleo já entregue de doações, bolsas,
persistência, autenticação e estruturas AED básicas. A matriz contém 4 histórias
implementadas, 2 parciais, 36 planejadas e 2 sem evidência; essa contagem serve
ao inventário e não reclassifica histórias futuras como defeitos. As declarações
de Swagger, CI/CD, Docker da aplicação e Clean Architecture exigem evidência ou
ajuste documental em etapa posterior, mas esta T-001 não recomenda nem aplica
correções.

## Limitações desta tarefa

Não foram executados Maven, compilação C, Docker, teste Java/C ou testes de
integração: isso pertence ao levantamento de qualidade/operação. O teste Node
associado a AC-001 valida somente a presença e coerência documental desta
matriz; ele não aprova comportamento funcional Java/C, nem substitui as provas
reais citadas acima.
