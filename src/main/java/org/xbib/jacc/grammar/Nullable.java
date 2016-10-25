package org.xbib.jacc.grammar;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class Nullable extends Analysis {

    private final boolean[] nullables;
    private final boolean[] consider;
    private final Grammar grammar;
    private final int numNTs;

    Nullable(Grammar grammar) {
        super(grammar.getComponents());
        this.grammar = grammar;
        numNTs = grammar.getNumNTs();
        nullables = new boolean[numNTs];
        consider = new boolean[numNTs];
        for (int i = 0; i < numNTs; i++) {
            nullables[i] = false;
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
                    nullables[i] = true;
                    consider[i] = false;
                    flag = true;
                    break;
                }
                if (grammar.isTerminal(ai[l]) || grammar.isNonterminal(ai[l]) && !consider[ai[l]]) {
                    j++;
                }
            }
            if (j == prods.length) {
                consider[i] = false;
            }
        }
        return flag;
    }

    @Override
    public boolean isAt(int i) {
        return grammar.isNonterminal(i) && nullables[i];
    }

    public void display(Writer writer) throws IOException {
        writer.write("Nullable = {");
        super.display(writer, numNTs, grammar);
        writer.write("}\n");
    }
}
