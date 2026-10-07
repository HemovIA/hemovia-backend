#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Demonstração de estruturas encadeadas da Unidade 1. Memória dos nós
   reservada com malloc e liberada explicitamente com free. */
typedef struct { char id[32]; char status[24]; } BloodBag;
typedef struct BagNode { BloodBag value; struct BagNode *next; } BagNode;
typedef struct { BagNode *head; size_t size; } StockList;

typedef struct { char id[32]; char hospital[64]; } Request;
typedef struct RequestNode { Request value; struct RequestNode *next; } RequestNode;
typedef struct { RequestNode *front, *rear; size_t size; } RequestQueue;

typedef struct { char bag_id[32], previous_status[24], resulting_status[24]; } Operation;
typedef struct OperationNode { Operation value; struct OperationNode *next; } OperationNode;
typedef struct { OperationNode *top; size_t size; } HistoryStack;

static void copy_text(char *dst, size_t capacity, const char *src) {
    snprintf(dst, capacity, "%s", src);
}

static int stock_add(StockList *list, const BloodBag *bag) {
    BagNode *node = malloc(sizeof *node);
    if (!node) return 0;
    node->value = *bag; node->next = NULL;
    if (!list->head) list->head = node;
    else { BagNode *it = list->head; while (it->next) it = it->next; it->next = node; }
    list->size++; return 1;
}

static BloodBag *stock_find(StockList *list, const char *id) {
    for (BagNode *it = list->head; it; it = it->next)
        if (strcmp(it->value.id, id) == 0) return &it->value;
    return NULL;
}

static int stock_remove(StockList *list, const char *id) {
    BagNode *previous = NULL, *current = list->head;
    while (current) {
        if (strcmp(current->value.id, id) == 0) {
            if (previous) previous->next = current->next; else list->head = current->next;
            free(current); list->size--; return 1;
        }
        previous = current; current = current->next;
    }
    return 0;
}

static int queue_enqueue(RequestQueue *queue, const Request *request) {
    RequestNode *node = malloc(sizeof *node);
    if (!node) return 0;
    node->value = *request; node->next = NULL;
    if (queue->rear) queue->rear->next = node; else queue->front = node;
    queue->rear = node; queue->size++; return 1;
}

static int queue_dequeue(RequestQueue *queue, Request *out) {
    if (!queue->front) return 0;
    RequestNode *old = queue->front; *out = old->value;
    queue->front = old->next; if (!queue->front) queue->rear = NULL;
    free(old); queue->size--; return 1;
}

static int history_push(HistoryStack *stack, const Operation *operation) {
    OperationNode *node = malloc(sizeof *node);
    if (!node) return 0;
    node->value = *operation; node->next = stack->top;
    stack->top = node; stack->size++; return 1;
}

static int history_pop(HistoryStack *stack, Operation *out) {
    if (!stack->top) return 0;
    OperationNode *old = stack->top; *out = old->value;
    stack->top = old->next; free(old); stack->size--; return 1;
}

static int history_undo(HistoryStack *stack, StockList *stock) {
    if (!stack->top) return 0;
    BloodBag *bag = stock_find(stock, stack->top->value.bag_id);
    if (!bag) return 0;
    Operation operation;
    if (!history_pop(stack, &operation)) return 0;
    copy_text(bag->status, sizeof bag->status, operation.previous_status);
    return 1;
}

static void stock_destroy(StockList *list) {
    while (list->head) { BagNode *old = list->head; list->head = old->next; free(old); }
    list->size = 0;
}
static void queue_destroy(RequestQueue *queue) {
    while (queue->front) { RequestNode *old = queue->front; queue->front = old->next; free(old); }
    queue->rear = NULL; queue->size = 0;
}
static void history_destroy(HistoryStack *stack) {
    while (stack->top) { OperationNode *old = stack->top; stack->top = old->next; free(old); }
    stack->size = 0;
}

/* ABB complementar: inserção e percurso inorder recursivos. Não participa
   do estoque/fila/pilha do backlog AED U1. */
typedef struct TreeNode { int key; struct TreeNode *left, *right; } TreeNode;
static TreeNode *tree_insert(TreeNode *root, int key) {
    if (!root) { TreeNode *n = malloc(sizeof *n); if (!n) return NULL; n->key=key; n->left=n->right=NULL; return n; }
    if (key < root->key) root->left = tree_insert(root->left, key);
    else if (key > root->key) root->right = tree_insert(root->right, key);
    return root;
}
static void tree_inorder(const TreeNode *root) {
    if (!root) return;
    tree_inorder(root->left); printf("%d ", root->key); tree_inorder(root->right);
}
static void tree_destroy(TreeNode *root) {
    if (!root) return;
    tree_destroy(root->left);
    tree_destroy(root->right);
    free(root);
}

int main(void) {
    StockList stock = {0}; RequestQueue queue = {0}; HistoryStack history = {0};
    BloodBag bag; copy_text(bag.id,sizeof bag.id,"BAG-999"); copy_text(bag.status,sizeof bag.status,"AVAILABLE");
    if (!stock_add(&stock,&bag)) return EXIT_FAILURE;
    printf("Busca estoque BAG-999: %s\n", stock_find(&stock,"BAG-999") ? "encontrada" : "ausente");
    printf("Remoção BAG-000 (inexistente): %s\n", stock_remove(&stock,"BAG-000") ? "removida" : "não encontrada");

    Request a,b,out; copy_text(a.id,sizeof a.id,"REQ-101"); copy_text(a.hospital,sizeof a.hospital,"Hospital A");
    copy_text(b.id,sizeof b.id,"REQ-102"); copy_text(b.hospital,sizeof b.hospital,"Hospital B");
    if (!queue_enqueue(&queue,&a) || !queue_enqueue(&queue,&b)) return EXIT_FAILURE;
    if (queue_dequeue(&queue,&out)) printf("FIFO: %s\n",out.id);

    Operation op; copy_text(op.bag_id,sizeof op.bag_id,"BAG-999");
    copy_text(op.previous_status,sizeof op.previous_status,"AVAILABLE");
    copy_text(op.resulting_status,sizeof op.resulting_status,"RESERVED");
    if (!history_push(&history,&op)) return EXIT_FAILURE;
    copy_text(bag.status,sizeof bag.status,"RESERVED");
    if (history_undo(&history,&stock)) printf("Undo: %s volta para %s\n",bag.id,bag.status);

    TreeNode *tree=NULL; int keys[]={8,3,10,1,6};
    for (size_t i=0;i<sizeof keys/sizeof keys[0];i++) tree=tree_insert(tree,keys[i]);
    printf("ABB inorder (recursão): "); tree_inorder(tree); putchar('\n');
    tree_destroy(tree); stock_destroy(&stock); queue_destroy(&queue); history_destroy(&history);
    return EXIT_SUCCESS;
}
