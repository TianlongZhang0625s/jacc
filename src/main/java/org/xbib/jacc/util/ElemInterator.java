package org.xbib.jacc.util;

/**
 *
 */
class ElemInterator implements Interator {

    private int count;
    private int limit;
    private int[] a;

    ElemInterator(int[] ai, int i, int j) {
        a = ai;
        count = i;
        limit = j;
    }

    ElemInterator(int[] ai) {
        this(ai, 0, ai.length);
    }

    @Override
    public int next() {
        return a[count++];
    }

    @Override
    public boolean hasNext() {
        return count < limit;
    }
}
