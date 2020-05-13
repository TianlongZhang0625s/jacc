package org.xbib.jacc;

import org.xbib.jacc.compiler.Failure;
import org.xbib.jacc.compiler.Handler;
import org.xbib.jacc.compiler.Source;
import org.xbib.jacc.compiler.SourceLexer;
import org.xbib.jacc.compiler.Warning;

import java.io.IOException;

class JaccLexer extends SourceLexer implements JaccTokens {

    private int lastLiteral;

    JaccLexer(Handler handler, Source source) throws IOException {
        super(handler, source);
    }

    @Override
    public int nextToken() throws IOException {
        while (true) {
            skipWhitespace();
            markPosition();
            lexemeText = null;
            switch (c) {
                case -1:
                    token = ENDINPUT;
                    return token;
                case ':':
                    nextChar();
                    token = COLON;
                    return token;
                case ';':
                    nextChar();
                    token = SEMI;
                    return token;
                case '|':
                    nextChar();
                    token = BAR;
                    return token;
                case '<':
                    nextChar();
                    token = TOPEN;
                    return token;
                case '>':
                    nextChar();
                    token = TCLOSE;
                    return token;
                case '[':
                    nextChar();
                    token = BOPEN;
                    return token;
                case ']':
                    nextChar();
                    token = BCLOSE;
                    return token;
                case '.':
                    nextChar();
                    token = DOT;
                    return token;
                case '%':
                    if (directive() != -1) {
                        return token;
                    }
                    break;
                case '"':
                    if (string() != -1) {
                        return token;
                    }
                    break;
                case '\'':
                    if (literal() != -1) {
                        return token;
                    }
                    break;
                case '{':
                    if (action() != -1) {
                        return token;
                    }
                    break;
                case '/':
                    skipComment();
                    break;
                default:
                    if (Character.isJavaIdentifierStart((char) c)) {
                        return identifier();
                    }
                    if (Character.isDigit((char) c)) {
                        return number();
                    }
                    illegalCharacter();
                    nextChar();
                    break;
            }
        }
    }

    String readWholeLine() throws IOException {
        if (line == null) {
            return null;
        }
        String s = line;
        if (col > 0) {
            s = s.substring(col);
        }
        nextLine();
        return s;
    }

    String readCodeLine() throws IOException {
        while (isWhitespace(c)) {
            nextChar();
        }
        return readWholeLine();
    }

    private boolean isWhitespace(int i) {
        return i == 32 || i == 12;
    }

    private void skipWhitespace() throws IOException {
        while (isWhitespace(c)) {
            nextChar();
        }
        while (c == 10) {
            nextLine();
            while (isWhitespace(c)) {
                nextChar();
            }
        }
    }

    private void skipComment() throws IOException {
        nextChar();
        if (c == 47) {
            nextLine();
        } else {
            if (c == 42) {
                nextChar();
                do {
                    if (c == 42) {
                        do {
                            nextChar();
                        } while (c == 42);
                        if (c == 47) {
                            nextChar();
                            return;
                        }
                    }
                    if (c == -1) {
                        report(new Failure(getPos(), "Unterminated comment"));
                        return;
                    }
                    if (c == 10) {
                        nextLine();
                    } else {
                        nextChar();
                    }
                } while (true);
            }
            report(new Failure(getPos(), "Illegal comment format"));
        }
    }

    private int identifier() {
        int i = col;
        do {
            nextChar();
        } while (c != -1 && Character.isJavaIdentifierPart((char) c));
        lexemeText = line.substring(i, col);
        token = IDENT;
        return token;
    }

    private int directive() throws IOException {
        nextChar();
        if (c == 37) {
            nextChar();
            token = MARK;
            return token;
        }
        if (Character.isJavaIdentifierStart((char) c)) {
            identifier();
            switch (lexemeText) {
                case "token":
                    token = TOKEN;
                    return token;
                case "type":
                    token = TYPE;
                    return token;
                case "prec":
                    token = PREC;
                    return token;
                case "left":
                    token = LEFT;
                    return token;
                case "right":
                    token = RIGHT;
                    return token;
                case "nonassoc":
                    token = NONASSOC;
                    return token;
                case "start":
                    token = START;
                    return token;
                case "package":
                    token = PACKAGE;
                    return token;
                case "extends":
                    token = EXTENDS;
                    return token;
                case "implements":
                    token = IMPLEMENTS;
                    return token;
                case "semantic":
                    token = SEMANTIC;
                    return token;
                case "get":
                    token = GETTOKEN;
                    return token;
                case "next":
                    token = NEXTTOKEN;
                    return token;
                case "class":
                    token = CLASS;
                    return token;
                case "interface":
                    token = INTERFACE;
                    return token;
                default:
                    report(new Failure(getPos(), "Unrecognized directive"));
                    return ERROR;
            }
        }
        if (c == 123) {
            nextChar();
            return code();
        } else {
            report(new Failure(getPos(), "Illegal directive syntax"));
            return ERROR;
        }
    }

    private int code() throws IOException {
        int i = col;
        StringBuilder sb = null;
        do {
            if (c == 37) {
                do {
                    nextChar();
                } while (c == 37);
                if (c == 125) {
                    lexemeText = endBuffer(sb, i, col - 1);
                    nextChar();
                    token = CODE;
                    return token;
                }
            }
            if (c == -1) {
                report(new Failure(getPos(), "Code fragment terminator %} not found"));
                lexemeText = endBuffer(sb, i, col);
                token = CODE;
                return token;
            }
            if (c == 10) {
                if (sb == null) {
                    sb = new StringBuilder(line.substring(i, col));
                } else {
                    sb.append('\n');
                    sb.append(line);
                }
                nextLine();
            } else {
                nextChar();
            }
        } while (true);
    }

    private String endBuffer(StringBuilder sb, int i, int j) {
        if (sb == null) {
            return line.substring(i, j);
        }
        sb.append('\n');
        if (line != null) {
            sb.append(line, 0, j);
        }
        return sb.toString();
    }

    int getLastLiteral() {
        return lastLiteral;
    }

    private int number() {
        int i = col;
        int j = 0;
        int k = Character.digit((char) c, 10);
        do {
            j = 10 * j + k;
            nextChar();
            k = Character.digit((char) c, 10);
        } while (k >= 0);
        lexemeText = line.substring(i, col);
        lastLiteral = j;
        token = INTLIT;
        return token;
    }

    private int string() {
        nextChar();
        int i = col;
        while (c != 34 && c != 10 && c != -1) {
            if (c == 92) {
                escapeChar();
            } else {
                nextChar();
            }
        }
        lexemeText = line.substring(i, col);
        if (c == 34) {
            nextChar();
        } else {
            report(new Warning(getPos(), "Missing \" on string literal"));
        }
        token = STRLIT;
        return token;
    }

    private int literal() {
        int i = col;
        nextChar();
        if (c == 92) {
            escapeChar();
        } else {
            if (c != 39 && c != 10 && c != -1) {
                lastLiteral = c;
                nextChar();
            } else {
                report(new Failure(getPos(), "Illegal character literal syntax"));
                return ERROR;
            }
        }
        if (c == 39) {
            nextChar();
        } else {
            report(new Warning(getPos(), "Missing ' on character literal"));
        }
        lexemeText = line.substring(i, col);
        token = CHARLIT;
        return token;
    }

    private void escapeChar() {
        nextChar();
        switch (c) {
            case '"':
            case '\'':
            case '\\':
            case 'b':
            case 'f':
            case 'n':
            case 'r':
            case 't':
                lastLiteral = c;
                nextChar();
                return;
            default:
                break;
        }
        int i = Character.digit((char) c, 8);
        if (i >= 0) {
            lastLiteral = 0;
            int j = i >= 4 ? 2 : 3;
            do {
                lastLiteral = (lastLiteral << 3) + i;
                nextChar();
                i = Character.digit((char) c, 8);
            } while (i >= 0 && --j > 0);
        } else {
            report(new Failure(getPos(), "Syntax error in escape sequence"));
        }
    }

    private int action() throws IOException {
        int i = col;
        int j = 0;
        StringBuilder sb = null;
        do {
            if (c == 125) {
                if (--j == 0) {
                    nextChar();
                    lexemeText = endBuffer(sb, i, col);
                    token = ACTION;
                    return token;
                }
            } else {
                if (c == 123) {
                    j++;
                }
            }
            if (c == -1) {
                report(new Failure(getPos(), "Unterminated action"));
                lexemeText = endBuffer(sb, i, col);
                token = ACTION;
                return token;
            }
            if (c == 10) {
                if (sb == null) {
                    sb = new StringBuilder(line.substring(i, col));
                } else {
                    sb.append('\n');
                    sb.append(line);
                }
                nextLine();
            } else {
                nextChar();
            }
        } while (true);
    }

    private void illegalCharacter() {
        report(new Warning(getPos(), "Ignoring illegal character"));
    }
}
