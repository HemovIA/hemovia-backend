# Modelo persistente — finalizando entidades base

## Convenções e segurança

As entidades usam UUID como identidade, enums com `EnumType.STRING`, associações `LAZY` e `@Version` nos agregados mutáveis. Datas de calendário são `LocalDate`; eventos operacionais são `Instant` (PostgreSQL `timestamptz`). A API serializa DTOs, não entidades JPA, para evitar ciclos de associações e exposição de credenciais.

`User` continua sendo identidade de acesso e `Person` é o doador sintético: não são o mesmo cadastro. Senhas só existem no fluxo interno de autenticação codificadas; `UserResponseDTO` e `AuthResponseDTO` não têm esse campo.

## Entidades, campos e cardinalidades

| Entidade | Campos relevantes | Relações e regras |
|---|---|---|
| User | nome, e-mail, CPF, telefone, senha codificada | Pode ser ator conhecido de `StockMovement`; nunca é serializado como credencial. |
| Person | nome, e-mail, telefone | Um doador pode ter várias `Donation` e `BloodBag`; relações legadas desconhecidas podem permanecer nulas. |
| Hospital | nome, cidade, latitude, longitude, prioridade | Um hospital recebe várias `BloodRequest` e é destino de várias `DeliveryRoute`. Latitude -90..90, longitude -180..180 e prioridade 1..5. |
| BloodCenter | nome, cidade, latitude, longitude | Origem conhecida de doações/bolsas, atendimento de alocações e origem de rotas. Coordenadas têm os mesmos limites de Hospital. |
| Vehicle | código operacional, capacidade | Código único; capacidade é número positivo de bolsas. Um veículo pode executar vários transportes. |
| Donation | data de coleta, `collectionCenter` legado, doador/hemocentro opcionais | Uma doação origina zero ou várias bolsas. Não há remoção em cascata de pessoa ou bolsa. |
| BloodBag | ABO, Rh, componente, coleta, validade, estado, volume, doador/centro/doação opcionais | Cada bolsa aponta para no máximo uma doação. Volume é opcional, em mL, e quando informado é positivo; validade não antecede coleta. |
| BloodRequest | hospital, ABO/Rh, componente, quantidade, estado, createdAt/updatedAt | A quantidade é inteira positiva em unidades de bolsa; estado inicial `PENDING`. |
| Allocation | requisição, hemocentro, estado, instantes | Agrupa vários `AllocationItem`; conserva histórico e versão para concorrência. |
| AllocationItem | alocação, bolsa, estado de reserva, criado/liberado em | Uma bolsa tem no máximo uma reserva ativa (`RESERVED` ou `IN_TRANSIT`); `RELEASED` permanece no histórico e permite nova reserva. |
| DeliveryRoute | origem, destino, distância, duração, pontos | Origem é `BloodCenter`, destino é `Hospital`; pontos ordenados têm posição única por rota. |
| RouteWaypoint | latitude, longitude | Valor embutido, não entidade independente; a posição é mantida pela coleção ordenada da rota. |
| Transport | alocação, rota, veículo, estado, início/fim | Uma execução por alocação nesta versão; bolsas vêm de `AllocationItem`, não de lista duplicada. Término não antecede início. |
| TelemetryReading | transporte, sensor, medido/recebido em, temperatura | Temperatura é Celsius, obrigatória e finita; não há faixa clínica universal deduzida. |
| StockMovement | bolsa, estado anterior/novo, tipo, instante, ator, motivo | Histórico sem remoção em cascata; ator pode ser desconhecido. |

## Estados e unidades

| Tipo | Valores |
|---|---|
| RhFactor | `POSITIVE`, `NEGATIVE` |
| BloodType | `A`, `B`, `AB`, `O` |
| BloodComponent | `WHOLE_BLOOD`, `RED_BLOOD_CELLS`, `PLASMA`, `PLATELETS`, `CRYOPRECIPITATE` |
| BloodBagStatus | `AVAILABLE`, `RESERVED`, `IN_TRANSIT`, `DELIVERED`, `DISCARDED` |
| RequestStatus | `PENDING`, `ALLOCATED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED` |
| AllocationStatus | `ALLOCATED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED` |
| ReservationStatus | `RESERVED`, `IN_TRANSIT`, `DELIVERED`, `RELEASED` |
| TransportStatus | `PLANNED`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED` |
| MovementType | `ENTRY`, `RESERVATION`, `RELEASE`, `DISPATCH`, `DELIVERY`, `DISCARD` |

As medidas são: volume em mL, distância em km, duração estimada em segundos e temperatura em °C. CHECKs, FKs, `NOT NULL`, unicidades e o índice parcial de reserva ativa reforçam no banco as validações da aplicação. Valores legados de enumeração desconhecidos abortam a migração com diagnóstico; não são convertidos por ordinal ou aproximação.

## Compatibilidade legada

`Donation.collectionCenter` continua como texto legado enquanto `bloodCenter` só é preenchido quando a origem é conhecida. O contrato que criava uma bolsa por doação permanece: `bloodBag` representa exatamente uma bolsa, enquanto `bloodBags` sempre representa a coleção. Quando há várias bolsas, o campo singular é `null`; nenhuma bolsa é escolhida arbitrariamente. Não se inventam doadores, hemocentros ou vínculos para completar dados antigos.

As migrações V1–V3 permanecem imutáveis. V4–V7 fazem a evolução transacional, rejeitando código legado desconhecido ou uma bolsa vinculada ambiguamente a múltiplas doações antes de uma transformação destrutiva.
