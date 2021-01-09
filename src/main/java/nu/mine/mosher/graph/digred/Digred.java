package nu.mine.mosher.graph.digred;

import nu.mine.mosher.graph.digred.gui.DigredGui;
import nu.mine.mosher.graph.digred.util.*;
import org.slf4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.prefs.Preferences;

public class Digred {
    private static Logger LOG;

    public static class LogConfig extends LogbackConfigurator {
    }

    private static class DigredVersion extends Version {
    }

    public static Preferences prefs() {
        return Preferences.userNodeForPackage(Digred.class);
    }

    public static void main(final String... args) {
        DigredGui gui = null;
        try {
            initLogging();
            LOG.info("version: {}", new DigredVersion().version());
            gui = DigredGui.create();
        } catch (final Throwable e) {
            logProgramTermination(e);
        } finally {
            System.out.flush();
            System.err.flush();
        }
        if (Objects.nonNull(gui)) {
            gui.waitForEventThread();
            LOG.info("Main application shutdown is complete.");
        }
    }

    private static void initLogging() {
        LogConfig.testSubsystem();
        LOG = LoggerFactory.getLogger(Digred.class);
    }

    private static void logProgramTermination(final Throwable e) {
        Objects.requireNonNull(e);
        if (Objects.nonNull(LOG)) {
            LOG.error("Program terminating due to error:", e);
        } else {
            try {
                final Path pathTemp = Files.createTempFile(Digred.class.getName()+"-", ".log");
                e.printStackTrace(new PrintStream(new FileOutputStream(pathTemp.toFile()), true));
            } catch (final Throwable reallyBad) {
                e.printStackTrace();
                reallyBad.printStackTrace();
            }
        }
    }
}
