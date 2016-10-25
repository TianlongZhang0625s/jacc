package org.xbib.jacc.compiler;

/**
 *
 */
abstract class Diagnostic extends Exception {

    private final String text;
    private final transient Position position;

    Diagnostic(String s) {
        this.position = null;
        this.text = s;
    }

    Diagnostic(Position position, String s) {
        this.position = position;
        this.text = s;
    }

    String getText() {
        return text;
    }

    Position getPos() {
        return position;
    }
}
