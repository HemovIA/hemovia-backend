# Projeto Integrador HemovIA — AED Unidade 1

Implementação didática das estruturas básicas ligadas ao domínio HemovIA. O backlog delimita a U1 a uma lista encadeada de bolsas, uma fila FIFO de requisições hospitalares e uma pilha LIFO de eventos para undo. Os arquivos C mostram alocação/liberação manual; as classes Java são estruturas próprias que podem ser instanciadas por serviços Spring Boot. Elas não substituem o repositório JPA: são estruturas em memória para o exercício e para consumo direto por código Java.

## Escopo

- **Estoque:** lista simplesmente encadeada; inserção no fim, busca sequencial por ID e remoção por ID.
- **Requisições:** fila encadeada com referências ao início e fim; enqueue no fim e dequeue no início (FIFO).
- **Histórico:** pilha encadeada; push/pop no topo (LIFO), com estado anterior e posterior da bolsa para apoiar undo.
- **Recursão e árvore:** exemplo complementar de ABB com inserção, busca e percurso inorder recursivos. Não é uma quarta estrutura de domínio pedida para AED U1 nem participa das regras de estoque.

Não há roteirização, FEFO, hash ou compatibilidade ABO/Rh.

## Correspondência entre C e Java — baseada neste código

**Lista de estoque.** Em `c/hemovia_aed_u1.c`, `StockList` aponta para `BagNode *head`, e cada `BagNode` possui uma `BloodBag value` e um `next`. `stock_add` percorre os ponteiros até o último nó, `stock_find` percorre sequencialmente comparando `id`, e `stock_remove` religa o anterior ao próximo antes de liberar o nó. Em Java, `BloodBagStockList` reproduz os mesmos elos por `Node.next`, mantém `head`, percorre a cadeia em `add`/`findById`/`removeById` e atualiza os mesmos casos de remoção (início ou meio/fim). Assim, a busca segue O(n) nas duas versões; o Java gerencia a memória dos objetos, enquanto a versão C libera o nó removido com `free`.

**Fila de requisições.** No C, `RequestQueue` guarda `front` e `rear`: `queue_enqueue` conecta o novo nó após `rear`, e `queue_dequeue` copia e remove `front`, atualizando `rear` quando a fila fica vazia. `HospitalRequestQueue` faz os mesmos passos com `front`, `rear` e `Node.next`; `enqueue` acrescenta no fim e `dequeue` devolve o primeiro. A ordem observável é, portanto, FIFO em ambas. Fila vazia é indicada por retorno falso no C e por `EmptyQueueException` em Java, sem dereferenciar ponteiro/referência nula.

**Pilha de histórico.** `HistoryStack.top` no C e `StockHistoryStack.top` em Java representam o mesmo topo. `history_push`/`push` liga o nó novo antes do topo anterior; `history_pop`/`pop` salva o evento, avança o topo e remove o nó. O registro contém `bag_id`, `previous_status` e `resulting_status` no C e `bagId`, `previousStatus` e `resultingStatus` no `StockOperation`. `history_undo` e `StockHistoryStack.undo` consultam o evento do topo, localizam a bolsa, só então removem o evento e restauram `previous_status` na própria bolsa. Assim, undo reproduz a semântica do cenário do backlog. Pilha vazia retorna falso em C e lança `EmptyStackException` em Java; bolsa não encontrada mantém o evento no topo.

**ABB e recursão complementar.** `tree_insert` e `tree_inorder` no C e os métodos privados recursivos `insert`, `contains` e `inOrder` em `BinarySearchTree` aplicam a mesma comparação de chave: menores à esquerda, maiores à direita. O inorder visita esquerda, nó, direita e produz chaves ordenadas. Este exemplo cobre árvore binária de busca e recursão solicitadas como conteúdo, sem indexar o estoque (hash/índices ficam fora da U1).

## Integração Java

As classes em `api/src/main/java/com/hemovia/api/structures/basic` não dependem de `List`, `Queue`, `Deque` ou `Stack` da biblioteca para implementar os elos. Um serviço pode compor `BloodBagStockList`, `HospitalRequestQueue` e `StockHistoryStack` como campos. `BloodBagStockList` armazena objetos `BloodBag` existentes no domínio; a fila e a pilha usam registros pequenos de domínio da própria U1. O estado de undo é descrito pelo evento e sua aplicação fica a cargo do serviço, que deve localizar a bolsa e restaurar `previousStatus` conforme o fluxo de domínio.

## Compilar e executar o exemplo C

Com GCC/Clang instalado, na raiz do repositório:

```sh
gcc -std=c11 -Wall -Wextra -pedantic aed-u1/c/hemovia_aed_u1.c -o hemovia-aed-u1
./hemovia-aed-u1
```

O programa demonstra busca no estoque, remoção FIFO, undo e percurso recursivo inorder; os destruidores liberam todos os nós restantes. A implementação Java usa Java 21 já configurado no Maven do backend.
