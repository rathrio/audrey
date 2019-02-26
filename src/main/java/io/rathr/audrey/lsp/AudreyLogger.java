package io.rathr.audrey.lsp;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AudreyLogger {
    static {
        System.setProperty(
            "java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT] [AUDREY] [%4$-7s] %5$s %n"
        );
    }

    private static final Logger LOGGER = Logger.getLogger(AudreyServerLauncher.class.getName());

    public static void info(final String message) {
        LOGGER.log(Level.INFO, message);
    }

    public static void error(final String message) {
        LOGGER.log(Level.SEVERE, message);
    }
}
