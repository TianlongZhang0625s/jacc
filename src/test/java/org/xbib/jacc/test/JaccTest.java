package org.xbib.jacc.test;

import org.junit.jupiter.api.Test;
import org.xbib.jacc.Jacc;
import org.xbib.jacc.MachineType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JaccTest {

    @Test
    public void calc() throws IOException {
        try (InputStream inputStream1 = getInputStream("Calc.jacc");
           InputStream inputStream2 = getInputStream("Calc.errs");
           InputStream inputStream3 = getInputStream("CalcParser.java");
           InputStream inputStream5 = getInputStream("CalcTokens.java");
           InputStream inputStream7 = getInputStream("CalcParser.output")) {
            Jacc jacc = new Jacc();
            jacc.setName("Calc");
            jacc.setInputStream(inputStream1);
            jacc.setErrorDiagnostics(new InputStreamReader(inputStream2, StandardCharsets.UTF_8));
            jacc.setDir("build/");
            jacc.setEnableVerboseMachineDescription(true);
            jacc.execute();
            try (InputStream inputStream4 = Files.newInputStream(Paths.get("build/CalcParser.java"));
                 InputStream inputStream6 = Files.newInputStream(Paths.get("build/CalcTokens.java"));
                 InputStream inputStream8 = Files.newInputStream(Paths.get("build/CalcParser.output"))) {
                StreamMatcher.assertStream("CalcParser.java",
                        inputStream3, inputStream4);
                StreamMatcher.assertStream("CalcTokens.java",
                        inputStream5, inputStream6);
                StreamMatcher.assertStream("CalcParser.output",
                        inputStream7, inputStream8);
            }
        }
    }

    @Test
    public void traceCalc() throws IOException {
        try (InputStream inputStream1 = getInputStream("Calc.jacc");
             InputStream inputStream2 = getInputStream("example1");
             InputStream inputStream3 = getInputStream("example1.out")) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Jacc jacc = new Jacc();
            jacc.setName("Calc");
            jacc.setInputStream(inputStream1);
            jacc.setParserInputs(new InputStreamReader(inputStream2, StandardCharsets.UTF_8));
            jacc.setEnableParserOutput(false);
            jacc.setEnableTokenOutput(false);
            jacc.setOutputStream(byteArrayOutputStream);
            jacc.setDir("build/");
            jacc.execute();
            StreamMatcher.assertStream("example1.out",
                    inputStream3,
                    new ByteArrayInputStream(byteArrayOutputStream.toByteArray())
            );
        }
    }

    @Test
    public void simpleCalc() throws IOException {
        try (InputStream inputStream1 = getInputStream("simpleCalc.jacc");
             InputStream inputStream2 = getInputStream("Calc.java")) {
            Jacc jacc = new Jacc();
            jacc.setName("simpleCalc");
            jacc.setInputStream(inputStream1);
            jacc.setMachineType(MachineType.SLR1);
            jacc.setDir("build/");
            jacc.execute();
            StreamMatcher.assertStream("Calc.java",
                    inputStream2,
                    Files.newInputStream(Paths.get("build/Calc.java")));
        }
    }

    private static InputStream getInputStream(String resource) throws IOException {
        return JaccTest.class.getClassLoader().getResource("org/xbib/jacc/" + resource).openStream();
    }

}
