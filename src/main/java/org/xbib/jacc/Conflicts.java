package org.xbib.jacc;

import org.xbib.jacc.grammar.Grammar;
import org.xbib.jacc.grammar.Machine;

/**
 *
 */
class Conflicts {

    private final int type;
    private final int arg1;
    private final int arg2;
    private final Grammar.Symbol sym;
    private Conflicts next;

    private Conflicts(int i, int j, int k, Grammar.Symbol symbol, Conflicts conflicts) {
        this.type = i;
        this.arg1 = j;
        this.arg2 = k;
        this.sym = symbol;
        this.next = conflicts;
    }

    static Conflicts sr(int i, int j, Grammar.Symbol symbol, Conflicts conflicts) {
        return append(conflicts, new Conflicts(0, i, j, symbol, null));
    }

    static Conflicts rr(int i, int j, Grammar.Symbol symbol, Conflicts conflicts) {
        return append(conflicts, new Conflicts(1, i, j, symbol, null));
    }

    private static Conflicts append(Conflicts conflicts, Conflicts conflicts1) {
        if (conflicts == null) {
            return conflicts1;
        }
        Conflicts conflicts2;
        conflicts2 = conflicts;
        while (conflicts2.next != null) {
            conflicts2 = conflicts2.next;
        }
        conflicts2.next = conflicts1;
        return conflicts;
    }

    static String describe(Machine machine, int i, Conflicts c) {
        Conflicts conflicts = c;
        if (conflicts == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String s = System.getProperty("line.separator", "\n");
        for (; conflicts != null; conflicts = conflicts.next) {
            sb.append(i);
            sb.append(": ");
            if (conflicts.type == 0) {
                sb.append("shift/reduce conflict (");
                if (conflicts.arg1 < 0) {
                    sb.append("$end");
                } else {
                    sb.append("shift ");
                    sb.append(conflicts.arg1);
                }
                sb.append(" and red'n ");
                sb.append(machine.reduceItem(i, conflicts.arg2).getSeqNo());
            } else {
                sb.append("reduce/reduce conflict (red'ns ");
                sb.append(machine.reduceItem(i, conflicts.arg1).getSeqNo());
                sb.append(" and ");
                sb.append(machine.reduceItem(i, conflicts.arg2).getSeqNo());
            }
            sb.append(") on ");
            sb.append(conflicts.sym.getName());
            sb.append(s);
        }
        return sb.toString();
    }
}
