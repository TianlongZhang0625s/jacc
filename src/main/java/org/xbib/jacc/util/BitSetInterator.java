package org.xbib.jacc.util;

class BitSetInterator implements Interator {

    private int[] set;
    private int pos;
    private int mask;
    private int num;
    private int bitCount;

    BitSetInterator(int[] ai, int i) {
        this.set = ai;
        this.num = i;
        this.pos = 0;
        this.mask = 1;
        this.bitCount = 0;
    }

    private void advance() {
        num++;
        if (++bitCount == 32) {
            pos++;
            bitCount = 0;
            mask = 1;
        } else {
            mask <<= 1;
        }
    }

    @Override
    public int next() {
        int i = num;
        advance();
        return i;
    }

    @Override
    public boolean hasNext() {
        while (pos < set.length && (set[pos] & mask) == 0) {
            advance();
        }
        return pos < set.length;
    }
}
