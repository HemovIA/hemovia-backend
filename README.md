# 🩸 HEMOVIA — Especificação do Backend

> **Projeto:** HEMOVIA
> **Curso:** Análise e Desenvolvimento de Sistemas — CESAR School
> **Semestre:** 2026.2 — 3º Semestre
> **Tipo:** Projeto Integrador
> **Backend:** Java + Spring Boot
> **Arquitetura:** Clean Architecture + princípios SOLID
> **Dados:** Exclusivamente sintéticos

---

# 1. Visão Geral

O backend do **HEMOVIA** será responsável por implementar as regras de negócio, persistência, algoritmos e integrações necessárias para simular a gestão e distribuição de hemocomponentes.

A aplicação deverá centralizar:

* Gestão de hospitais;
* Gestão de hemocomponentes;
* Controle de estoque;
* Requisições hospitalares;
* Compatibilidade ABO/Rh;
* Priorização FEFO;
* Roteirização por Dijkstra;
* Distribuição;
* Telemetria;
* Indicadores estatísticos;
* Processamento concorrente;
* Comunicação via API REST;
* Monitoramento;
* CI/CD e deploy.

O backend deverá ser desenvolvido de forma modular para evitar que as regras de negócio fiquem acopladas ao framework, banco de dados ou mecanismos externos.

---

# 2. Objetivos Arquiteturais

A arquitetura deverá priorizar:

1. **Separação de responsabilidades**
2. **Baixo acoplamento**
3. **Alta coesão**
4. **Testabilidade**
5. **Manutenibilidade**
6. **Extensibilidade**
7. **Independência de framework**
8. **Independência de banco de dados**
9. **Isolamento dos algoritmos**
10. **Clareza das regras de negócio**

O objetivo é evitar uma arquitetura em que os controllers concentrem regras de negócio ou os services se tornem classes gigantes.

---

# 3. Arquitetura

O backend utilizará uma abordagem baseada em **Clean Architecture**, organizada em quatro grandes camadas:

```text
┌──────────────────────────────────────────────────────┐
│                  PRESENTATION                        │
│                                                      │
│ Controllers / REST / Exception Handlers              │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│                  APPLICATION                         │
│                                                      │
│ Use Cases / DTOs / Ports / Orquestração              │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│                    DOMAIN                            │
│                                                      │
│ Entities / Value Objects / Rules / Algorithms        │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│                 INFRASTRUCTURE                       │
│                                                      │
│ JPA / PostgreSQL / External APIs / Telemetry         │
└──────────────────────────────────────────────────────┘
```

## Regra fundamental

A dependência deve apontar **para dentro**.

```text
Presentation
     ↓
Application
     ↓
Domain
```

A infraestrutura implementa contratos definidos pelas camadas internas:

```text
Infrastructure
     ↓
implements
     ↓
Application / Domain ports
```

O domínio não deverá depender de:

* Spring;
* JPA;
* PostgreSQL;
* HTTP;
* JSON;
* Controllers;
* APIs externas.

---

# 4. Estrutura de Pacotes

A estrutura recomendada:

```text
backend/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── br/
    │   │       └── com/
    │   │           └── hemovia/
    │   │               │
    │   │               ├── HemoviaApplication.java
    │   │               │
    │   │               ├── domain/
    │   │               │   ├── model/
    │   │               │   ├── valueobject/
    │   │               │   ├── enums/
    │   │               │   ├── exception/
    │   │               │   └── service/
    │   │               │
    │   │               ├── application/
    │   │               │   ├── usecase/
    │   │               │   ├── dto/
    │   │               │   │   ├── request/
    │   │               │   │   └── response/
    │   │               │   └── port/
    │   │               │       ├── input/
    │   │               │       └── output/
    │   │               │
    │   │               ├── infrastructure/
    │   │               │   ├── persistence/
    │   │               │   ├── telemetry/
    │   │               │   ├── configuration/
    │   │               │   └── algorithm/
    │   │               │
    │   │               └── presentation/
    │   │                   ├── controller/
    │   │                   ├── handler/
    │   │                   └── mapper/
    │   │
    │   └── resources/
    │       ├── application.yml
    │       └── db/
    │           └── migration/
    │
    └── test/
        └── java/
            └── br/
                └── com/
                    └── hemovia/
```

---

# 5. Camada Domain

A camada `domain` representa o coração do sistema.

Ela deverá conter as regras que representam o negócio do HEMOVIA.

## 5.1 Entidades

Entidades inicialmente previstas:

```text
Hospital
Hemocomponent
BloodBag
Inventory
Request
Allocation
Route
Transport
Telemetry
```

---

# 6. Entidade Hospital

```text
Hospital
├── id
├── name
├── city
├── latitude
├── longitude
└── priority
```

### Responsabilidade

Representar uma unidade hospitalar que pode realizar requisições e receber hemocomponentes.

### Regras

* Deve possuir identificação única;
* Deve possuir nome;
* Deve possuir localização;
* Deve possuir prioridade operacional.

---

# 7. Entidade BloodBag

```text
BloodBag
├── id
├── bloodType
├── rhFactor
├── component
├── collectionDate
├── expirationDate
└── status
```

### Regras

Uma bolsa:

* não pode ser utilizada após a validade;
* deve possuir um componente;
* deve possuir tipo sanguíneo;
* deve possuir fator Rh;
* possui um estado operacional.

### Estados

```text
AVAILABLE
RESERVED
IN_TRANSIT
DELIVERED
EXPIRED
DISCARDED
```

---

# 8. Entidade Inventory

O estoque será responsável pela disponibilidade das bolsas.

```text
Inventory
├── id
├── location
├── bloodBag
└── quantity
```

Entretanto, o sistema deverá manter uma estrutura de acesso eficiente para consultas por:

```text
Tipo sanguíneo
Componente
Validade
Status
```

A disciplina de AED prevê especificamente um **índice de estoque por hash**.

---

# 9. Entidade Request

Representa uma solicitação hospitalar.

```text
Request
├── id
├── hospital
├── bloodType
├── rhFactor
├── component
├── quantity
├── priority
├── deliveryDeadline
└── status
```

### Status

```text
PENDING
ANALYZING
ALLOCATED
IN_TRANSIT
DELIVERED
CANCELLED
```

---

# 10. Entidade Allocation

A entidade `Allocation` representa a decisão do sistema sobre quais bolsas serão destinadas a uma requisição.

```text
Allocation
├── id
├── request
├── selectedBags
├── route
├── createdAt
└── status
```

Essa entidade será importante porque separa:

> **Pedido do hospital**

de

> **Decisão de alocação do sistema**

---

# 11. Entidade Route

Representará o resultado da roteirização.

```text
Route
├── origin
├── destination
├── nodes
├── distance
├── estimatedTime
└── status
```

---

# 12. Entidade Telemetry

Representará dados simulados do transporte.

```text
Telemetry
├── id
├── transport
├── temperature
├── latitude
├── longitude
└── timestamp
```

A telemetria será utilizada para demonstrar a comunicação entre os componentes e o processamento concorrente previsto no projeto.

---

# 13. Value Objects

Sempre que uma informação possuir significado próprio, deverá ser considerada como Value Object em vez de simplesmente utilizar tipos primitivos espalhados pelo sistema.

Exemplos:

```text
BloodType
RhFactor
BloodComponent
Coordinates
Temperature
Money
Distance
```

### Exemplo conceitual

Em vez de:

```java
double temperature;
```

o domínio poderá trabalhar com:

```text
Temperature
├── value
└── unit
```

Isso permite concentrar validações no próprio objeto.

---

# 14. Enums

Enums deverão representar estados e categorias controladas.

```text
BloodType

A
B
AB
O
```

```text
RhFactor

POSITIVE
NEGATIVE
```

```text
BloodComponent

RED_BLOOD_CELLS
PLASMA
PLATELETS
```

```text
BloodBagStatus

AVAILABLE
RESERVED
IN_TRANSIT
DELIVERED
EXPIRED
DISCARDED
```

```text
RequestStatus

PENDING
ANALYZING
ALLOCATED
IN_TRANSIT
DELIVERED
CANCELLED
```

---

# 15. Regras de Domínio

As regras críticas deverão permanecer no domínio.

## Regra 1 — Bolsa vencida

```text
IF expirationDate < currentDate
THEN
    bag cannot be allocated
```

---

## Regra 2 — Compatibilidade

```text
Request
   ↓
Compatibility Rule
   ↓
Compatible Bags
```

A implementação deverá utilizar as regras didáticas ABO/Rh definidas pela equipe e documentadas no projeto.

---

## Regra 3 — FEFO

Entre as bolsas compatíveis disponíveis:

```text
expirationDate ASC
```

A bolsa que vencer primeiro possui maior prioridade.

---

## Regra 4 — Quantidade

Se:

```text
requestedQuantity > availableCompatibleQuantity
```

a requisição não poderá ser totalmente atendida.

O sistema deverá informar a indisponibilidade.

---

# 16. Camada Application

A camada `application` conterá os **casos de uso**.

Um caso de uso representa uma ação que o sistema consegue executar.

---

# 17. Casos de Uso

## Gestão de hospitais

```text
CreateHospital
GetHospital
ListHospitals
UpdateHospital
DeleteHospital
```

## Gestão de bolsas

```text
CreateBloodBag
GetBloodBag
ListBloodBags
UpdateBloodBag
DeleteBloodBag
```

## Estoque

```text
GetInventory
SearchInventory
GetInventoryByBloodType
```

## Requisições

```text
CreateRequest
GetRequest
ListRequests
CancelRequest
```

## Alocação

```text
ProcessRequest
CheckCompatibility
AllocateBloodBags
```

## Roteirização

```text
CalculateRoute
```

## Transporte

```text
StartTransport
UpdateTransport
CompleteTransport
```

## Telemetria

```text
ReceiveTelemetry
GetTelemetry
GetCurrentTemperature
```

## Estatística

```text
GetInventoryStatistics
GetDemandStatistics
GetDisposalStatistics
GetStockoutProbability
```

---

# 18. Caso de Uso Principal

O principal caso de uso será:

```text
ProcessRequest
```

Ele será responsável por orquestrar o fluxo completo.

```text
                    ProcessRequest
                          │
                          ▼
                 Validate Request
                          │
                          ▼
                 Check Inventory
                          │
                          ▼
             Check ABO/Rh Compatibility
                          │
                          ▼
                    Apply FEFO
                          │
                          ▼
                  Reserve Bags
                          │
                          ▼
                  Calculate Route
                          │
                          ▼
                 Create Allocation
                          │
                          ▼
                 Start Transport
```

---

# 19. Não colocar tudo em ProcessRequest

Apesar de `ProcessRequest` orquestrar o fluxo, ele não deverá implementar todas as regras.

Exemplo:

```text
ProcessRequest
    │
    ├── CompatibilityService
    │
    ├── AllocationService
    │
    ├── FefoService
    │
    ├── RouteService
    │
    └── TransportService
```

Assim evitamos um `God Service`.

---

# 20. Ports & Adapters

A comunicação entre aplicação e infraestrutura deverá utilizar interfaces.

Exemplo:

```java
public interface BloodBagRepository {

    Optional<BloodBag> findById(UUID id);

    List<BloodBag> findAvailable();

    void save(BloodBag bloodBag);
}
```

O domínio/aplicação conhece apenas a interface.

A infraestrutura implementa:

```text
BloodBagRepository
        ▲
        │
JpaBloodBagRepository
```

---

# 21. Repository Adapter

Na infraestrutura:

```text
infrastructure/
└── persistence/
    ├── entity/
    ├── repository/
    ├── mapper/
    └── adapter/
```

Exemplo:

```text
BloodBagRepository
        ▲
        │
JpaBloodBagRepositoryAdapter
        │
        ▼
Spring Data JPA
        │
        ▼
PostgreSQL
```

Dessa forma, o domínio não conhece JPA.

---

# 22. DTOs

Entidades de domínio **não deverão ser expostas diretamente pela API**.

A API trabalhará com DTOs.

### Request

```text
CreateRequestRequest
```

```json
{
  "hospitalId": "uuid",
  "bloodType": "O",
  "rhFactor": "POSITIVE",
  "component": "RED_BLOOD_CELLS",
  "quantity": 4,
  "priority": "HIGH",
  "deliveryDeadline": "2026-08-11T18:00:00"
}
```

### Response

```text
RequestResponse
```

```json
{
  "id": "uuid",
  "status": "PENDING",
  "hospital": "Hospital Central",
  "component": "RED_BLOOD_CELLS",
  "quantity": 4
}
```

---

# 23. Controllers

Controllers deverão ser responsáveis apenas por:

1. Receber HTTP;
2. Validar entrada básica;
3. Converter DTO;
4. Chamar o caso de uso;
5. Converter resposta;
6. Retornar HTTP.

### Controller NÃO deverá:

* implementar FEFO;
* calcular Dijkstra;
* verificar compatibilidade;
* manipular diretamente o banco;
* possuir regras de negócio.

---

# 24. API REST

## Hospitais

```http
POST   /api/v1/hospitals
GET    /api/v1/hospitals
GET    /api/v1/hospitals/{id}
PUT    /api/v1/hospitals/{id}
DELETE /api/v1/hospitals/{id}
```

---

## Bolsas

```http
POST   /api/v1/blood-bags
GET    /api/v1/blood-bags
GET    /api/v1/blood-bags/{id}
PUT    /api/v1/blood-bags/{id}
DELETE /api/v1/blood-bags/{id}
```

---

## Estoque

```http
GET /api/v1/inventory
GET /api/v1/inventory/blood-type/{type}
GET /api/v1/inventory/component/{component}
```

---

## Requisições

```http
POST /api/v1/requests
GET  /api/v1/requests
GET  /api/v1/requests/{id}
POST /api/v1/requests/{id}/process
POST /api/v1/requests/{id}/cancel
```

---

## Rotas

```http
POST /api/v1/routes/calculate
GET  /api/v1/routes/{id}
```

---

## Telemetria

```http
POST /api/v1/telemetry
GET  /api/v1/transports/{id}/telemetry
GET  /api/v1/transports/{id}/temperature
```

---

## Dashboard

```http
GET /api/v1/dashboard/summary
GET /api/v1/dashboard/inventory
GET /api/v1/dashboard/demand
GET /api/v1/dashboard/disposal
GET /api/v1/dashboard/stockout
```

---

# 25. Módulo de Algoritmos

Os algoritmos de AED deverão possuir um módulo próprio.

```text
algorithm/
├── graph/
│   ├── Graph
│   ├── GraphNode
│   ├── GraphEdge
│   └── Dijkstra
│
├── inventory/
│   ├── InventoryHash
│   └── InventoryIndex
│
├── priority/
│   ├── FefoQueue
│   └── ExpirationComparator
│
└── compatibility/
    ├── CompatibilityRule
    ├── CompatibilityMatrix
    └── CompatibilityMatcher
```

A documentação do PI prevê especificamente:

* grafo;
* Dijkstra;
* hash;
* fila de prioridade;
* FEFO;
* matching ABO/Rh;
* análise de complexidade.

---

# 26. Dijkstra

O algoritmo deverá receber:

```text
Graph
Origin
Destination
```

e retornar:

```text
RouteResult
├── nodes
├── totalDistance
└── estimatedTime
```

### Fluxo

```text
Graph
  ↓
Dijkstra
  ↓
Shortest Path
  ↓
RouteResult
```

A complexidade deverá ser documentada de acordo com a implementação efetivamente utilizada.

---

# 27. FEFO

O algoritmo receberá:

```text
List<BloodBag>
```

e produzirá:

```text
PriorityQueue<BloodBag>
```

Critério:

```text
expirationDate ASC
```

### Fluxo

```text
Available Bags
      ↓
Compatible Bags
      ↓
FEFO Queue
      ↓
First Expiring
      ↓
Allocation
```

---

# 28. Compatibilidade ABO/Rh

O módulo deverá separar a regra de compatibilidade da aplicação.

```text
CompatibilityService
        │
        ▼
CompatibilityMatcher
        │
        ▼
CompatibilityResult
```

Resultado:

```text
CompatibilityResult
├── compatible
├── reason
└── matchedBags
```

---

# 29. Estratégia de Integração dos Algoritmos

Os algoritmos não deverão existir apenas para demonstração acadêmica.

Eles deverão participar do fluxo real:

```text
                    REQUEST
                       │
                       ▼
                COMPATIBILITY
                       │
                       ▼
                    FEFO
                       │
                       ▼
                  ALLOCATION
                       │
                       ▼
                   DIJKSTRA
                       │
                       ▼
                     ROUTE
```

Isso é particularmente importante porque a rubrica de POO U2 exige integração real dos algoritmos de AED e dos painéis de Estatística.

---

# 30. Estatística

A camada de estatística deverá ser separada das regras transacionais.

```text
application/
└── usecase/
    └── statistics/
```

Exemplos:

```text
CalculateInventoryStatistics
CalculateDemandStatistics
CalculateDisposalRate
CalculateStockoutProbability
```

---

# 31. Indicadores

## Estoque

```text
totalBags
availableBags
reservedBags
expiredBags
```

## Demanda

```text
totalRequests
pendingRequests
completedRequests
averageRequestsPerHospital
```

## Operação

```text
averageProcessingTime
minimumProcessingTime
maximumProcessingTime
```

## Perdas

```text
expiredBags
disposalRate
```

## Risco

```text
stockoutProbability
```

A documentação determina que os indicadores sejam calculados sobre dados do próprio projeto, e não sobre exemplos genéricos.

---

# 32. Telemetria

A telemetria será tratada como uma entrada de dados independente.

```text
TelemetryController
        ↓
ReceiveTelemetry
        ↓
TelemetryService
        ↓
TelemetryRepository
```

---

# 33. Processamento Concorrente

O backend deverá utilizar processamento concorrente principalmente na ingestão de telemetria.

Exemplo conceitual:

```text
                 Telemetry Queue
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
       Thread 1     Thread 2     Thread 3
          │            │            │
          └────────────┼────────────┘
                       ▼
                Telemetry Service
```

O objetivo não é utilizar threads artificialmente, mas demonstrar uma necessidade real de processamento concorrente, conforme solicitado pela disciplina de SO.

---

# 34. Sincronização

Operações críticas deverão possuir controle de concorrência.

Exemplo:

Duas requisições simultâneas tentando reservar a mesma bolsa.

```text
Request A ──┐
            ├──> BloodBag
Request B ──┘
```

O sistema deverá garantir que uma mesma bolsa não seja alocada duas vezes.

### Regra

```text
AVAILABLE
   ↓
RESERVED
```

Essa transição deverá ser atômica.

---

# 35. Tratamento de Exceções

O backend deverá possuir tratamento global.

```text
presentation/
└── handler/
    ├── GlobalExceptionHandler
    ├── ApiError
    └── ExceptionMapper
```

### Resposta padrão

```json
{
  "timestamp": "2026-08-11T15:30:00",
  "status": 400,
  "error": "BUSINESS_RULE_ERROR",
  "message": "Quantidade de bolsas compatíveis insuficiente.",
  "path": "/api/v1/requests/123/process"
}
```

---

# 36. Exceções de Domínio

Exemplos:

```text
BloodBagExpiredException
IncompatibleBloodException
InsufficientInventoryException
RequestAlreadyProcessedException
InvalidRouteException
TemperatureOutOfRangeException
BloodBagAlreadyReservedException
```

As exceções deverão representar problemas reais do domínio.

---

# 37. Validação

A validação deverá ocorrer em níveis diferentes.

### Entrada da API

```text
@NotNull
@NotBlank
@Positive
@Future
```

### Domínio

Regras que não podem ser violadas independentemente da origem da informação.

Exemplo:

```text
Uma bolsa vencida nunca pode ser alocada.
```

Essa regra não deve depender do controller.

---

# 38. Persistência

### Banco

**PostgreSQL**

### Tecnologia

**Spring Data JPA**

Porém:

```text
Domain
  X
  NÃO conhece JPA
```

A entidade de domínio deverá ser independente da entidade de persistência quando isso contribuir para manter a separação arquitetural.

---

# 39. Estrutura de Persistência

```text
infrastructure/
└── persistence/
    ├── entity/
    │   ├── HospitalEntity
    │   ├── BloodBagEntity
    │   ├── RequestEntity
    │   ├── AllocationEntity
    │   ├── RouteEntity
    │   ├── TransportEntity
    │   └── TelemetryEntity
    │
    ├── repository/
    │   ├── SpringDataHospitalRepository
    │   ├── SpringDataBloodBagRepository
    │   └── ...
    │
    ├── mapper/
    │   ├── HospitalPersistenceMapper
    │   └── ...
    │
    └── adapter/
        ├── HospitalRepositoryAdapter
        ├── BloodBagRepositoryAdapter
        └── ...
```

---

# 40. Padrões de Projeto

O backend deverá utilizar padrões de forma **justificada**, e não apenas para cumprir checklist.

## Repository

Abstrair persistência.

```text
Use Case
   ↓
Repository Interface
   ↓
Repository Adapter
   ↓
JPA
```

---

## Strategy

Recomendado para regras que possam variar.

Exemplo:

```text
CompatibilityStrategy
        │
        ├── RedBloodCellCompatibility
        ├── PlasmaCompatibility
        └── PlateletCompatibility
```

Isso permitirá adicionar regras sem alterar o fluxo principal.

---

## Factory

Pode ser utilizada para criação de estruturas de domínio específicas.

Exemplo:

```text
BloodBagFactory
```

---

## Adapter

Utilizado para conectar a aplicação a:

* banco;
* telemetria;
* serviços externos;
* infraestrutura.

---

## Observer / Event

Pode ser utilizado para eventos internos.

Exemplo:

```text
BloodBagReservedEvent
        ↓
UpdateDashboard
        ↓
RecordStatistic
```

O uso deverá ser avaliado apenas se realmente simplificar o projeto.

---

# 41. SOLID

O backend deverá seguir os princípios SOLID.

## S — Single Responsibility

Uma classe deve possuir uma responsabilidade principal.

Evitar:

```text
RequestService
├── valida requisição
├── calcula Dijkstra
├── consulta banco
├── calcula estatística
├── envia telemetria
└── gera relatório
```

Preferir:

```text
RequestValidator
AllocationService
RouteService
StatisticsService
TelemetryService
```

---

## O — Open/Closed

Novas estratégias deverão poder ser adicionadas sem alterar o código existente.

Exemplo:

```text
CompatibilityStrategy
```

---

## L — Liskov Substitution

Implementações de uma mesma abstração devem poder substituir umas às outras.

---

## I — Interface Segregation

Interfaces pequenas e específicas.

Evitar:

```text
HemoviaService
```

com dezenas de métodos.

Preferir:

```text
HospitalRepository
BloodBagRepository
RequestRepository
RouteRepository
TelemetryRepository
```

---

## D — Dependency Inversion

A aplicação dependerá de abstrações.

```text
Use Case
   ↓
Interface
   ↑
Implementation
```

---

# 42. Testes

O backend deverá possuir testes em diferentes níveis.

## Testes unitários

Prioridade para:

* Compatibilidade;
* FEFO;
* Dijkstra;
* Hash;
* Regras de estoque;
* Validação;
* Probabilidade.

---

## Testes de integração

Validar:

* Controllers;
* Repositories;
* Banco;
* Casos de uso;
* APIs.

---

## Teste end-to-end

O principal cenário:

```text
Criar requisição
      ↓
Processar requisição
      ↓
Verificar compatibilidade
      ↓
Aplicar FEFO
      ↓
Calcular rota
      ↓
Criar transporte
      ↓
Receber telemetria
      ↓
Finalizar entrega
```

---

# 43. Cobertura de Testes

Os fluxos críticos deverão possuir alta cobertura.

Especialmente:

```text
✓ Compatibilidade
✓ FEFO
✓ Alocação
✓ Reserva
✓ Dijkstra
✓ Concorrência
✓ Telemetria
✓ Regras de validade
```

A rubrica de POO U2 exige cobertura de testes automatizados nos fluxos críticos.

---

# 44. Documentação da API

A API deverá ser documentada com **OpenAPI/Swagger**.

Cada endpoint deverá informar:

* Método;
* URL;
* Parâmetros;
* Request body;
* Response;
* Status codes;
* Erros possíveis.

Exemplo:

```text
POST /api/v1/requests/{id}/process
```

### 200

```json
{
  "requestId": "123",
  "status": "ALLOCATED",
  "allocatedBags": 4,
  "routeId": "456"
}
```

### 409

```json
{
  "error": "INSUFFICIENT_INVENTORY",
  "message": "Não existem bolsas compatíveis suficientes."
}
```

---

# 45. Versionamento da API

Todas as APIs públicas deverão utilizar versionamento:

```text
/api/v1/...
```

Exemplo:

```text
/api/v1/hospitals
/api/v1/blood-bags
/api/v1/requests
/api/v1/routes
```

Isso permite evoluir a API futuramente sem quebrar consumidores existentes.

---

# 46. Configuração

Configurações deverão ficar fora do código.

Exemplo:

```yaml
server:
  port: ${PORT:8080}

spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

Nunca armazenar:

```text
senha
token
chave privada
credenciais
```

diretamente no Git.

---

# 47. Migrações

O banco deverá ser versionado por migrations.

Estrutura:

```text
resources/
└── db/
    └── migration/
        ├── V1__create_hospitals.sql
        ├── V2__create_blood_bags.sql
        ├── V3__create_requests.sql
        ├── V4__create_allocations.sql
        ├── V5__create_routes.sql
        ├── V6__create_transports.sql
        └── V7__create_telemetry.sql
```

---

# 48. Seed de Dados Sintéticos

O sistema deverá possuir dados sintéticos para demonstração.

Exemplo:

```text
5 hospitais
100 bolsas
10 veículos
20 requisições
```

Os valores podem ser ajustados durante o desenvolvimento.

Nenhum dado real de paciente ou doador deverá ser utilizado.

---

# 49. Observabilidade

O backend deverá registrar:

* Requisições HTTP;
* Erros;
* Tempo de resposta;
* Processamento de requisições;
* Telemetria;
* Falhas de alocação;
* Falhas de comunicação.

Exemplo de log:

```text
INFO
Request #REQ-001 received

INFO
Checking compatibility

INFO
4 compatible bags found

INFO
FEFO allocation started

INFO
4 bags reserved

INFO
Calculating shortest route

INFO
Route calculated successfully
```

---

# 50. Health Check

O backend deverá possuir:

```http
GET /actuator/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

Isso facilitará o monitoramento e o deploy.

---

# 51. CI/CD

Pipeline mínimo:

```text
┌─────────────┐
│ Git Push    │
└──────┬──────┘
       ↓
┌─────────────┐
│ Build       │
└──────┬──────┘
       ↓
┌─────────────┐
│ Unit Tests  │
└──────┬──────┘
       ↓
┌─────────────┐
│ Integration │
│ Tests       │
└──────┬──────┘
       ↓
┌─────────────┐
│ Package     │
└──────┬──────┘
       ↓
┌─────────────┐
│ Deploy      │
└─────────────┘
```

O projeto prevê pipeline de CI/CD e deploy ainda na U1.

---

# 52. Docker

O backend deverá possuir:

```text
Dockerfile
docker-compose.yml
```

Ambiente local:

```text
┌─────────────────────┐
│ Backend              │
│ Spring Boot          │
│ Port 8080            │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ PostgreSQL           │
│ Port 5432            │
└─────────────────────┘
```

---

# 53. Fluxo completo do backend

```text
                         CLIENT
                           │
                           ▼
                    REST CONTROLLER
                           │
                           ▼
                      USE CASE
                           │
              ┌────────────┼─────────────┐
              ▼            ▼             ▼
       Compatibility     FEFO         Inventory
          Service       Service         Service
              │            │             │
              └────────────┼─────────────┘
                           ▼
                       Allocation
                           │
                           ▼
                      Route Service
                           │
                           ▼
                         Dijkstra
                           │
                           ▼
                        Transport
                           │
                           ▼
                       Telemetry
                           │
                           ▼
                     PostgreSQL
```

---

# 54. Fluxo crítico — Processamento de Requisição

```text
POST /api/v1/requests/{id}/process
                │
                ▼
        ProcessRequestUseCase
                │
                ▼
        Buscar requisição
                │
                ▼
        Validar requisição
                │
                ▼
        Consultar estoque
                │
                ▼
       Filtrar compatibilidade
                │
                ▼
             Aplicar FEFO
                │
                ▼
        Verificar quantidade
                │
                ▼
       Reservar bolsas
                │
                ▼
        Calcular Dijkstra
                │
                ▼
       Criar Allocation
                │
                ▼
        Criar Transport
                │
                ▼
       Atualizar Request
                │
                ▼
              RESPONSE
```

---

# 55. Regras de Ouro da Arquitetura

Para manter a qualidade do backend durante o desenvolvimento:

### Regra 01

> Controller não possui regra de negócio.

### Regra 02

> Repository não possui regra de negócio.

### Regra 03

> Domain não depende do Spring.

### Regra 04

> Algoritmos não ficam dentro dos Controllers.

### Regra 05

> Entidades JPA não são retornadas diretamente pela API.

### Regra 06

> Casos de uso representam ações do sistema.

### Regra 07

> Interfaces ficam voltadas para dentro da arquitetura.

### Regra 08

> Infraestrutura implementa as abstrações.

### Regra 09

> Toda regra crítica possui teste.

### Regra 10

> Toda funcionalidade nova deve respeitar o escopo fechado do MVP.

---

# 56. Definition of Done — Backend

Uma funcionalidade será considerada concluída quando:

* [ ] Regra de negócio implementada;
* [ ] Caso de uso implementado;
* [ ] Interface necessária definida;
* [ ] Adapter implementado;
* [ ] Controller implementado, quando necessário;
* [ ] DTOs criados;
* [ ] Validações implementadas;
* [ ] Tratamento de erros implementado;
* [ ] Testes unitários criados;
* [ ] Testes de integração criados quando necessários;
* [ ] Documentação da API atualizada;
* [ ] Código revisado;
* [ ] Nenhuma regra de negócio dentro do controller;
* [ ] Nenhuma dependência indevida entre camadas;
* [ ] Commit realizado.

---

# 57. Prioridade de Implementação

O backend deverá ser desenvolvido nesta ordem:

## Fase 01 — Fundação

* [ ] Criar projeto Spring Boot;
* [ ] Configurar Java;
* [ ] Configurar PostgreSQL;
* [ ] Configurar migrations;
* [ ] Configurar estrutura Clean Architecture;
* [ ] Configurar tratamento global de erros;
* [ ] Configurar testes.

## Fase 02 — Domínio

* [ ] Hospital;
* [ ] BloodBag;
* [ ] Inventory;
* [ ] Request;
* [ ] Allocation;
* [ ] Route;
* [ ] Transport;
* [ ] Telemetry.

## Fase 03 — CRUD

* [ ] Hospital;
* [ ] BloodBag;
* [ ] Inventory;
* [ ] Request.

## Fase 04 — Algoritmos

* [ ] Hash;
* [ ] FEFO;
* [ ] Compatibilidade ABO/Rh;
* [ ] Grafo;
* [ ] Dijkstra.

## Fase 05 — Integração

* [ ] ProcessRequest;
* [ ] Allocation;
* [ ] Route;
* [ ] Transport.

## Fase 06 — Telemetria

* [ ] Recepção;
* [ ] Persistência;
* [ ] Processamento concorrente;
* [ ] Alertas.

## Fase 07 — Estatística

* [ ] Estoque;
* [ ] Demanda;
* [ ] Tempo;
* [ ] Descarte;
* [ ] Desabastecimento.

## Fase 08 — Infraestrutura

* [ ] Docker;
* [ ] CI/CD;
* [ ] Deploy;
* [ ] Health Check;
* [ ] Logs;
* [ ] Monitoramento.

## Fase 09 — Qualidade

* [ ] Testes unitários;
* [ ] Testes de integração;
* [ ] Teste end-to-end;
* [ ] Documentação;
* [ ] Revisão arquitetural.

---

# 58. Resultado Esperado

Ao final do desenvolvimento, o backend deverá permitir que o seguinte fluxo seja executado por meio de APIs:

```text
🏥 Hospital
      │
      │ POST /requests
      ▼
📋 Requisição
      │
      │ POST /requests/{id}/process
      ▼
🧬 Compatibilidade ABO/Rh
      │
      ▼
⏳ FEFO
      │
      ▼
📦 Alocação das bolsas
      │
      ▼
🗺️ Dijkstra
      │
      ▼
🚚 Transporte
      │
      ▼
🌡️ Telemetria
      │
      ▼
📊 Indicadores
```

O resultado será um backend modular, testável e preparado para ser consumido pelo frontend, mantendo as responsabilidades de cada disciplina integradas ao mesmo produto.

---

# 59. Resumo Arquitetural

```text
HEMOVIA BACKEND
│
├── DOMAIN
│   ├── Entities
│   ├── Value Objects
│   ├── Business Rules
│   └── Algorithms
│
├── APPLICATION
│   ├── Use Cases
│   ├── DTOs
│   └── Ports
│
├── PRESENTATION
│   ├── REST Controllers
│   ├── Exception Handler
│   └── API Mappers
│
└── INFRASTRUCTURE
    ├── PostgreSQL
    ├── JPA
    ├── Telemetry
    ├── Configuration
    └── External Adapters
```

### Princípio central

> **O domínio define o que o HEMOVIA faz. A aplicação define como os casos de uso são orquestrados. A apresentação define como o sistema conversa com o mundo externo. A infraestrutura define como as dependências externas são implementadas.**
