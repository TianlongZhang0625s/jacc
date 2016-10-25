package org.xbib.jacc.util;

/**
 *
 */
public class SCC {

    public static int[][] get(int[][] ai, int[][] ai1, int i) {
        return new GetComponents(ai, i, new ArrangeByFinish(ai1, i).getFinishOrder()).getComponents();
    }

    public static int[][] get(int[][] ai) {
        return get(ai, invert(ai), ai.length);
    }

    private static int[][] invert(int[][] ai) {
        return invert(ai, ai.length);
    }

    public static int[][] invert(int[][] ai, int i) {
        int[] ai1 = new int[i];
        for (int j = 0; j < i; j++) {
            for (int k = 0; k < ai[j].length; k++) {
                ai1[ai[j][k]]++;
            }
        }
        int[][] ai2 = new int[i][];
        for (int l = 0; l < i; l++) {
            ai2[l] = new int[ai1[l]];
        }
        for (int i1 = 0; i1 < i; i1++) {
            for (int j1 = 0; j1 < ai[i1].length; j1++) {
                int k1 = ai[i1][j1];
                ai1[k1]--;
                ai2[k1][ai1[k1]] = i1;
            }
        }
        return ai2;
    }

    private static class GetComponents extends DepthFirst {

        private int numComps;
        private int[] compNo;

        GetComponents(int[][] ai, int i, int[] ai1) {
            super(new ElemInterator(ai1), ai);
            numComps = 0;
            compNo = new int[i];
        }

        @Override
        void doneVisit(int i) {
            compNo[i] = numComps;
        }

        @Override
        void doneTree() {
            numComps++;
        }

        int[][] getComponents() {
            search();
            int[] ai = new int[numComps];
            for (int aCompNo : compNo) {
                ai[aCompNo]++;
            }
            int ai1[][] = new int[numComps][];
            for (int j = 0; j < numComps; j++) {
                ai1[j] = new int[ai[j]];
            }
            for (int k = 0; k < compNo.length; k++) {
                int l = compNo[k];
                ai1[l][--ai[l]] = k;
            }
            return ai1;
        }
    }

    private static class ArrangeByFinish extends DepthFirst {

        private int dfsNum;
        private int[] order;

        ArrangeByFinish(int[][] ai, int i) {
            super(new SeqInterator(0, i), ai);
            dfsNum = i;
            order = new int[dfsNum];
        }

        @Override
        void doneVisit(int i) {
            order[--dfsNum] = i;
        }

        @Override
        void doneTree() {
            // do nothing
        }

        int[] getFinishOrder() {
            search();
            return order;
        }
    }

    abstract static class DepthFirst {

        private Interator seq;
        private int[][] adjs;
        private int[] visited;

        DepthFirst(Interator interator, int[][] ai) {
            seq = interator;
            adjs = ai;
            visited = BitSet.make(ai.length);
        }

        void search() {
            do {
                if (!seq.hasNext()) {
                    break;
                }
                if (visit(seq.next())) {
                    doneTree();
                }
            } while (true);
        }

        private boolean visit(int i) {
            if (BitSet.addTo(visited, i)) {
                int[] ai = adjs[i];
                for (int j : ai) {
                    visit(j);
                }
                doneVisit(i);
                return true;
            } else {
                return false;
            }
        }

        abstract void doneVisit(int i);

        abstract void doneTree();
    }

}
