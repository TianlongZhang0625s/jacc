package org.xbib.jacc.grammar;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public final class Finitary extends Analysis {

    private final boolean[] finitaries;
    private final boolean[] consider;
    private final Grammar grammar;
    private final int numNTs;

    Finitary(Grammar grammar) {
        super(grammar.getComponents());
        this.grammar = grammar;
        numNTs = grammar.getNumNTs();
        finitaries = new boolean[numNTs];
        consider = new boolean[numNTs];
        for (int i = 0; i < numNTs; i++) {
            finitaries[i] = false;
            consider[i] = true;
        }
        bottomUp();
    }

    @Override
    protected boolean analyze(int i) {
        boolean flag = false;
        if (consider[i]) {
            int j = 0;
            Grammar.Prod[] prods = grammar.getProds(i);
            for (Grammar.Prod prod : prods) {
                int[] ai = prod.getRhs();
                int l;
                l = 0;
                while (l < ai.length && isAt(ai[l])) {
                    l++;
                }
                if (l >= ai.length) {
                    finitaries[i] = true;
                    consider[i] = false;
                    flag = true;
                    break;
                }
                if (!consider[ai[l]]) {
                    j++;
                }
            }
            if (j == prods.length) {
                consider[i] = false;
            }
        }
        return flag;
    }

    public boolean isAt(int i) {
        return grammar.isTerminal(i) || finitaries[i];
    }

    public void display(Writer writer) throws IOException {
        writer.write("Finitary = {");
        super.display(writer, numNTs, grammar);
        writer.write("}\n");
    }
}
