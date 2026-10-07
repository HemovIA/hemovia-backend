package com.hemovia.api.structures.basic;

/** ABB complementar para demonstrar árvore binária e recursão. */
public final class BinarySearchTree {
    private Node root;
    private static final class Node {
        final int key;
        Node left, right;
        Node(int key) { this.key = key; }
    }

    public void insert(int key) { root = insert(root, key); }
    private Node insert(Node node, int key) {
        if (node == null) return new Node(key);
        if (key < node.key) node.left = insert(node.left, key);
        else if (key > node.key) node.right = insert(node.right, key);
        return node;
    }

    public boolean contains(int key) { return contains(root, key); }
    private boolean contains(Node node, int key) {
        if (node == null) return false;
        if (key == node.key) return true;
        return contains(key < node.key ? node.left : node.right, key);
    }

    public String inOrder() {
        StringBuilder result = new StringBuilder();
        inOrder(root, result);
        return result.toString().trim();
    }
    private void inOrder(Node node, StringBuilder result) {
        if (node == null) return;
        inOrder(node.left, result);
        if (!result.isEmpty()) result.append(' ');
        result.append(node.key);
        inOrder(node.right, result);
    }
}
