package org.xbib.jacc.grammar;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
abstract class Analysis {

    private int[][] comps;

    Analysis(int[][] ai) {
        comps = ai;
    }

    void bottomUp() {
        for (int[] comp : comps) {
            analyzeComponent(comp);
        }
    }

    void topDown() {
        for (int i = comps.length; i-- > 0; ) {
            analyzeComponent(comps[i]);
        }
    }

    private void analyzeComponent(int[] ai) {
        for (boolean flag = true; flag; ) {
            flag = false;
            int i = 0;
            while (i < ai.length) {
                flag |= analyze(ai[i]);
                i++;
            }
        }
    }

    public void display(Writer writer, int numNTs, Grammar grammar) throws IOException {
        int i = 0;
        for (int j = 0; j < numNTs; j++) {
            if (!isAt(j)) {
                continue;
            }
            if (i > 0) {
                writer.write(", ");
            }
            writer.write(grammar.getSymbol(j).getName());
            i++;
        }
    }

    public abstract boolean isAt(int i);

    protected abstract boolean analyze(int i);
}
