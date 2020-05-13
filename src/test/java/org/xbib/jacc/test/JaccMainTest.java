package org.xbib.jacc.test;

import org.junit.Test;
import org.xbib.jacc.Jacc;

public class JaccMainTest {

    @Test
    public void jaccMain() throws Exception {
        Jacc.main(new String[] {"-d", "build/", "src/test/resources/org/xbib/jacc/Calc.jacc"});
    }
}
