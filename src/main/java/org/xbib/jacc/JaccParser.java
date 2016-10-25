package org.xbib.jacc;

import org.xbib.jacc.compiler.Failure;
import org.xbib.jacc.compiler.Handler;
import org.xbib.jacc.compiler.Phase;
import org.xbib.jacc.compiler.Position;
import org.xbib.jacc.compiler.Warning;
import org.xbib.jacc.grammar.Grammar;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
class JaccParser extends Phase implements JaccTokens {

    private static final Logger logger = Logger.getLogger(JaccParser.class.getName());

    private final JaccSettings jaccSettings;
    private final NamedJaccSymbols terminals;
    private final NamedJaccSymbols nonterms;
    private final NumJaccSymbols literals;
    private int seqNo;
    private JaccLexer lexer;
    private int precedence;
    private JaccSymbol startSymbol;

    JaccParser(Handler handler, JaccSettings jaccSettings) {
        super(handler);
        this.jaccSettings = jaccSettings;
        this.terminals = new NamedJaccSymbols();
        this.nonterms = new NamedJaccSymbols();
        this.literals = new NumJaccSymbols();
        this.seqNo = 1;
        this.precedence = 0;
        this.startSymbol = null;
    }

    public Grammar getGrammar() {
        try {
            JaccSymbol[] ajaccsymbol;
            JaccProd[][] ajaccprod;
            int i = nonterms.getSize();
            int j = terminals.getSize() + literals.getSize() + 1;
            if (i == 0 || startSymbol == null) {
                report(new Failure("No nonterminals defined"));
                return null;
            }
            ajaccsymbol = new JaccSymbol[i + j];
            literals.fill(ajaccsymbol, terminals.fill(ajaccsymbol, nonterms.fill(ajaccsymbol, 0)));
            ajaccsymbol[(i + j) - 1] = new JaccSymbol("$end");
            ajaccsymbol[(i + j) - 1].setNum(0);
            int k = 1;
            for (int l = 0; l < j - 1; l++) {
                if (ajaccsymbol[i + l].getNum() >= 0) {
                    continue;
                }
                while (literals.find(k) != null) {
                    k++;
                }
                ajaccsymbol[i + l].setNum(k++);
            }
            int i1 = 0;
            while (true) {
                if (i1 >= i) {
                    break;
                }
                if (ajaccsymbol[i1] == startSymbol) {
                    if (i1 > 0) {
                        JaccSymbol jaccsymbol = ajaccsymbol[0];
                        ajaccsymbol[0] = ajaccsymbol[i1];
                        ajaccsymbol[i1] = jaccsymbol;
                    }
                    break;
                }
                i1++;
            }
            for (int j1 = 0; j1 < ajaccsymbol.length; j1++) {
                ajaccsymbol[j1].setTokenNo(j1);
            }
            ajaccprod = new JaccProd[nonterms.getSize()][];
            for (int k1 = 0; k1 < ajaccprod.length; k1++) {
                ajaccprod[k1] = ajaccsymbol[k1].getProds();
                if (ajaccprod[k1] == null || ajaccprod[k1].length == 0) {
                    report(new Failure("No productions for " + ajaccsymbol[k1].getName()));
                }
            }
            return new Grammar(ajaccsymbol, ajaccprod);
        } catch (JaccException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            report(new Failure("Internal problem " + e.getMessage()));
            return null;
        }
    }

    void parse(JaccLexer jacclexer) throws IOException {
        lexer = jacclexer;
        terminals.findOrAdd("error");
        parseDefinitions();
        if (jacclexer.getToken() != 1) {
            report(new Failure(jacclexer.getPos(), "Missing grammar"));
        } else {
            jacclexer.nextToken();
            parseGrammar();
            String s;
            if (jacclexer.getToken() == 1) {
                while ((s = jacclexer.readWholeLine()) != null) {
                    jaccSettings.addPostText(s);
                    jaccSettings.addPostText("\n");
                }
            }
        }
        jacclexer.close();
    }

    int[] parseSymbols(JaccLexer jacclexer) throws IOException {
        lexer = jacclexer;
        SymList symlist = null;
        while (true) {
            JaccSymbol jaccsymbol = parseDefinedSymbol();
            if (jaccsymbol == null) {
                if (jacclexer.getToken() != 0) {
                    report(new Warning(jacclexer.getPos(), "Ignoring extra tokens at end of input"));
                }
                jacclexer.close();
                return SymList.toIntArray(symlist);
            }
            symlist = new SymList(jaccsymbol, symlist);
            jacclexer.nextToken();
        }
    }

    void parseErrorExamples(JaccLexer jacclexer, JaccJob jaccjob) throws IOException {
        lexer = jacclexer;
        while (true) {
            if (jacclexer.getToken() != 5) {
                break;
            }
            String s = jacclexer.getLexeme();
            if (jacclexer.nextToken() == 58) {
                jacclexer.nextToken();
            } else {
                report(new Warning(jacclexer.getPos(), "A colon was expected here"));
            }
            int i;
            while (true) {
                Position position = jacclexer.getPos();
                SymList symlist = null;
                JaccSymbol jaccsymbol;
                while ((jaccsymbol = parseDefinedSymbol()) != null) {
                    symlist = new SymList(jaccsymbol, symlist);
                    jacclexer.nextToken();
                }
                int[] ai = SymList.toIntArray(symlist);
                jaccjob.errorExample(position, s, ai);
                i = jacclexer.getToken();
                if (i != 124) {
                    break;
                }
                jacclexer.nextToken();
            }
            if (i != 0) {
                if (i != 59) {
                    report(new Failure(jacclexer.getPos(), "Unexpected token; a semicolon was expected here"));
                    do {
                        i = jacclexer.nextToken();
                    } while (i != 59 && i != 0);
                }
                if (i == 59) {
                    jacclexer.nextToken();
                }
            }
        }
        if (jacclexer.getToken() != 0) {
            report(new Failure(jacclexer.getPos(), "Unexpected token; ignoring the rest of this file"));
        }
        jacclexer.close();
    }

    private void parseDefinitions() throws IOException {
        boolean flag = false;
        while (true) {
            switch (lexer.getToken()) {
                case 0: // '\0'
                case 1: // '\001'
                    return;
                default:
                    break;
            }
            if (parseDefinition()) {
                flag = false;
            } else {
                if (!flag) {
                    flag = true;
                    report(new Failure(lexer.getPos(), "Syntax error in definition"));
                }
                lexer.nextToken();
            }
        }
    }

    private boolean parseDefinition() throws IOException {
        switch (lexer.getToken()) {
            case CODE:
                jaccSettings.addPreText(lexer.getLexeme());
                lexer.nextToken();
                return true;
            case TOKEN:
                parseTokenDefn();
                return true;
            case TYPE:
                parseTypeDefn();
                return true;
            case LEFT:
                parseFixityDefn(Fixity.left(precedence++));
                return true;
            case NONASSOC:
                parseFixityDefn(Fixity.nonass(precedence++));
                return true;
            case RIGHT:
                parseFixityDefn(Fixity.right(precedence++));
                return true;
            case START:
                parseStart();
                return true;
            case CLASS:
                jaccSettings.setClassName(parseIdent(lexer.getLexeme(), jaccSettings.getClassName()));
                return true;
            case INTERFACE:
                jaccSettings.setInterfaceName(parseIdent(lexer.getLexeme(), jaccSettings.getInterfaceName()));
                return true;
            case PACKAGE:
                jaccSettings.setPackageName(parseDefnQualName(lexer.getLexeme(), jaccSettings.getPackageName()));
                return true;
            case EXTENDS:
                jaccSettings.setExtendsName(parseDefnQualName(lexer.getLexeme(), jaccSettings.getExtendsName()));
                return true;
            case IMPLEMENTS:
                lexer.nextToken();
                String s = parseQualName();
                if (s != null) {
                    jaccSettings.addImplementsNames(s);
                }
                return true;
            case SEMANTIC:
                jaccSettings.setTypeName(parseDefnQualName(lexer.getLexeme(), jaccSettings.getTypeName()));
                if (lexer.getToken() == 58) {
                    jaccSettings.setGetSemantic(lexer.readCodeLine());
                    lexer.nextToken();
                }
                return true;
            case GETTOKEN:
                jaccSettings.setGetToken(lexer.readCodeLine());
                lexer.nextToken();
                return true;
            case NEXTTOKEN:
                jaccSettings.setNextToken(lexer.readCodeLine());
                lexer.nextToken();
                return true;
            case IDENT:
            case CHARLIT:
            case STRLIT:
            case INTLIT:
            case ACTION:
            case PREC:
            default:
                return false;
        }
    }

    private void parseStart() throws IOException {
        Position position = lexer.getPos();
        lexer.nextToken();
        JaccSymbol jaccsymbol = parseNonterminal();
        if (jaccsymbol == null) {
            report(new Failure(position, "Missing start symbol"));
        } else {
            if (startSymbol == null) {
                startSymbol = jaccsymbol;
            } else {
                report(new Failure(position, "Multiple %start definitions are not permitted"));
            }
            lexer.nextToken();
        }
    }

    private String parseIdent(String s, String s1) throws IOException {
        Position position = lexer.getPos();
        if (lexer.nextToken() != 3) {
            report(new Failure(lexer.getPos(), "Syntax error in %" + s + " directive; identifier expected"));
            return s1;
        }
        String s2 = lexer.getLexeme();
        lexer.nextToken();
        if (s2 != null && s1 != null) {
            report(new Failure(position, "Multiple %" + s + " definitions are not permitted"));
            s2 = s1;
        }
        return s2;
    }

    private String parseDefnQualName(String s, String s1) throws IOException {
        Position position = lexer.getPos();
        lexer.nextToken();
        String s2 = parseQualName();
        if (s2 != null && s1 != null) {
            report(new Failure(position, "Multiple %" + s + " definitions are not permitted"));
            s2 = s1;
        }
        return s2;
    }

    private void parseTokenDefn() throws IOException {
        Position position = lexer.getPos();
        String s = optionalType();
        int i = 0;
        while (true) {
            JaccSymbol jaccsymbol = parseTerminal();
            if (jaccsymbol == null) {
                if (i == 0) {
                    report(new Failure(position, "Missing symbols in %token definition"));
                }
                return;
            }
            addType(jaccsymbol, s);
            lexer.nextToken();
            i++;
        }
    }

    private void parseTypeDefn() throws IOException {
        Position position = lexer.getPos();
        String s = optionalType();
        int i = 0;
        while (true) {
            JaccSymbol jaccsymbol = parseSymbol();
            if (jaccsymbol == null) {
                if (i == 0) {
                    report(new Failure(position, "Missing symbols in %type definition"));
                }
                return;
            }
            addType(jaccsymbol, s);
            lexer.nextToken();
            i++;
        }
    }

    private void parseFixityDefn(Fixity fixity) throws IOException {
        Position position = lexer.getPos();
        String s = optionalType();
        int i = 0;
        while (true) {
            JaccSymbol jaccsymbol = parseTerminal();
            if (jaccsymbol == null) {
                if (i == 0) {
                    report(new Failure(position, "Missing symbols in fixity definition"));
                }
                return;
            }
            addFixity(jaccsymbol, fixity);
            addType(jaccsymbol, s);
            lexer.nextToken();
            i++;
        }
    }

    private String optionalType() throws IOException {
        label0:
        {
            StringBuilder sb = new StringBuilder();
            label1:
            {
                if (lexer.nextToken() != TOPEN) {
                    break label0;
                }
                lexer.nextToken();
                sb.append(parseQualName());
                while (true) {
                    if (lexer.getToken() != BOPEN) {
                        break label1;
                    }
                    if (lexer.nextToken() != BCLOSE) {
                        break;
                    }
                    lexer.nextToken();
                    sb.append("[]");
                }
                report(new Failure(lexer.getPos(), "Missing ']' in array type"));
            }
            if (lexer.getToken() == TCLOSE) {
                lexer.nextToken();
            } else if (sb.length() > 0) {
                report(new Failure(lexer.getPos(), "Missing `>' in type specification"));
            }
            return sb.toString();
        }
        return null;
    }

    private void addFixity(JaccSymbol jaccsymbol, Fixity fixity) {
        if (!jaccsymbol.setFixity(fixity)) {
            report(new Warning(lexer.getPos(), "Cannot change fixity for " + jaccsymbol.getName()));
        }
    }

    private void addType(JaccSymbol jaccsymbol, String s) {
        if (s != null && !jaccsymbol.setType(s)) {
            report(new Warning(lexer.getPos(), "Cannot change type for " + jaccsymbol.getName()));
        }
    }

    private void parseGrammar() throws IOException {
        JaccSymbol jaccsymbol;
        while ((jaccsymbol = parseLhs()) != null) {
            if (startSymbol == null) {
                startSymbol = jaccsymbol;
            }
            jaccsymbol.addProduction(parseRhs());
            for (; lexer.getToken() == 124; jaccsymbol.addProduction(parseRhs())) {
                lexer.nextToken();
            }

            if (lexer.getToken() == 59) {
                lexer.nextToken();
            } else {
                report(new Warning(lexer.getPos(), "Missing ';' at end of rule"));
            }
        }
    }

    private JaccSymbol parseLhs() throws IOException {
        boolean flag = false;
        for (int i = lexer.getToken(); i != 1 && i != 0; ) {
            JaccSymbol jaccsymbol = parseNonterminal();
            if (jaccsymbol == null) {
                if (!flag) {
                    if (parseTerminal() != null) {
                        report(new Failure(lexer.getPos(), "Terminal symbol used on left hand side of rule"));
                    } else {
                        report(new Failure(lexer.getPos(), "Missing left hand side in rule"));
                    }
                    flag = true;
                }
                i = lexer.nextToken();
            } else {
                i = lexer.nextToken();
                if (i != 58) {
                    if (!flag) {
                        report(new Failure(lexer.getPos(), "Missing colon after left hand side of rule"));
                    }
                    flag = true;
                } else {
                    lexer.nextToken();
                    return jaccsymbol;
                }
            }
        }

        return null;
    }

    private JaccProd parseRhs() throws IOException {
        Fixity fixity = null;
        SymList symlist = null;
        while (true) {
            while (lexer.getToken() == 10) {
                lexer.nextToken();
                JaccSymbol jaccsymbol = parseSymbol();
                if (jaccsymbol == null) {
                    report(new Failure(lexer.getPos(), "Missing token for %prec directive"));
                } else if (jaccsymbol.getFixity() == null) {
                    report(new Failure(lexer.getPos(),
                            "Ignoring %prec annotation because no fixity has been specified for " +
                                    jaccsymbol.getName()));
                    lexer.nextToken();
                } else {
                    if (fixity != null) {
                        report(new Warning(lexer.getPos(), "Multiple %prec annotations in production"));
                    }
                    fixity = jaccsymbol.getFixity();
                    lexer.nextToken();
                }
            }
            JaccSymbol jaccsymbol1 = parseSymbol();
            if (jaccsymbol1 == null) {
                break;
            }
            symlist = new SymList(jaccsymbol1, symlist);
            lexer.nextToken();
        }
        String s = null;
        Position position = null;
        if (lexer.getToken() == 7) {
            s = lexer.getLexeme();
            position = lexer.getPos();
            lexer.nextToken();
        }
        JaccSymbol[] ajaccsymbol = SymList.toArray(symlist);
        return new JaccProd(fixity, ajaccsymbol, position, s, seqNo++);
    }

    private String parseQualName() throws IOException {
        if (lexer.getToken() != 3) {
            report(new Failure(lexer.getPos(), "Syntax error in qualified name; identifier expected"));
            return null;
        }
        StringBuilder stringbuffer = new StringBuilder();
        while (true) {
            stringbuffer.append(lexer.getLexeme());
            if (lexer.nextToken() != 46) {
                break;
            }
            if (lexer.nextToken() != 3) {
                report(new Failure(lexer.getPos(), "Syntax error in qualified name"));
                break;
            }
            stringbuffer.append('.');
        }
        return stringbuffer.toString();
    }

    private JaccSymbol parseTerminal() {
        String s = lexer.getLexeme();
        switch (lexer.getToken()) {
            case 3: // '\003'
                if (nonterms.find(s) != null) {
                    return null;
                } else {
                    return terminals.findOrAdd(s);
                }

            case 4: // '\004'
                return literals.findOrAdd(s, lexer.getLastLiteral());
            default:
                break;
        }
        return null;
    }

    private JaccSymbol parseNonterminal() {
        String s = lexer.getLexeme();
        if (lexer.getToken() == 3) {
            if (terminals.find(s) != null) {
                return null;
            } else {
                return nonterms.findOrAdd(s);
            }
        }
        return null;
    }

    private JaccSymbol parseSymbol() {
        String s = lexer.getLexeme();
        switch (lexer.getToken()) {
            case 3: // '\003'
                JaccSymbol jaccsymbol;
                jaccsymbol = terminals.find(s);
                if (jaccsymbol == null) {
                    jaccsymbol = nonterms.findOrAdd(s);
                }
                return jaccsymbol;
            case 4: // '\004'
                return literals.findOrAdd(s, lexer.getLastLiteral());
            default:
                break;
        }
        return null;
    }

    private JaccSymbol parseDefinedSymbol() {
        String s = lexer.getLexeme();
        switch (lexer.getToken()) {
            case 3: // '\003'
                JaccSymbol jaccsymbol = nonterms.find(s);
                return jaccsymbol == null ? terminals.find(s) : jaccsymbol;

            case 4: // '\004'
                return literals.find(lexer.getLastLiteral());
            default:
                break;
        }
        return null;
    }

    private static class SymList {

        JaccSymbol head;
        SymList tail;

        SymList(JaccSymbol jaccsymbol, SymList symlist) {
            head = jaccsymbol;
            tail = symlist;
        }

        static int length(SymList list) {
            SymList symlist = list;
            int i = 0;
            for (; symlist != null; symlist = symlist.tail) {
                i++;
            }
            return i;
        }

        static JaccSymbol[] toArray(SymList list) {
            SymList symlist = list;
            int i = length(symlist);
            JaccSymbol[] ajaccsymbol = new JaccSymbol[i];
            while (i > 0) {
                ajaccsymbol[--i] = symlist.head;
                symlist = symlist.tail;
            }
            return ajaccsymbol;
        }

        static int[] toIntArray(SymList list) {
            SymList symlist = list;
            int i = length(symlist);
            int[] ai = new int[i];
            while (i > 0) {
                ai[--i] = symlist.head.getTokenNo();
                symlist = symlist.tail;
            }
            return ai;
        }
    }
}
