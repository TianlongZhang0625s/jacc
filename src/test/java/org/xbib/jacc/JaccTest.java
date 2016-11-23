package org.xbib.jacc;

import org.junit.Test;
import org.xbib.jacc.helper.StreamMatcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 */
public class JaccTest {

    @Test
    public void calc() throws IOException {
        Jacc jacc = new Jacc();
        jacc.setName("Calc");
        jacc.setInputStream(getResource("Calc.jacc"));
        jacc.setErrorDiagnostics(getResource("Calc.errs"));
        jacc.setDir("build/");
        jacc.setEnableVerboseMachineDescription(true);
        jacc.execute();
        StreamMatcher.assertStream("CalcParser.java",
                getResource("CalcParser.java"),
                Files.newInputStream(Paths.get("build/CalcParser.java")));
        StreamMatcher.assertStream("CalcTokens.java",
                getResource("CalcTokens.java"),
                Files.newInputStream(Paths.get("build/CalcTokens.java")));
        StreamMatcher.assertStream("CalcParser.output",
                getResource("CalcParser.output"),
                Files.newInputStream(Paths.get("build/CalcParser.output")));
    }

    @Test
    public void traceCalc() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Jacc jacc = new Jacc();
        jacc.setName("Calc");
        jacc.setInputStream(getResource("Calc.jacc"));
        jacc.setParserInputs(getResource("example1"));
        jacc.setEnableParserOutput(false);
        jacc.setEnableTokenOutput(false);
        jacc.setOutputStream(byteArrayOutputStream);
        jacc.setDir("build/");
        jacc.execute();
        StreamMatcher.assertStream("example1.out",
                getResource("example1.out"),
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray())
                );
    }

    @Test
    public void simpleCalc() throws IOException {
        Jacc jacc = new Jacc();
        jacc.setName("simpleCalc");
        jacc.setInputStream(getResource("simpleCalc.jacc"));
        jacc.setMachineType(MachineType.SLR1);
        jacc.setDir("build/");
        jacc.execute();
        StreamMatcher.assertStream("Calc.java",
                getResource("Calc.java"),
                Files.newInputStream(Paths.get("build/Calc.java")));
    }

    private InputStream getResource(String resource) throws IOException {
        return getClass().getClassLoader().getResource("org/xbib/jacc/" + resource).openStream();
    }

}
