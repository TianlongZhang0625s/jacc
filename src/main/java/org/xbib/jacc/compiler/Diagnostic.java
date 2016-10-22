package org.xbib.jacc.compiler;

/**
 *
 */
abstract class Diagnostic extends Exception {

    private String text;
    private Position position;

    Diagnostic(String s) {
        text = s;
    }

    Diagnostic(Position position, String s) {
        this.position = position;
        text = s;
    }

    String getText() {
        return text;
    }

    Position getPos() {
        return position;
    }
}
