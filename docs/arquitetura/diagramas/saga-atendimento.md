# Saga de atendimento

Projeção em que Requisições orquestra o atendimento. Os comandos e eventos atravessam a fila e carregam ID de saga, tentativa e mensagem.

```mermaid
sequenceDiagram
    actor H as Hospital
    participant R as Requisições
    participant B as Estoque
    participant E as Expedição
    H->>R: Criar requisição
    R->>B: ReservarBolsas
    alt estoque insuficiente
        B-->>R: ReservaRecusada
        Note over R: Pedido volta a aguardar atendimento
    else reserva integral
        B-->>R: ReservaConfirmada
        R->>E: PrepararCarga
        alt rota e carga preparadas
            E-->>R: CargaPreparada
            Note over R: Pedido ALLOCATED
            R->>E: AutorizarTransporte
        else falha ou timeout
            R->>E: CancelarPreparacao
            E-->>R: PreparacaoInvalidada
            R->>B: LiberarReserva
            B-->>R: ReservaLiberada
            Note over R: Pedido PENDING
        end
    end
```

Expedição invalida a tentativa de forma idempotente mesmo se o comando de cancelamento chegar antes do de preparação. O transporte permanece bloqueado até autorização. Se a compensação falhar, Requisições conserva o estado pendente de compensação e permite retomada após investigação.
