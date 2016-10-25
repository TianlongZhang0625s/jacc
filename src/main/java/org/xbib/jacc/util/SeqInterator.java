package org.xbib.jacc.util;

/**
 *
 */
class SeqInterator implements Interator {

    private int count;
    private int limit;

    SeqInterator(int i, int j) {
        count = i;
        limit = j;
    }

    @Override
    public int next() {
        return count++;
    }

    @Override
    public boolean hasNext() {
        return count < limit;
    }
}
