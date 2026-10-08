/*
 *     Copyright 2020, 2021, 2026, Christopher Alan Mosher, New York, New York, USA, <cmosher01@gmail.com>.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        try {
            initLogging();
            LOG.info("version: {}", new DigredVersion().version());

            DigredGui.create().waitForEventThread();

            LOG.info("Main application shutdown is complete.");
        } catch (final Throwable e) {
            logProgramTermination(e);
        } finally {
            System.out.flush();
            System.err.flush();
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
