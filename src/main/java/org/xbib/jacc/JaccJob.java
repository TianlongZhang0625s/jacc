package org.xbib.jacc;

import org.xbib.jacc.compiler.Failure;
import org.xbib.jacc.compiler.Handler;
import org.xbib.jacc.compiler.JavaSource;
import org.xbib.jacc.compiler.Phase;
import org.xbib.jacc.compiler.Position;
import org.xbib.jacc.compiler.Warning;
import org.xbib.jacc.grammar.Finitary;
import org.xbib.jacc.grammar.Grammar;
import org.xbib.jacc.grammar.LookaheadMachine;
import org.xbib.jacc.grammar.Parser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 *
 */
class JaccJob extends Phase {

    private final JaccSettings jaccSettings;

    private final JaccParser parser;

    private final Writer writer;

    private JaccTables tables;

    private JaccResolver resolver;

    JaccJob(Handler handler, Writer writer, JaccSettings jaccSettings) {
        super(handler);
        this.writer = writer;
        this.jaccSettings = jaccSettings;
        this.parser = new JaccParser(handler, jaccSettings);
    }

    JaccSettings getJaccSettings() {
        return jaccSettings;
    }

    JaccTables getTables() {
        return tables;
    }

    JaccResolver getResolver() {
        return resolver;
    }

    void parseGrammarStream(Reader reader) throws IOException {
        try (Reader reader1 = reader; JaccLexer jacclexer = new JaccLexer(getHandler(), new JavaSource(getHandler(), reader1))) {
            jacclexer.nextToken();
            parser.parse(jacclexer);
        }
    }

    void readErrorExamples(Reader reader) throws IOException {
        try (Reader reader1 = reader; JaccLexer jacclexer = new JaccLexer(getHandler(), new JavaSource(getHandler(), reader1))) {
            jacclexer.nextToken();
            parser.parseErrorExamples(jacclexer, this);
        }
    }

    void readRunExample(Reader reader, boolean flag) throws IOException {
        try (Reader reader1 = reader; JaccLexer jacclexer = new JaccLexer(getHandler(), new JavaSource(getHandler(), reader1))) {
            jacclexer.nextToken();
            runExample(parser.parseSymbols(jacclexer), flag);
        }
    }

    void buildTables() {
        Grammar grammar = parser.getGrammar();
        if (grammar == null || !allDeriveFinite(grammar)) {
            return;
        }
        LookaheadMachine lookaheadmachine = jaccSettings.makeMachine(grammar);
        this.resolver = new JaccResolver(lookaheadmachine);
        this.tables = new JaccTables(lookaheadmachine, resolver);
        if (tables.getProdUnused() > 0) {
            report(new Warning(tables.getProdUnused() + " rules never reduced"));
        }
        if (resolver.getNumSRConflicts() > 0 || resolver.getNumRRConflicts() > 0) {
            report(new Warning("conflicts: " +
                    resolver.getNumSRConflicts() +
                    " shift/reduce, " +
                    resolver.getNumRRConflicts() +
                    " reduce/reduce"));
        }
    }

    private boolean allDeriveFinite(Grammar grammar) {
        Finitary finitary = grammar.getFinitary();
        boolean flag = true;
        for (int i = 0; i < grammar.getNumNTs(); i++) {
            if (!finitary.isAt(i)) {
                flag = false;
                report(new Failure("No finite strings can be derived for " +
                        grammar.getNonterminal(i)));
            }
        }
        return flag;
    }

    private void runExample(int[] ai, boolean flag) throws IOException {
        Grammar grammar = parser.getGrammar();
        Parser parser1 = new Parser(tables, ai);
        writer.write("start ");
        do {
            writer.write(" :  ");
            parser1.display(writer, flag);
            switch (parser1.step()) {
                case 0:
                    writer.write("Accept!\n");
                    return;
                case 1:
                    writer.write("error in state ");
                    writer.write(parser1.getState());
                    writer.write(", next symbol ");
                    writer.write(grammar.getSymbol(parser1.getNextSymbol()).toString());
                    return;
                case 3:
                    writer.write("goto  ");
                    break;
                case 2:
                    writer.write("shift ");
                    break;
                case 4:
                    writer.write("reduce");
                    break;
                default:
                    break;
            }
        } while (true);
    }

    void errorExample(Position position, String s, int[] ai) {
        Parser p = new Parser(tables, ai);
        int i;
        do {
            i = p.step();
        } while (i != 0 && i != 1);
        if (i == 0) {
            report(new Warning(position, "Example for \"" + s + "\" does not produce an error"));
        } else {
            Grammar grammar = tables.getMachine().getGrammar();
            int j = p.getNextSymbol();
            if (grammar.isNonterminal(j)) {
                report(new Warning(position,
                        "Example for \"" + s + "\" reaches an error at the nonterminal " + grammar.getSymbol(j)));
            } else {
                int k = p.getState();
                if (!tables.errorAt(k, j)) {
                    report(new Failure(position, "Error example results in internal error"));
                } else {
                    String s1 = tables.errorSet(k, j, s);
                    if (s1 != null) {
                        report(new Warning(position,
                                "Multiple errors are possible in state " + k + " on terminal " +
                                        grammar.getSymbol(j) + ":\n - " + s1 + "\n - " + s));
                    }
                }
            }
        }
    }
}
