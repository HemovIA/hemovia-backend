# language: pt
@POOUS06 @AC-031
Funcionalidade: Alocar bolsas de hemácias para uma requisição

  Cenário: Manter o pedido pendente quando faltam bolsas elegíveis
    Dado que uma operadora do hemocentro está autenticada
    E há uma requisição pendente de 5 bolsas de hemácias AB negativo
    E o hemocentro tem apenas 2 bolsas elegíveis para essa requisição
    Quando a operadora solicita a alocação com CSRF válido
    Então a API responde 409 com o código "INSUFFICIENT_INVENTORY"
    E a requisição continua em "PENDING"
    E as 2 bolsas continuam em "AVAILABLE"
    E nenhuma alocação ou reserva é gravada para a requisição
