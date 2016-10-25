package org.xbib.jacc.compiler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ConsoleHandler extends Handler {

    private static final Logger logger = Logger.getLogger(ConsoleHandler.class.getName());

    @Override
    protected void respondTo(Diagnostic diagnostic) {
        Position position = diagnostic.getPos();
        Level level;
        StringBuilder sb = new StringBuilder();
        if (diagnostic instanceof Warning) {
            level = Level.WARNING;
        } else {
            level = Level.SEVERE;
        }
        if (position != null) {
            sb.append(position.describe());
        }
        String s = diagnostic.getText();
        if (s != null) {
            sb.append(s);
        }
        logger.log(level, sb.toString());
    }
}
