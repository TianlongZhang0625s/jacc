package org.xbib.jacc;

abstract class JaccSymbols {

    Node root;

    int size;

    JaccSymbols() {
        root = null;
        size = 0;
    }

    private static int fill(JaccSymbol[] ajaccsymbol, int pos, Node node) {
        int i = pos;
        if (node != null) {
            i = fill(ajaccsymbol, i, node.left);
            ajaccsymbol[i++] = node.data;
            i = fill(ajaccsymbol, i, node.right);
        }
        return i;
    }

    int getSize() {
        return size;
    }

    int fill(JaccSymbol[] ajaccsymbol, int i) {
        return fill(ajaccsymbol, i, root);
    }

    /**
     *
     */
    static class Node {

        Node left;

        JaccSymbol data;

        Node right;

        Node(JaccSymbol data) {
            this.data = data;
        }
    }
}
