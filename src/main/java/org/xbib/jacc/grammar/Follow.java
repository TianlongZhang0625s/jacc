package org.xbib.jacc.grammar;

import org.xbib.jacc.util.BitSet;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class Follow extends Analysis {

    private Grammar grammar;
    private Nullable nullable;
    private First first;
    private int numNTs;
    private int[][] follows;

    Follow(Grammar grammar, Nullable nullable, First first1) {
        super(grammar.getComponents());
        this.grammar = grammar;
        this.nullable = nullable;
        first = first1;
        numNTs = grammar.getNumNTs();
        int numTs = grammar.getNumTs();
        follows = new int[numNTs][];
        for (int i = 0; i < numNTs; i++) {
            follows[i] = BitSet.make(numTs);
        }
        BitSet.set(follows[0], numTs - 1);
        topDown();
    }

    @Override
    public boolean isAt(int i) {
        return false;
    }

    @Override
    protected boolean analyze(int i) {
        boolean flag = false;
        Grammar.Prod[] aprod = grammar.getProds(i);
        for (Grammar.Prod anAprod : aprod) {
            int[] ai = anAprod.getRhs();
            for (int k = 0; k < ai.length; k++) {
                if (!grammar.isNonterminal(ai[k])) {
                    continue;
                }
                int l = k + 1;
                do {
                    if (l >= ai.length) {
                        break;
                    }
                    if (grammar.isTerminal(ai[l])) {
                        if (BitSet.addTo(follows[ai[k]], ai[l] - numNTs)) {
                            flag = true;
                        }
                        break;
                    }
                    if (BitSet.addTo(follows[ai[k]], first.at(ai[l]))) {
                        flag = true;
                    }
                    if (!nullable.isAt(ai[l])) {
                        break;
                    }
                    l++;
                } while (true);
                if (l >= ai.length && BitSet.addTo(follows[ai[k]], follows[i])) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    public int[] at(int i) {
        return follows[i];
    }

    public void display(Writer writer) throws IOException {
        writer.write("Follow sets:\n");
        for (int i = 0; i < follows.length; i++) {
            writer.write(" Follow(" + grammar.getSymbol(i) + "): {");
            writer.write(grammar.displaySymbolSet(at(i), numNTs));
            writer.write("}\n");
        }

    }
}
