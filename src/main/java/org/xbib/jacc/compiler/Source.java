package org.xbib.jacc.compiler;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 */
public abstract class Source extends Phase implements Closeable {

    Source(Handler handler) {
        super(handler);
    }

    public abstract String readLine() throws IOException;

    public abstract int getLineNo();

    String getLine(int i) {
        return null;
    }

}
