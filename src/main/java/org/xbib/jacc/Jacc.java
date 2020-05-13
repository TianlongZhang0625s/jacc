package org.xbib.jacc;

import org.xbib.jacc.compiler.ConsoleHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Jacc {

    private static final Logger logger = Logger.getLogger(Jacc.class.getName());

    private final String suffix;

    private final JaccSettings jaccSettings;

    private String className;

    private InputStream inputStream;

    private Reader errorDiagnostics;

    private Reader parserInputs;

    private OutputStream outputStream;

    private boolean enableParserOutput;

    private boolean enableTokenOutput;

    private boolean enableVerboseMachineDescription;

    private boolean includeCalculations;

    private boolean includeStateNumbers;

    private String dir;

    public Jacc() {
        this.className = null;
        this.inputStream = null;
        this.suffix = ".jacc";
        this.jaccSettings = new JaccSettings();
        this.enableParserOutput = true;
        this.enableTokenOutput = true;
        this.enableVerboseMachineDescription = false;
        this.includeCalculations = false;
        this.errorDiagnostics = null;
        this.parserInputs = null;
        this.includeStateNumbers = false;
        this.dir = null;
        this.outputStream = System.out;
    }

    public void setIncludeCalculations(boolean includeCalculations) {
        this.includeCalculations = includeCalculations;
    }

    public void setEnableParserOutput(boolean enableParserOutput) {
        this.enableParserOutput = enableParserOutput;
    }

    public void setEnableTokenOutput(boolean enableTokenOutput) {
        this.enableTokenOutput = enableTokenOutput;
    }

    public void setEnableVerboseMachineDescription(boolean enableVerboseMachineDescription) {
        this.enableVerboseMachineDescription = enableVerboseMachineDescription;
    }

    public void setMachineType(MachineType machineType) {
        jaccSettings.setMachineType(machineType);
    }

    public void setIncludeStateNumbers(boolean includeStateNumbers) {
        this.includeStateNumbers = includeStateNumbers;
    }

    public void setName(String name) {
        this.className = name;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setErrorDiagnostics(Reader errorDiagnostics) {
        this.errorDiagnostics = errorDiagnostics;
    }

    public Reader getErrorDiagnostics() {
        return errorDiagnostics;
    }

    public void setParserInputs(Reader parserInputs) {
        this.parserInputs = parserInputs;
    }

    public Reader getParserInputs() {
        return parserInputs;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public static void main(String[] args) throws Exception {
        Jacc jacc = new Jacc();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                if (arg.length() == 1) {
                    usage("Missing command line options");
                }
                int j = 1;
                while (j < arg.length()) {
                    switch (arg.charAt(j)) {
                        case 'f':
                            jacc.setIncludeCalculations(true);
                            break;
                        case 'p':
                            jacc.setEnableParserOutput(false);
                            break;
                        case 't':
                            jacc.setEnableTokenOutput(false);
                            break;
                        case 'v':
                            jacc.setEnableVerboseMachineDescription(true);
                            break;
                        case '0':
                            jacc.setMachineType(MachineType.LR0);
                            break;
                        case 's':
                            jacc.setMachineType(MachineType.SLR1);
                            break;
                        case 'a':
                            jacc.setMachineType(MachineType.LALR1);
                            break;
                        case 'n':
                            jacc.setIncludeStateNumbers(true);
                            break;
                        case 'e':
                            if (i + 1 >= args.length) {
                                usage("Missing filename for -e option");
                            }
                            jacc.setErrorDiagnostics(Files.newBufferedReader(Paths.get(args[++i])));
                            break;
                        case 'r':
                            if (i + 1 >= args.length) {
                                usage("Missing filename for -r option");
                            }
                            jacc.setParserInputs(Files.newBufferedReader(Paths.get(args[++i])));
                            break;
                        case 'd':
                            if (i + 1 >= args.length) {
                                usage("Missing directory for -d option");
                            }
                            jacc.setDir(args[++i]);
                            break;
                        case 'o':
                            if (i + 1 >= args.length) {
                                usage("Missing filename for -o option");
                            }
                            jacc.setOutputStream(Files.newOutputStream(Paths.get(args[++i])));
                            break;
                        default:
                            usage("Unrecognized command line option " + arg.charAt(j));
                            break;
                    }
                    j++;
                }
            } else if (!arg.endsWith(jacc.getSuffix())) {
                usage("Input file must have \"" + jacc.getSuffix() + "\" suffix: " + arg);
            } else {
                jacc.setInputStream(Files.newInputStream(Paths.get(arg)));
            }
        }
        if (jacc.getInputStream() == null) {
            usage("No input file(s) specified");
        } else {
            jacc.execute();
        }
    }

    public void execute() throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            if (dir == null) {
                dir = ".";
            }
            if (!dir.endsWith("/")) {
                dir = dir + "/";
            }
            final JaccJob job = new JaccJob(consoleHandler, writer, jaccSettings);
            job.parseGrammarStream(reader);
            job.buildTables();
            jaccSettings.fillBlanks(className);
            if (errorDiagnostics != null) {
                job.readErrorExamples(errorDiagnostics);
                errorDiagnostics.close();
            }
            if (consoleHandler.getNumFailures() == 0) {
                if (enableParserOutput) {
                    (new ParserOutput(consoleHandler, job)).write(dir + jaccSettings.getClassName() + ".java");
                }
                if (enableTokenOutput) {
                    (new TokensOutput(consoleHandler, job)).write(dir + jaccSettings.getInterfaceName() + ".java");
                }
                if (enableVerboseMachineDescription) {
                    (new TextOutput(consoleHandler, job, includeCalculations)).write(dir + jaccSettings.getClassName() + ".output");
                }
                if (parserInputs != null) {
                    job.readRunExample(parserInputs, includeStateNumbers);
                    parserInputs.close();
                }
            } else {
                writer.write("There were failures.\n");
            }
        }
    }

    private static void usage(String s) {
        logger.log(Level.INFO, s);
        String mesg = "usage: jacc [options] file.jacc ...\n" +
                "options (individually, or in combination):\n" +
                " -p        do not generate parser\n" +
                " -t        do not generate token specification\n" +
                " -v        output text description of machine\n" +
                " -f        show first/follow sets (with -h or -v)\n" +
                " -a        treat as LALR(1) grammar (default)\n" +
                " -s        treat as SLR(1) grammar\n" +
                " -0        treat as LR(0) grammar\n" +
                " -r file   run parser on input in file\n" +
                " -n        show state numbers in parser output\n" +
                " -e file   read error cases from file\n" +
                " -d dir    output files to directory\n" +
                " -o file   name of output file for parser runs\n";
        logger.log(Level.INFO, mesg);
    }
}

