package org.xbib.jacc.compiler;

import java.io.IOException;

public abstract class SourceLexer extends Lexer implements AutoCloseable {

    protected String line;

    protected int col;

    protected int c;

    private Source source;

    private final SourcePosition pos;

    public SourceLexer(Handler handler, Source source) throws IOException {
        super(handler);
        this.col = -1;
        this.source = source;
        this.pos = new SourcePosition(source);
        this.line = source.readLine();
        nextChar();
    }

    @Override
    public Position getPos() {
        return pos.copy();
    }

    protected void markPosition() {
        pos.updateCoords(source.getLineNo(), col);
    }

    protected void nextLine() throws IOException {
        line = source.readLine();
        col = -1;
        nextChar();
    }

    protected int nextChar() {
        if (line == null) {
            c = -1;
            col = 0;
        } else {
            if (++col >= line.length()) {
                c = 10;
            } else {
                c = line.charAt(col);
            }
        }
        return c;
    }

    @Override
    public void close() throws IOException {
        if (source != null) {
            source.close();
            source = null;
        }
    }
}
